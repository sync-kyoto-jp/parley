package jp.kyoto.sync.parley.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class DecimatorTest {

    @Test
    fun halvesEvenLength() {
        val out = Decimator().process(ShortArray(4800))
        assertEquals(2400, out.size)
    }

    @Test
    fun silenceStaysSilent() {
        val out = Decimator().process(ShortArray(4800))
        assertTrue(out.all { it.toInt() == 0 })
    }

    @Test
    fun dcGainIsUnityAtSteadyState() {
        val c: Short = 1000
        val out = Decimator().process(ShortArray(4800) { c })
        // 履歴がゼロ初期化のため先頭は過渡応答。十分ウォームした末尾で DC ゲイン≒1 を確認。
        assertTrue("last=${out.last()}", abs(out.last() - 1000) <= 2)
    }

    @Test
    fun maintainsLengthAcrossFrames() {
        val d = Decimator()
        assertEquals(2400, d.process(ShortArray(4800)).size)
        assertEquals(2400, d.process(ShortArray(4800)).size)
    }

    @Test
    fun handlesOddLengthWithPhaseCarry() {
        val d = Decimator()
        // 5 サンプル → p=0,2,4 の 3 サンプル。次フレームは位相 1 から開始。
        assertEquals(3, d.process(ShortArray(5)).size)
    }
}
