package jp.kyoto.sync.honnyaku.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import jp.kyoto.sync.honnyaku.R
import jp.kyoto.sync.honnyaku.audio.AudioCapturer
import jp.kyoto.sync.honnyaku.audio.AudioPlayer
import jp.kyoto.sync.honnyaku.audio.Route
import jp.kyoto.sync.honnyaku.core.ApiKeyStore
import jp.kyoto.sync.honnyaku.core.Mode
import jp.kyoto.sync.honnyaku.core.SessionBus
import jp.kyoto.sync.honnyaku.core.Status
import jp.kyoto.sync.honnyaku.net.TranslationSession
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
        }
        return START_STICKY
    }

    private fun start() {
        val s = SessionBus.status.value
        if (s == Status.RUNNING || s == Status.CONNECTING) return

        startForegroundCompat()
        SessionBus.beginSession()
        SessionBus.status.value = Status.CONNECTING

        scope.launch {
            try {
                val apiKey = ApiKeyStore(this@TranslationService).load()
                if (apiKey.isNullOrBlank()) {
                    reportError(IllegalStateException("API キーが未設定です。設定画面から登録してください。"))
                    stopSelf()
                    return@launch
                }

                val conv = SessionBus.conversation.value

                earphonePlayer = AudioPlayer(this@TranslationService, Route.EARPHONE).apply { start() }
                speakerPlayer = AudioPlayer(this@TranslationService, Route.SPEAKER).apply { start() }

                incoming = TranslationSession(
                    apiKey = apiKey,
                    targetLang = conv.incomingTarget.code,
                    http = http,
                    listener = object : TranslationSession.Listener {
                        override fun onTranslatedAudio(pcm16: ByteArray) {
                            earphonePlayer?.write(pcm16) // 相手の訳はイヤホンへ
                        }

                        override fun onOutputTranscript(text: String) {
                            SessionBus.partnerTranscript.value += text
                        }

                        override fun onError(t: Throwable) = reportError(t)
                    },
                ).also { it.connect() }

                outgoing = TranslationSession(
                    apiKey = apiKey,
                    targetLang = conv.outgoingTarget.code,
                    http = http,
                    listener = object : TranslationSession.Listener {
                        override fun onTranslatedAudio(pcm16: ByteArray) {
                            speakerPlayer?.write(pcm16) // 自分の訳はスピーカーへ
                        }

                        override fun onOutputTranscript(text: String) {
                            SessionBus.myTranscript.value += text
                        }

                        override fun onError(t: Throwable) = reportError(t)
                    },
                ).also { it.connect() }

                // マイク取り込み開始。現在の Mode に応じて送り先を切り替える。
                capturer = AudioCapturer { frame ->
                    when (SessionBus.mode.value) {
                        Mode.LISTENING -> incoming?.sendAudio(frame)
                        Mode.SPEAKING -> outgoing?.sendAudio(frame)
                    }
                }.also { it.start() }

                SessionBus.status.value = Status.RUNNING
            } catch (t: Throwable) {
                reportError(t)
                stopSelf()
            }
        }
    }

    private fun reportError(t: Throwable) {
        SessionBus.errorMessage.value = t.message ?: t.toString()
        SessionBus.status.value = Status.ERROR
    }

    private fun startForegroundCompat() {
        val channelId = "parley_translation"
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "同時通訳", NotificationManager.IMPORTANCE_LOW),
            )
        }
        val notif: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Parley")
            .setContentText("同時通訳を実行中")
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
        capturer?.stop()
        incoming?.close()
        outgoing?.close()
        earphonePlayer?.stop()
        speakerPlayer?.stop()
        scope.cancel()
        SessionBus.status.value = Status.IDLE
        super.onDestroy()
    }

    companion object {
        private const val NOTIF_ID = 1
        const val ACTION_START = "jp.kyoto.sync.honnyaku.START"
        const val ACTION_STOP = "jp.kyoto.sync.honnyaku.STOP"
        const val ACTION_SET_MODE = "jp.kyoto.sync.honnyaku.SET_MODE"
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
