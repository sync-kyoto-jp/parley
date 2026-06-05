package jp.kyoto.sync.parley.audio

/**
 * PCM16 のバイト列変換ユーティリティ。
 *
 * レート変換（48k→24k）はステートフルな [Decimator] が担当する。
 */
object Resampler {

    /** ShortArray(PCM16) → little-endian ByteArray */
    fun shortsToBytesLE(samples: ShortArray): ByteArray {
        val bytes = ByteArray(samples.size * 2)
        var bi = 0
        for (s in samples) {
            val v = s.toInt()
            bytes[bi++] = (v and 0xFF).toByte()
            bytes[bi++] = ((v shr 8) and 0xFF).toByte()
        }
        return bytes
    }
}
