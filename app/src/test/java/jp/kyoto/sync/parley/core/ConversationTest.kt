package jp.kyoto.sync.parley.core

import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationTest {

    @Test
    fun defaultTargetsAreCorrect() {
        val c = Conversation.JA_EN
        assertEquals(Lang.JA, c.myLang)
        assertEquals(Lang.EN, c.partnerLang)
        // incoming（相手→自分）は自分言語、outgoing（自分→相手）は相手言語。
        assertEquals(Lang.JA, c.incomingTarget)
        assertEquals(Lang.EN, c.outgoingTarget)
    }

    @Test
    fun swappedTargets() {
        val c = Conversation(myLang = Lang.EN, partnerLang = Lang.JA)
        assertEquals(Lang.EN, c.incomingTarget)
        assertEquals(Lang.JA, c.outgoingTarget)
    }

    @Test
    fun langCodes() {
        assertEquals("ja", Lang.JA.code)
        assertEquals("en", Lang.EN.code)
    }
}
