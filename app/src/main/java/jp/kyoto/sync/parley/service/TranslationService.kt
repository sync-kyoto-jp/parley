package jp.kyoto.sync.parley.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import jp.kyoto.sync.parley.R
import jp.kyoto.sync.parley.audio.AudioCapturer
import jp.kyoto.sync.parley.audio.AudioPlayer
import jp.kyoto.sync.parley.audio.Route
import jp.kyoto.sync.parley.core.ApiKeyStore
import jp.kyoto.sync.parley.core.AppSettings
import jp.kyoto.sync.parley.core.Mode
import jp.kyoto.sync.parley.core.SessionBus
import jp.kyoto.sync.parley.core.Status
import jp.kyoto.sync.parley.net.TranslationSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * 双方向同時通訳のフォアグラウンドサービス。
 *
 * 設計（ハウリング回避のため、まずは半二重 / プッシュトゥトーク）:
 *  - Mode.LISTENING: マイク=相手の声 → incoming(訳:自分言語) → イヤホン
 *  - Mode.SPEAKING : マイク=自分の声 → outgoing(訳:相手言語) → スピーカー
 *
 * マイクのフレームは現在の Mode のセッションだけに流すので、スピーカー出力が
 * マイクへ回り込むハウリングを構造的に防げる。
 *
 * API キーは [ApiKeyStore] から読み出し、各セッションに直接渡す（BYOK）。
 *
 * 半二重ゆえ片方向が落ちると通訳は成立しないため、致命的エラーでは [fatalError] で
 * 全リソースを解放して停止する（マイクを開いたまま放置しない）。
 */
