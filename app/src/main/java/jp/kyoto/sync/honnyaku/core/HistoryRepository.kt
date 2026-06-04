package jp.kyoto.sync.honnyaku.core

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** 1 セッション分の会話。partnerText=相手→自分言語の訳、myText=自分→相手言語の訳。 */
data class ChatSession(
    val id: String,
    val startedAt: Long,
    val myLang: String,
    val partnerLang: String,
    val partnerText: String,
    val myText: String,
)

/**
 * 会話履歴を端末内 JSON ファイル（filesDir/sessions.json）に保存する単純なリポジトリ。
 * 規模が小さいので 1 ファイルに全件を保持し、保存のたびに書き直す。
 */
class HistoryRepository(context: Context) {

    private val file = File(context.filesDir, "sessions.json")
    private val mutex = Mutex()

    suspend fun list(): List<ChatSession> = withContext(Dispatchers.IO) {
        mutex.withLock { readAll() }.sortedByDescending { it.startedAt }
    }

    /** 同一 ID があれば置き換え、なければ追加（冪等）。 */
    suspend fun upsert(session: ChatSession) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val merged = readAll().filter { it.id != session.id } + session
            writeAll(merged)
        }
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        mutex.withLock { writeAll(readAll().filter { it.id != id }) }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        mutex.withLock { writeAll(emptyList()) }
    }

    private fun readAll(): List<ChatSession> {
        if (!file.exists()) return emptyList()
        return runCatching {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                ChatSession(
                    id = o.getString("id"),
                    startedAt = o.getLong("startedAt"),
                    myLang = o.getString("myLang"),
                    partnerLang = o.getString("partnerLang"),
                    partnerText = o.optString("partnerText"),
                    myText = o.optString("myText"),
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun writeAll(sessions: List<ChatSession>) {
        val arr = JSONArray()
        sessions.forEach { s ->
            arr.put(
                JSONObject()
                    .put("id", s.id)
                    .put("startedAt", s.startedAt)
                    .put("myLang", s.myLang)
                    .put("partnerLang", s.partnerLang)
                    .put("partnerText", s.partnerText)
                    .put("myText", s.myText),
            )
        }
        file.writeText(arr.toString())
    }
}
