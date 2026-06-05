package jp.kyoto.sync.parley.net

import android.util.Base64
import android.util.Log
import jp.kyoto.sync.parley.core.Config
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

/**
 * OpenAI translations への 1 方向の翻訳セッション（WebSocket）。
 *
 * BYOK 方式のため、利用者の API キーを Bearer に直接使って接続する
 * （端末 → OpenAI の TLS 直結。中継サーバなし）。
 *
 * 1 セッション = ソース自動検出 → 1 ターゲット言語。
 * 双方向会話では incoming / outgoing の 2 本を生成して使う。
 */
class TranslationSession(
    private val apiKey: String,
    private val targetLang: String,
    private val transcriptionModel: String,
    private val http: OkHttpClient,
    private val listener: Listener,
) {
    interface Listener {
        /** 訳された音声チャンク（24kHz PCM16 mono / little-endian） */
        fun onTranslatedAudio(pcm16: ByteArray)

        /** 訳文（ターゲット言語）の字幕デルタ */
        fun onOutputTranscript(text: String)

        /** 原文（ソース言語）の字幕デルタ */
        fun onInputTranscript(text: String) {}

        fun onOpen() {}
        fun onClosed() {}
        fun onError(t: Throwable) {}
    }

    private var ws: WebSocket? = null

    fun connect() {
        val req = Request.Builder()
            .url(Config.OPENAI_REALTIME_TRANSLATIONS_WS)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        ws = http.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                sendSessionUpdate()
                listener.onOpen()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleEvent(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure (target=$targetLang)", t)
                listener.onError(t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                listener.onClosed()
            }
        })
    }

    /** 接続直後に出力言語・文字起こし・ノイズ低減を設定する。 */
    private fun sendSessionUpdate() {
        val session = JSONObject().apply {
            put(
                "audio",
                JSONObject().apply {
                    put(
                        "input",
                        JSONObject().apply {
                            put("transcription", JSONObject().put("model", transcriptionModel))
                            // 相手が少し離れている場合は "far_field" も検証する。
                            put("noise_reduction", JSONObject().put("type", "near_field"))
                        },
                    )
                    put("output", JSONObject().put("language", targetLang))
                },
            )
        }
        val msg = JSONObject()
            .put("type", RealtimeEvent.SESSION_UPDATE)
            .put("session", session)
        ws?.send(msg.toString())
    }

    /** 24kHz PCM16 mono(LE) のフレームを送信する。 */
    fun sendAudio(pcm16: ByteArray) {
        val b64 = Base64.encodeToString(pcm16, Base64.NO_WRAP)
        val msg = JSONObject()
            .put("type", RealtimeEvent.INPUT_AUDIO_APPEND)
            .put("audio", b64)
        ws?.send(msg.toString())
    }

    private fun handleEvent(text: String) {
        val json = runCatching { JSONObject(text) }.getOrNull() ?: return
        when (json.optString("type")) {
            RealtimeEvent.OUTPUT_AUDIO_DELTA -> {
                val b64 = json.optString("delta")
                if (b64.isNotEmpty()) {
                    listener.onTranslatedAudio(Base64.decode(b64, Base64.NO_WRAP))
                }
            }

            RealtimeEvent.OUTPUT_TRANSCRIPT_DELTA ->
                listener.onOutputTranscript(json.optString("delta"))

            RealtimeEvent.INPUT_TRANSCRIPT_DELTA ->
                listener.onInputTranscript(json.optString("delta"))

            RealtimeEvent.SESSION_CLOSED ->
                listener.onClosed()

            RealtimeEvent.ERROR ->
                listener.onError(
                    RuntimeException(json.optJSONObject("error")?.toString() ?: text),
                )
        }
    }

    /** セッションを閉じる。close イベントを送ってから WS を閉じる。 */
    fun close() {
        runCatching {
            ws?.send(JSONObject().put("type", RealtimeEvent.SESSION_CLOSE).toString())
        }
        runCatching { ws?.close(1000, "client closing") }
        ws = null
    }

    companion object {
        private const val TAG = "TranslationSession"
    }
}
