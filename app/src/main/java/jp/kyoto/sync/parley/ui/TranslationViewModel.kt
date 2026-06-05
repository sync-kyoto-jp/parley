package jp.kyoto.sync.parley.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import jp.kyoto.sync.parley.core.ApiKeyStore
import jp.kyoto.sync.parley.core.ChatSession
import jp.kyoto.sync.parley.core.Conversation
import jp.kyoto.sync.parley.core.HistoryRepository
import jp.kyoto.sync.parley.core.Mode
import jp.kyoto.sync.parley.core.SessionBus
import jp.kyoto.sync.parley.service.TranslationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 画面の状態は [SessionBus] の StateFlow をそのまま公開し、
 * 操作は [TranslationService] への Intent として送る。
 * API キーは [ApiKeyStore]、会話履歴は [HistoryRepository] が担当する。
 */
class TranslationViewModel(app: Application) : AndroidViewModel(app) {

    private val keyStore = ApiKeyStore(app)
    private val history = HistoryRepository(app)

    private val _hasApiKey = MutableStateFlow(keyStore.hasKey())
    val hasApiKey: StateFlow<Boolean> = _hasApiKey

    private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val sessions: StateFlow<List<ChatSession>> = _sessions

    val status = SessionBus.status
    val mode = SessionBus.mode
    val conversation = SessionBus.conversation
    val partnerTranscript = SessionBus.partnerTranscript
    val myTranscript = SessionBus.myTranscript
    val errorMessage = SessionBus.errorMessage

    // --- API キー ---

    fun saveApiKey(key: String) {
        keyStore.save(key.trim())
        _hasApiKey.value = true
    }

    fun clearApiKey() {
        keyStore.clear()
        _hasApiKey.value = false
    }

    // --- セッション制御 ---

    fun start() {
        persistCurrent()                            // 直前の会話を履歴へ
        TranslationService.start(getApplication())  // 内部で beginSession() しクリア
    }

    fun stop() {
        persistCurrent()                            // 停止時に履歴へ
        TranslationService.stop(getApplication())
    }

    /** 新しい会話: 直前を履歴へ保存してから画面をクリア。 */
    fun newConversation() {
        persistCurrent()
        SessionBus.clearCurrent()
    }

    fun swapDirection() {
        val c = SessionBus.conversation.value
        SessionBus.conversation.value = Conversation(myLang = c.partnerLang, partnerLang = c.myLang)
    }

    fun pttDown() = TranslationService.setMode(getApplication(), Mode.SPEAKING)

    fun pttUp() = TranslationService.setMode(getApplication(), Mode.LISTENING)

    // --- 履歴 ---

    fun loadHistory() {
        viewModelScope.launch { _sessions.value = history.list() }
    }

    fun deleteSession(id: String) {
        viewModelScope.launch {
            history.delete(id)
            _sessions.value = history.list()
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            history.clearAll()
            _sessions.value = emptyList()
        }
    }

    /**
     * 現在の会話を履歴へ保存する。値は同期的に読み取ってから書き込むので、
     * 直後にセッションがクリアされても取りこぼさない。空なら何もしない。
     * ID は開始時刻なので upsert により重複しない。
     */
    private fun persistCurrent() {
        val startedAt = SessionBus.currentStartedAt
        val partner = SessionBus.partnerTranscript.value
        val mine = SessionBus.myTranscript.value
        if (startedAt == 0L || (partner.isBlank() && mine.isBlank())) return
        val conv = SessionBus.conversation.value
        val session = ChatSession(
            id = startedAt.toString(),
            startedAt = startedAt,
            myLang = conv.myLang.code,
            partnerLang = conv.partnerLang.code,
            partnerText = partner,
            myText = mine,
        )
        viewModelScope.launch {
            history.upsert(session)
            _sessions.value = history.list()
        }
    }
}
