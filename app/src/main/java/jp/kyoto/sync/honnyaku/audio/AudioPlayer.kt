package jp.kyoto.sync.honnyaku.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import jp.kyoto.sync.honnyaku.core.Config

/** 再生先 */
enum class Route { EARPHONE, SPEAKER }

/**
 * 24kHz PCM16 mono を再生する。出力先（イヤホン / スピーカー）を選択できる。
 *
 * - incoming（相手→自分）= EARPHONE: 自分の耳にだけ訳を届ける
 * - outgoing（自分→相手）= SPEAKER : 相手に聞かせる
 */
class AudioPlayer(
    private val context: Context,
    private val route: Route,
) {
    private var track: AudioTrack? = null

    fun start() {
        if (track != null) return

        val minBuf = AudioTrack.getMinBufferSize(
            Config.API_SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(Config.API_SAMPLE_RATE)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        track = AudioTrack(
            attrs,
            format,
            maxOf(minBuf, Config.API_SAMPLE_RATE), // 余裕を持ったバッファ
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE,
        ).also {
            applyPreferredDevice(it)
            it.play()
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

    /** 24kHz PCM16 mono(LE) を再生キューへ書き込む（ブロッキング）。 */
    fun write(pcm16: ByteArray) {
        track?.write(pcm16, 0, pcm16.size)
    }

    fun stop() {
        runCatching { track?.stop() }
        track?.release()
        track = null
    }
}
