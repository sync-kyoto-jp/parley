package jp.kyoto.sync.parley.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 48kHz → 24kHz の 2:1 デシメータ（アンチエイリアス付き）。
 *
 * 単純な隣接平均ではエイリアシング（折り返し雑音）が残るため、間引きの前に
 * Hamming 窓 sinc 設計の FIR ローパスを適用する。フレーム境界で生じる不連続
 * （クリック）を避けるため、直前フレーム末尾のサンプルと間引き位相を内部に
 * 保持する**ステートフル**実装。1 セッション = 1 インスタンスで使うこと。
 *
 * 計算量はフレーム100ms（出力2400サンプル）あたり 2400 × numTaps 程度で軽量。
 */
class Decimator(numTaps: Int = DEFAULT_TAPS) {

    // DC ゲイン 1 に正規化済みの FIR 係数。
    private val taps: FloatArray = buildLowPass(numTaps, CUTOFF)

    // 直前フレーム末尾の入力サンプル（畳み込みの履歴）。ゼロ初期化。
    private val history = ShortArray(taps.size - 1)

    // 次フレーム先頭での間引き位相（0 または 1）。フレーム長が奇数でも連続性を保つ。
    private var phase = 0

    /** 48kHz mono を 24kHz mono に変換して返す。 */
    fun process(input: ShortArray): ShortArray {
        val n = taps.size
        val h = history.size // = n - 1

        // [履歴 | 入力] を連結してフィルタの参照域を作る。
        val ext = ShortArray(h + input.size)
        System.arraycopy(history, 0, ext, 0, h)
        for (i in input.indices) ext[h + i] = input[i]

        val start = phase
        val outLen = if (start >= input.size) 0 else (input.size - start + 1) / 2
        val out = ShortArray(outLen)

        var oi = 0
        var p = start
        while (p < input.size) {
            // y = Σ taps[k] * ext[(h + p) - k]
            var acc = 0f
            val base = h + p
            for (k in 0 until n) acc += taps[k] * ext[base - k]
            out[oi++] = acc.coerceIn(-32768f, 32767f).toInt().toShort()
            p += 2
        }

        // 次フレームへ位相と履歴を引き継ぐ。
        phase = p - input.size
        if (ext.size >= h) System.arraycopy(ext, ext.size - h, history, 0, h)
        return out
    }

    companion object {
        /** 係数長（奇数）。長いほど急峻だが計算量増。23 で実用十分。 */
        private const val DEFAULT_TAPS = 23

        /**
         * 正規化カットオフ（cycles/sample, 入力48kHz基準）。
         * 出力ナイキストは 12kHz。0.225×48k ≈ 10.8kHz を通過帯域端にし、
         * 12kHz までで十分減衰させて折り返しを抑える。
         */
        private const val CUTOFF = 0.225f

        /** Hamming 窓 sinc によるローパス FIR を生成（DC ゲイン 1 へ正規化）。 */
        private fun buildLowPass(n: Int, fc: Float): FloatArray {
            val h = FloatArray(n)
            val mid = (n - 1) / 2.0
            var sum = 0.0
            for (i in 0 until n) {
                val x = i - mid
                val ideal = if (x == 0.0) 2.0 * fc else sin(2.0 * PI * fc * x) / (PI * x)
                val window = 0.54 - 0.46 * cos(2.0 * PI * i / (n - 1)) // Hamming
                val v = ideal * window
                h[i] = v.toFloat()
                sum += v
            }
            for (i in 0 until n) h[i] = (h[i] / sum).toFloat()
            return h
        }
    }
}
