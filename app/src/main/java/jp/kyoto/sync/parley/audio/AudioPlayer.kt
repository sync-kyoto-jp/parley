package jp.kyoto.sync.parley.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import jp.kyoto.sync.parley.core.Config
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/** 再生先 */
enum class Route { EARPHONE, SPEAKER }

/**
 * 24kHz PCM16 mono を再生する。出力先（イヤホン / スピーカー）を選択できる。
 *
 * - incoming（相手→自分）= EARPHONE: 自分の耳にだけ訳を届ける
 * - outgoing（自分→相手）= SPEAKER : 相手に聞かせる
 *
 * [write] は呼び出しスレッド（WebSocket 受信スレッド）をブロックしないよう、
 * 内部キューに積むだけにする。実際の `AudioTrack.write`（バッファ満杯時ブロック）
 * は専用の再生スレッドで行う。これにより、再生のつまりが字幕など他メッセージの
 * 受信処理を止めてしまうのを防ぐ。
 */
class AudioPlayer(
    private val context: Context,
    private val route: Route,
) {
    private var track: AudioTrack? = null
    private val queue = LinkedBlockingQueue<ByteArray>()
    @Volatile private var running = false
    private var worker: Thread? = null

    /** @throws IllegalStateException AudioTrack の初期化に失敗した場合 */
    fun start() {
        if (track != null) return

        val minBuf = AudioTrack.getMinBufferSize(
            Config.API_SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        check(minBuf > 0) { "この端末は ${Config.API_SAMPLE_RATE}Hz 再生に対応していません" }

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(Config.API_SAMPLE_RATE)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        val t = AudioTrack(
            attrs,
            format,
            maxOf(minBuf, Config.API_SAMPLE_RATE), // 余裕を持ったバッファ
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE,
        )
        if (t.state != AudioTrack.STATE_INITIALIZED) {
            t.release()
            error("AudioTrack の初期化に失敗しました（$route）")
        }
        applyPreferredDevice(t)
        t.play()
        track = t

        running = true
        worker = thread(name = "AudioPlayer-$route") {
            try {
                while (running) {
                    val buf = queue.poll(100, TimeUnit.MILLISECONDS) ?: continue
                    var off = 0
                    while (off < buf.size && running) {
                        val w = t.write(buf, off, buf.size - off)
                        if (w < 0) return@thread // ERROR_DEAD_OBJECT など。再生スレッドを終了。
                        off += w
                    }
                }
            } catch (_: InterruptedException) {
                // stop() による割り込み。正常終了。
            }
        }
    }

    /** 出力デバイスを route に合わせて優先指定する。 */
    private fun applyPreferredDevice(t: AudioTrack) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val target: AudioDeviceInfo? = when (route) {
            Route.EARPHONE -> devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        it.type == AudioDeviceInfo.TYPE_BLE_HEADSET)
            }

            Route.SPEAKER -> devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
            }
        }
        if (target != null) t.preferredDevice = target
    }

    /** 24kHz PCM16 mono(LE) を再生キューへ積む（非ブロッキング）。 */
    fun write(pcm16: ByteArray) {
        if (!running) return
        // ネットワークのバーストで貯まりすぎたら古いものを捨てて遅延を抑える。
        while (queue.size >= MAX_QUEUED_CHUNKS) queue.poll()
        queue.offer(pcm16)
    }

    fun stop() {
        running = false
        worker?.interrupt()
        worker?.join(300)
        worker = null
        queue.clear()
        runCatching { track?.stop() }
        track?.release()
        track = null
    }

    companion object {
        /** 再生キューの上限（チャンク数）。これを超えたら古いチャンクを破棄。 */
        private const val MAX_QUEUED_CHUNKS = 96
    }
}
