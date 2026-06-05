package jp.kyoto.sync.parley.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import jp.kyoto.sync.parley.core.Config
import kotlin.concurrent.thread

/**
 * 本体マイクから音声を取り込み、24kHz PCM16 mono(LE) のフレームを [onFrame] へ渡す。
 *
 * 録音は 48kHz で行い、[Decimator] で 24kHz へダウンサンプルしてから送る。
 * RECORD_AUDIO 権限は呼び出し側で取得済みであること。
 *
 * - 初期化・録音エラーは [onError] で通知する（無音や CPU ビジーループにしない）。
 * - 音声は連続して送る。無音判定（VAD）やノイズ低減は OpenAI Realtime 側が行うため、
 *   クライアント側でゲートを掛けると入力が分断され翻訳音声が途切れる。よって掛けない。
 */
class AudioCapturer(
    private val onFrame: (ByteArray) -> Unit,
    private val onError: (Throwable) -> Unit = {},
) {
    @Volatile private var running = false
    private var record: AudioRecord? = null
    private var worker: Thread? = null
    private val decimator = Decimator()

    @SuppressLint("MissingPermission")
    fun start() {
        if (running) return

        val minBuf = AudioRecord.getMinBufferSize(
            Config.CAPTURE_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minBuf <= 0) {
            onError(IllegalStateException("この端末は ${Config.CAPTURE_SAMPLE_RATE}Hz 録音に対応していません"))
            return
        }
        // 48kHz * 100ms = 4800 サンプル / フレーム
        val frameSamples = Config.CAPTURE_SAMPLE_RATE * Config.CHUNK_MS / 1000
        val bufSize = maxOf(minBuf, frameSamples * 2)

        val rec = try {
            AudioRecord(
                // 相手の声（やや遠方）向け。過剰な近接補正を避ける。
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                Config.CAPTURE_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufSize,
            )
        } catch (t: Throwable) {
            onError(t)
            return
        }
        if (rec.state != AudioRecord.STATE_INITIALIZED) {
            rec.release()
            onError(IllegalStateException("マイクを初期化できませんでした（他アプリが使用中の可能性）"))
            return
        }
        record = rec
        running = true
        rec.startRecording()

        worker = thread(name = "AudioCapturer") {
            val buf = ShortArray(frameSamples)
            try {
                while (running) {
                    val n = rec.read(buf, 0, buf.size)
                    if (n > 0) {
                        val frame48 = if (n == buf.size) buf else buf.copyOf(n)
                        val frame24 = decimator.process(frame48)
                        onFrame(Resampler.shortsToBytesLE(frame24))
                    } else if (n < 0 && running) {
                        // ERROR_INVALID_OPERATION(-3) など。放置するとビジーループになるため止める。
                        onError(IllegalStateException("マイク読み取りエラー (code=$n)"))
                        break
                    }
                }
            } catch (t: Throwable) {
                if (running) onError(t)
            }
        }
    }

    fun stop() {
        running = false
        worker?.join(500)
        worker = null
        runCatching { record?.stop() }
        record?.release()
        record = null
    }
}
