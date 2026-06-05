package jp.kyoto.sync.parley.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class ResamplerTest {

    @Test
    fun encodesLittleEndian() {
        val bytes = Resampler.shortsToBytesLE(shortArrayOf(0x0102, -1))
        assertEquals(4, bytes.size)
        assertArrayEquals(
            byteArrayOf(0x02, 0x01, 0xFF.toByte(), 0xFF.toByte()),
            bytes,
        )
    }

    @Test
    fun lengthIsTwiceSampleCount() {
        assertEquals(20, Resampler.shortsToBytesLE(ShortArray(10)).size)
    }
}
