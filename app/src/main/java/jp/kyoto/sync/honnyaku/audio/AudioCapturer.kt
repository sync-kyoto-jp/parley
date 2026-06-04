package jp.kyoto.sync.honnyaku.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import jp.kyoto.sync.honnyaku.core.Config
import kotlin.concurrent.thread

/**
 * 本体マイクから音声を取り込み、24kHz PCM16 mono(LE) のフレームを [onFrame] へ渡す。
 *
 * 録音は 48kHz で行い、24kHz へダウンサンプルしてから送る。
 * RECORD_AUDIO 権限は呼び出し側で取得済みであること。
 */
class AudioCapturer(
    private val onFrame: (ByteArray) -> Unit,
) {
    @Volatile private var running = false
    private var record: AudioRecord? = null
    private var worker: Thread? = null

    @SuppressLint("MissingPermission")
    fun start() {
        if (running) return

        val minBuf = AudioRecord.getMinBufferSize(
            Config.CAPTURE_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        // 48kHz * 100ms = 4800 サンプル / フレーム
        val frameSamples = Config.CAPTURE_SAMPLE_RATE * Config.CHUNK_MS / 1000
        val bufSize = maxOf(minBuf, frameSamples * 2)

        record = AudioRecord(
            // 相手の声（やや遠方）向け。過剰な近接補正を避ける。
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            Config.CAPTURE_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufSize,
        )

        running = true
        record?.startRecording()

        worker = thread(name = "AudioCapturer") {
            val buf = ShortArray(frameSamples)
            while (running) {
                val n = record?.read(buf, 0, buf.size) ?: 0
                if (n > 0) {
                    val frame48 = if (n == buf.size) buf else buf.copyOf(n)
                    val frame24 = Resampler.downsample48to24(frame48)
                    onFrame(Resampler.shortsToBytesLE(frame24))
                }
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
