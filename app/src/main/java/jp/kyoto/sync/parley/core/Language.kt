package jp.kyoto.sync.parley.core

/** 翻訳に使う言語。code は OpenAI の出力言語コード。 */
enum class Lang(val code: String, val displayName: String) {
    JA("ja", "日本語"),
    EN("en", "English"),
}

/**
 * 会話の言語ペア。
 *
 * - incoming（相手→自分）: 相手の声を [myLang] に訳してイヤホンへ
 * - outgoing（自分→相手）: 自分の声を [partnerLang] に訳してスピーカーへ
 *
 * gpt-realtime-translate はソース言語を自動検出するため、指定するのはターゲット言語のみ。
 */
data class Conversation(
    val myLang: Lang,
    val partnerLang: Lang,
) {
    /** 相手→自分: ターゲットは自分の言語 */
    val incomingTarget: Lang get() = myLang

    /** 自分→相手: ターゲットは相手の言語 */
    val outgoingTarget: Lang get() = partnerLang

    companion object {
        /** 既定: 自分=日本語 / 相手=英語 */
        val JA_EN = Conversation(myLang = Lang.JA, partnerLang = Lang.EN)
    }
}
