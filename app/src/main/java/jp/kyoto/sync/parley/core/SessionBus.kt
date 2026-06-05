package jp.kyoto.sync.parley.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

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
 *
 * 字幕は WebSocket 受信スレッド（incoming/outgoing で別スレッド）から更新されるため、
 * 追記は [update] によるアトミックな compare-and-set で行う。さらに長時間セッションで
 * 文字列が無限に伸びて再コンポーズが重くなるのを防ぐため、上限で古い側を間引く。
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

    /** 相手字幕にデルタを追記（スレッドセーフ・上限あり）。 */
    fun appendPartnerTranscript(delta: String) = append(partnerTranscript, delta)

    /** 自分字幕にデルタを追記（スレッドセーフ・上限あり）。 */
    fun appendMyTranscript(delta: String) = append(myTranscript, delta)

    private fun append(flow: MutableStateFlow<String>, delta: String) {
        if (delta.isEmpty()) return
        flow.update { current ->
            val combined = current + delta
            if (combined.length > MAX_TRANSCRIPT_CHARS) {
                combined.takeLast(MAX_TRANSCRIPT_CHARS)
            } else {
                combined
            }
        }
    }

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

    /** 字幕の保持上限（文字数）。これを超えると古い側から間引く。 */
    private const val MAX_TRANSCRIPT_CHARS = 16_000
}
