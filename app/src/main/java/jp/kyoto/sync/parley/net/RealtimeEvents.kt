package jp.kyoto.sync.parley.net

/**
 * /v1/realtime/translations エンドポイントのイベント種別。
 *
 * 注: translations エンドポイントは通常の Realtime API と一部イベント名が異なる
 * （"session." プレフィックスが付く）。公式ドキュメントに合わせている。
 */
object RealtimeEvent {
    // --- 送信（client → server） ---
    const val SESSION_UPDATE = "session.update"
    const val INPUT_AUDIO_APPEND = "session.input_audio_buffer.append"
    const val SESSION_CLOSE = "session.close"

    // --- 受信（server → client） ---
    const val OUTPUT_AUDIO_DELTA = "session.output_audio.delta"
    const val OUTPUT_TRANSCRIPT_DELTA = "session.output_transcript.delta"
    const val INPUT_TRANSCRIPT_DELTA = "session.input_transcript.delta"
    const val SESSION_CLOSED = "session.closed"
    const val ERROR = "error"
}
