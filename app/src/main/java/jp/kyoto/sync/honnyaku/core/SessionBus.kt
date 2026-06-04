package jp.kyoto.sync.honnyaku.core

import kotlinx.coroutines.flow.MutableStateFlow

/** 接続状態 */
enum class Status { IDLE, CONNECTING, RUNNING, ERROR }

/**
 * 入出力モード（ハウリング回避のため半二重 / プッシュトゥトーク）。
 * - LISTENING: マイク=相手の声 → 自分言語に訳してイヤホンへ
 * - SPEAKING : マイク=自分の声 → 相手言語に訳してスピーカーへ
 */
enum class Mode { LISTENING, SPEAKING }

/**
 * Service と UI を繋ぐ軽量な状態バス。
 *
 * 雛形のため object（プロセス内シングルトン）で簡素に実装している。
 * 規模が大きくなったら Repository + DI（Hilt 等）への置き換えを推奨。
 */
object SessionBus {
    val status = MutableStateFlow(Status.IDLE)
    val mode = MutableStateFlow(Mode.LISTENING)
    val conversation = MutableStateFlow(Conversation.JA_EN)

    /** 相手の発話を自分言語に訳した字幕（イヤホン側） */
    val partnerTranscript = MutableStateFlow("")

    /** 自分の発話を相手言語に訳した字幕（スピーカー側） */
    val myTranscript = MutableStateFlow("")

    val errorMessage = MutableStateFlow<String?>(null)

    /** 現在の会話の開始時刻(epoch ms)。0 は未開始/クリア済み。履歴保存の ID にも使う。 */
    @Volatile
    var currentStartedAt: Long = 0L

    /** 新しいセッション開始: 画面をクリアし、開始時刻を記録する。 */
    fun beginSession() {
        partnerTranscript.value = ""
        myTranscript.value = ""
        errorMessage.value = null
        currentStartedAt = System.currentTimeMillis()
    }

    /** 画面の会話をクリアする（セッション識別子も破棄）。 */
    fun clearCurrent() {
        partnerTranscript.value = ""
        myTranscript.value = ""
        errorMessage.value = null
        currentStartedAt = 0L
    }
}