class TranslationService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val http = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // WebSocket は無期限
        .build()

    private var capturer: AudioCapturer? = null
    private var earphonePlayer: AudioPlayer? = null // incoming → 自分の耳
    private var speakerPlayer: AudioPlayer? = null   // outgoing → 相手へ

    private var incoming: TranslationSession? = null // 相手→自分
    private var outgoing: TranslationSession? = null // 自分→相手

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stopSelf()
            ACTION_SET_MODE -> {
                val m = intent.getStringExtra(EXTRA_MODE)?.let { Mode.valueOf(it) } ?: Mode.LISTENING
                SessionBus.mode.value = m
            }
            // intent == null（START_NOT_STICKY 後の異常再起動など）や未知の action は何もしない。
        }
        // リアルタイム通訳はマイクとユーザー操作が前提。OS に勝手に再起動させない。
        return START_NOT_STICKY
    }

    private fun start() {
        val s = SessionBus.status.value
        if (s == Status.RUNNING || s == Status.CONNECTING) return

        // マイク権限が無ければフォアグラウンド(microphone)開始前に弾く（Android 14 で必須）。
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            reportError(IllegalStateException(getString(R.string.error_no_mic_permission)))
            stopSelf()
            return
        }

        startForegroundCompat()
        SessionBus.beginSession()
        SessionBus.status.value = Status.CONNECTING

        scope.launch {
            try {
                teardown() // 念のため前回分が残っていれば解放してから組み直す。

                val apiKey = ApiKeyStore(this@TranslationService).load()
                if (apiKey.isNullOrBlank()) {
                    fatalError(IllegalStateException(getString(R.string.error_no_api_key)))
                    return@launch
                }

                val transcriptionModel = AppSettings(this@TranslationService).transcriptionModel.id
                val conv = SessionBus.conversation.value

                earphonePlayer = AudioPlayer(this@TranslationService, Route.EARPHONE).apply { start() }
                speakerPlayer = AudioPlayer(this@TranslationService, Route.SPEAKER).apply { start() }

                incoming = TranslationSession(
                    apiKey = apiKey,
                    targetLang = conv.incomingTarget.code,
                    transcriptionModel = transcriptionModel,
                    http = http,
                    listener = object : TranslationSession.Listener {
                        override fun onTranslatedAudio(pcm16: ByteArray) {
                            earphonePlayer?.write(pcm16) // 相手の訳はイヤホンへ
                        }

                        override fun onOutputTranscript(text: String) {
                            SessionBus.appendPartnerTranscript(text)
                        }

                        override fun onInputTranscript(text: String) {
                            SessionBus.appendPartnerSource(text) // 相手の原文
                        }

                        override fun onError(t: Throwable) = fatalError(t)
                    },
                ).also { it.connect() }

                outgoing = TranslationSession(
                    apiKey = apiKey,
                    targetLang = conv.outgoingTarget.code,
                    transcriptionModel = transcriptionModel,
                    http = http,
                    listener = object : TranslationSession.Listener {
                        override fun onTranslatedAudio(pcm16: ByteArray) {
                            speakerPlayer?.write(pcm16) // 自分の訳はスピーカーへ
                        }

                        override fun onOutputTranscript(text: String) {
                            SessionBus.appendMyTranscript(text)
                        }

                        // 自分の原文は表示しない: 出力（訳）音声がスピーカーから流れ、本体マイクへ
                        // 回り込んで（エコー）原文へ混入するため（例:「こんにちは。Guten Tag.」）。
                        // 相手方向はイヤホン出力なのでエコーしにくく、原文を表示する。
                        override fun onError(t: Throwable) = fatalError(t)
                    },
                ).also { it.connect() }

                // マイク取り込み開始。現在の Mode に応じて送り先を切り替える。
                capturer = AudioCapturer(
                    onFrame = { frame ->
                        when (SessionBus.mode.value) {
                            Mode.LISTENING -> incoming?.sendAudio(frame)
                            Mode.SPEAKING -> outgoing?.sendAudio(frame)
                        }
                    },
                    onError = { t -> fatalError(t) },
                ).also { it.start() }

                SessionBus.status.value = Status.RUNNING
            } catch (t: Throwable) {
                fatalError(t)
            }
        }
    }

    /** エラーを通知し、全リソースを解放してサービスを止める（onDestroy で teardown）。 */
    private fun fatalError(t: Throwable) {
        reportError(t)
        stopSelf()
    }

    private fun reportError(t: Throwable) {
        SessionBus.errorMessage.value = t.message ?: t.toString()
        SessionBus.status.value = Status.ERROR
    }

    /** 取り込み・再生・セッションを全て停止し参照を破棄する（多重 start でのリーク防止）。 */
    private fun teardown() {
        capturer?.stop(); capturer = null
        incoming?.close(); incoming = null
        outgoing?.close(); outgoing = null
        earphonePlayer?.stop(); earphonePlayer = null
        speakerPlayer?.stop(); speakerPlayer = null
    }

    private fun startForegroundCompat() {
        val channelId = "parley_translation"
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_LOW),
            )
        }
        val notif: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notif_running))
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIF_ID, notif)
        }
    }

    override fun onDestroy() {
        teardown()
        scope.cancel()
        runCatching {
            http.dispatcher.executorService.shutdown()
            http.connectionPool.evictAll()
        }
        // エラー終了時は ERROR を保持し、通常停止のみ IDLE に戻す。
        if (SessionBus.status.value != Status.ERROR) SessionBus.status.value = Status.IDLE
        super.onDestroy()
    }

    companion object {
        private const val NOTIF_ID = 1
        const val ACTION_START = "jp.kyoto.sync.parley.START"
        const val ACTION_STOP = "jp.kyoto.sync.parley.STOP"
        const val ACTION_SET_MODE = "jp.kyoto.sync.parley.SET_MODE"
        const val EXTRA_MODE = "mode"

        fun start(ctx: Context) = ctx.startForegroundService(
            Intent(ctx, TranslationService::class.java).setAction(ACTION_START),
        )

        fun stop(ctx: Context) = ctx.startService(
            Intent(ctx, TranslationService::class.java).setAction(ACTION_STOP),
        )

        fun setMode(ctx: Context, mode: Mode) = ctx.startService(
            Intent(ctx, TranslationService::class.java)
                .setAction(ACTION_SET_MODE)
                .putExtra(EXTRA_MODE, mode.name),
        )
    }
}
