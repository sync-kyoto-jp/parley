package jp.kyoto.sync.honnyaku.audio

/**
 * シンプルな PCM16 リサンプラ / 変換ユーティリティ。
 *
 * 注: 線形補間ベースの簡易実装。音質を上げる場合はポリフェーズ FIR 等に置き換える。
 */
object Resampler {

    /** 48kHz → 24kHz (2:1)。隣接サンプルを平均して簡易ローパス + 間引き。 */
    fun downsample48to24(input: ShortArray): ShortArray {
        val out = ShortArray(input.size / 2)
        var j = 0
        var i = 0
        while (i + 1 < input.size) {
            out[j++] = ((input[i] + input[i + 1]) / 2).toShort()
            i += 2
        }
        return out
    }

    /** 任意レート変換（線形補間） */
    fun resampleLinear(input: ShortArray, inRate: Int, outRate: Int): ShortArray {
        if (inRate == outRate || input.isEmpty()) return input
        val outLen = (input.size.toLong() * outRate / inRate).toInt()
        val out = ShortArray(outLen)
        val ratio = inRate.toDouble() / outRate
        for (n in 0 until outLen) {
            val pos = n * ratio
            val i = pos.toInt()
            val frac = pos - i
            val s0 = input[i.coerceIn(0, input.size - 1)].toInt()
            val s1 = input[(i + 1).coerceIn(0, input.size - 1)].toInt()
            out[n] = (s0 + (s1 - s0) * frac).toInt().toShort()
        }
        return out
    }

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

    /** little-endian ByteArray → ShortArray(PCM16) */
    fun bytesLEToShorts(bytes: ByteArray): ShortArray {
        val out = ShortArray(bytes.size / 2)
        var i = 0
        var j = 0
        while (i + 1 < bytes.size) {
            val lo = bytes[i].toInt() and 0xFF
            val hi = bytes[i + 1].toInt()
            out[j++] = ((hi shl 8) or lo).toShort()
            i += 2
        }
        return out
    }
}
