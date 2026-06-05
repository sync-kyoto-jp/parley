# Parley

Parley is an Android application for real-time, two-way speech translation, powered by OpenAI's `gpt-realtime-translate` model.

[English](#english) | [日本語](#日本語)

---

## English

Parley translates a conversation between two people speaking different languages in real time. It runs without any developer-operated server: each user supplies their own OpenAI API key (BYOK), and audio is streamed directly from the device to OpenAI over TLS.

### Features

- **Real-time two-way translation** across 12 languages: English, French, German, Hindi, Indonesian, Italian, Japanese, Korean, Portuguese, Russian, Spanish, and Vietnamese.
- **Earphone / speaker routing** — the other person's speech is translated into your earphone, while your speech is translated through the speaker for them to hear.
- **Push-to-talk** half-duplex operation to avoid acoustic feedback.
- **Bilingual live captions** — the translated text, with the original-language transcript shown for the incoming side.
- **Conversation history** stored on the device, with review and deletion.
- **Selectable transcription model** for the original-language captions.
- **On-device encrypted key storage** using the Android Keystore (AES-GCM).
- **Japanese and English user interface.**

### Requirements

- Android 8.0 (API 26) or later.
- An OpenAI API key with access to `gpt-realtime-translate` and billing enabled. Usage is billed to your own OpenAI account.
- Earphones recommended (noise-cancelling for the best experience).

### Building

1. Open the project in Android Studio (JDK 17) and let Gradle sync.
2. Build and run on a device or emulator.

From the command line: `./gradlew :app:assembleDebug`

### Usage

1. On first launch, enter your OpenAI API key. It is stored encrypted on the device.
2. Grant the microphone permission and connect your earphones.
3. Choose your language and the other person's language, then tap Start.
4. The other person's speech is translated into your earphone. Hold the microphone button to speak; your speech is translated through the speaker.

### Privacy & Security

- The developer operates no server and does not collect, receive, or store your data.
- Audio is sent directly to OpenAI over TLS, for translation only.
- The API key is encrypted on the device with the Android Keystore and is never sent to anyone other than OpenAI.
- Conversation history is stored only on the device and is removed on uninstall.
- No advertising, analytics, or tracking.

See the [Privacy Policy](docs/privacy-policy.md) for details.

### License

Parley is licensed under the Apache License 2.0. See [LICENSE](LICENSE).

Copyright 2026 OKADA, Tomoyuki.

---

## 日本語

Parley は、異なる言語を話す二者の会話をリアルタイムに翻訳する Android アプリケーションです。開発者のサーバーを一切持たず、各利用者が自分の OpenAI API キーを入力し（BYOK）、音声は端末から OpenAI へ TLS で直接送信されます。

### 機能

- **12言語**（英語・フランス語・ドイツ語・ヒンディー語・インドネシア語・イタリア語・日本語・韓国語・ポルトガル語・ロシア語・スペイン語・ベトナム語）の**双方向リアルタイム翻訳**。
- **イヤホン／スピーカーの出し分け** — 相手の発話は自分のイヤホンへ、自分の発話は相手に聞かせるためスピーカーへ翻訳します。
- 音響フィードバックを避ける**プッシュトゥトーク**（半二重）動作。
- **バイリンガル字幕** — 訳文に加え、相手側は原文も表示します。
- 端末内に保存される**会話履歴**（閲覧・削除が可能）。
- 原文字幕用の**文字起こしモデルの選択**。
- Android Keystore による **API キーの端末内暗号化保存**（AES-GCM）。
- **日本語・英語の UI**。

### 動作環境

- Android 8.0 (API 26) 以降。
- `gpt-realtime-translate` を利用でき、支払い設定済みの OpenAI API キー。利用料金は各自の OpenAI アカウントに課金されます。
- イヤホン推奨（ノイズキャンセリング推奨）。

### ビルド

1. Android Studio（JDK 17）でプロジェクトを開き、Gradle を同期します。
2. 実機またはエミュレータでビルド・実行します。

コマンドラインの場合: `./gradlew :app:assembleDebug`

### 使い方

1. 初回起動時に OpenAI API キーを入力します。キーは端末内に暗号化保存されます。
2. マイク権限を許可し、イヤホンを接続します。
3. 自分の言語と相手の言語を選び、「開始」をタップします。
4. 相手の声は自分のイヤホンに訳されます。マイクボタンを押している間は、自分の声がスピーカーから相手の言語で流れます。

### プライバシーとセキュリティ

- 開発者はサーバーを持たず、利用者のデータを収集・受信・保存しません。
- 音声は翻訳のためにのみ、OpenAI へ TLS で直接送信されます。
- API キーは Android Keystore で端末内に暗号化保存し、OpenAI 以外へは送信しません。
- 会話履歴は端末内にのみ保存され、アンインストールで消去されます。
- 広告・解析・トラッキングはありません。

詳細は[プライバシーポリシー](docs/privacy-policy.md)を参照してください。

### ライセンス

Parley は Apache License 2.0 で提供されます。[LICENSE](LICENSE) を参照してください。

Copyright 2026 OKADA, Tomoyuki.
