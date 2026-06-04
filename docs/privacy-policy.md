# Parley プライバシーポリシー / Privacy Policy

最終更新日 / Last updated: 2026-06-04

---

## 日本語

### はじめに
本プライバシーポリシーは、OKADA, Tomoyuki（以下「開発者」）が提供する Android アプリ「Parley」（以下「本アプリ」）における情報の取り扱いについて説明します。

### 重要な前提：開発者はサーバーを持ちません
本アプリは開発者側のサーバーを一切持ちません。**開発者はあなたのデータを収集・受信・保存しません。** 音声の翻訳は、あなたが設定した OpenAI の API キーを用いて、あなたの端末から OpenAI のサービスへ直接送信して行われます。

### 取り扱う情報と利用目的
1. **マイク音声**：翻訳セッション中のみ、端末のマイクから音声を取得します。取得した音声はリアルタイムに翻訳のため OpenAI へ送信されます。本アプリは音声を端末や開発者のストレージに録音・保存しません。
2. **翻訳結果（文字起こし・テキスト）**：会話履歴として **あなたの端末内にのみ** 保存されます（後から見返すため）。外部には送信しません。アプリ内の削除機能、またはアンインストールで消去できます。
3. **OpenAI API キー（BYOK）**：あなたが入力した API キーは、Android Keystore により **端末内で暗号化** して保存されます。OpenAI への認証のためだけに使用し、開発者や OpenAI 以外の第三者へ送信することはありません。

### 第三者への提供（OpenAI）
翻訳処理のため、マイク音声は **OpenAI** に送信されます。OpenAI における取り扱いは、あなた自身の OpenAI アカウントおよび OpenAI の規約・プライバシーポリシーに従います。
- OpenAI プライバシーポリシー：https://openai.com/policies/privacy-policy
- OpenAI 利用ポリシー：https://openai.com/policies/usage-policies

執筆時点で OpenAI は、API 経由で送信されたデータを既定ではモデルの学習に使用せず、不正利用の監視等のため限定的な期間のみ保持するとしています。最新の条件は OpenAI のポリシーをご確認ください。

### データの保管場所・国際移転
- 端末内データ（API キー・会話履歴）：あなたの端末にのみ保存されます。
- OpenAI へ送信される音声：OpenAI は米国に所在するため、データは国外（米国）で処理されます。

### 使用する権限
- マイク（RECORD_AUDIO）：相手・自分の音声を翻訳するため。
- フォアグラウンドサービス／マイク（FOREGROUND_SERVICE, FOREGROUND_SERVICE_MICROPHONE）：画面オフ時も翻訳を継続するため。
- Bluetooth（BLUETOOTH_CONNECT）：イヤホン等の音声出力先を選択するため。
- 通知（POST_NOTIFICATIONS）：実行中であることを通知するため。
- インターネット（INTERNET）：OpenAI と通信するため。

### 広告・解析・トラッキング
本アプリは広告を表示せず、解析（アナリティクス）SDK やトラッキングを使用しません。

### セキュリティ
API キーは Android Keystore による暗号化で保存し、通信は TLS（暗号化）で行われます。ただし、インターネット通信に絶対的な安全はありません。

### お子様のプライバシー
本アプリは子供向けではなく、13歳未満（または居住地域で定める年齢未満）のお子様から意図的に情報を取得することはありません。

### あなたの選択・権利
- 会話履歴：アプリ内でいつでも削除できます。
- API キー：アプリ内で削除できます。
- すべての端末内データ：アプリのアンインストールで削除されます。

### 本ポリシーの変更
内容を変更する場合は、本ページの「最終更新日」を更新して掲示します。

### お問い合わせ
OKADA, Tomoyuki
メール：okada.tomoyuki@sync.kyoto.jp

---

## English

### Introduction
This Privacy Policy explains how the Android application "Parley" (the "App"), provided by OKADA, Tomoyuki (the "Developer"), handles information.

### Key principle: the Developer runs no server
The App has no developer-operated server. **The Developer does not collect, receive, or store your data.** Translation is performed by sending audio directly from your device to OpenAI, authenticated with the OpenAI API key that you provide.

### Information handled and purposes
1. **Microphone audio** — Captured only during an active translation session and streamed in real time to OpenAI for translation. The App does not record or store audio on the device or any developer storage.
2. **Translation results (transcripts/text)** — Stored **only on your device** as conversation history for your later review. Not transmitted externally. Removable via the in-app delete function or by uninstalling.
3. **OpenAI API key (BYOK)** — The key you enter is **encrypted and stored on your device** using the Android Keystore. It is used solely to authenticate your requests to OpenAI and is never sent to the Developer or any third party other than OpenAI.

### Sharing with third parties (OpenAI)
To perform translation, microphone audio is sent to **OpenAI**. OpenAI's handling is governed by your own OpenAI account and OpenAI's terms and privacy policy.
- OpenAI Privacy Policy: https://openai.com/policies/privacy-policy
- OpenAI Usage Policies: https://openai.com/policies/usage-policies

At the time of writing, OpenAI states that data submitted via its API is not used to train its models by default and is retained only for a limited period for abuse monitoring. Please refer to OpenAI's policies for current terms.

### Data location / international transfer
- On-device data (API key, conversation history): stored only on your device.
- Audio sent to OpenAI: OpenAI is located in the United States, so data is processed outside your country (in the U.S.).

### Permissions
- Microphone (RECORD_AUDIO): to translate speech.
- Foreground service / microphone (FOREGROUND_SERVICE, FOREGROUND_SERVICE_MICROPHONE): to keep translating with the screen off.
- Bluetooth (BLUETOOTH_CONNECT): to select the audio output device (e.g., earphones).
- Notifications (POST_NOTIFICATIONS): to indicate the service is running.
- Internet (INTERNET): to communicate with OpenAI.

### Ads, analytics, tracking
The App shows no ads and uses no analytics SDKs or tracking.

### Security
The API key is stored encrypted via the Android Keystore, and communication uses TLS. However, no method of internet transmission is ever completely secure.

### Children's privacy
The App is not directed to children and does not knowingly collect information from children under 13 (or the age defined by your jurisdiction).

### Your choices / rights
- Conversation history: delete anytime in the App.
- API key: delete in the App.
- All on-device data: removed by uninstalling the App.

### Changes to this policy
If we change this policy, we will update the "Last updated" date on this page.

### Contact
OKADA, Tomoyuki
Email: okada.tomoyuki@sync.kyoto.jp
