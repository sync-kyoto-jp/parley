package jp.kyoto.sync.parley.core

/**
 * 翻訳に使う言語。[code] は OpenAI の出力言語コード（ISO 639-1）、
 * [displayName] は各言語の自言語名（ピッカー表示用）。
 *
 * 一覧は gpt-realtime-translate の対応出力言語に合わせている。
 */
enum class Lang(val code: String, val displayName: String) {
    EN("en", "English"),
    FR("fr", "Français"),
    DE("de", "Deutsch"),
    HI("hi", "हिन्दी"),
    ID("id", "Bahasa Indonesia"),
    IT("it", "Italiano"),
    JA("ja", "日本語"),
    KO("ko", "한국어"),
    PT("pt", "Português"),
    RU("ru", "Русский"),
    ES("es", "Español"),
    VI("vi", "Tiếng Việt"),
    ;

    companion object {
        fun fromCode(code: String): Lang? = entries.firstOrNull { it.code == code }
    }
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
