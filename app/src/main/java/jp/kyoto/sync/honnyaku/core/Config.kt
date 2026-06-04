package jp.kyoto.sync.honnyaku.core

/**
 * アプリ全体の設定値。
 *
 * BYOK（Bring Your Own Key）方式のため、API キーはここには持たない。
 * 利用者が入力したキーを [ApiKeyStore] が端末内に暗号化保存する。
 */
object Config {

    /** OpenAI realtime translations WebSocket エンドポイント */
    const val OPENAI_REALTIME_TRANSLATIONS_WS =
        "wss://api.openai.com/v1/realtime/translations?model=gpt-realtime-translate"

    const val MODEL = "gpt-realtime-translate"

    /** API が要求する音声レート（入出力とも 24kHz PCM16 mono / little-endian） */
    const val API_SAMPLE_RATE = 24_000

    /** 端末のマイク録音レート。48kHz は広く対応 → 24kHz へダウンサンプルして送る。 */
    const val CAPTURE_SAMPLE_RATE = 48_000

    /** 1 チャンクの長さ(ms)。短いほど低遅延だがオーバーヘッド増。 */
    const val CHUNK_MS = 100

    /** API キーを取得するページ（設定画面のリンク用） */
    const val API_KEYS_URL = "https://platform.openai.com/api-keys"
}
