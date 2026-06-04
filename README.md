# Parley 🗣️⚡

> 話す言語が違っても、すぐ通じる。

OpenAI の `gpt-realtime-translate` を使った、**リアルタイム同時通訳 Android アプリ**です（ドラえもんの「ほんやくコンニャク」がコンセプト）。

- 相手の声（例: 英語）→ 自分の言語（日本語）に訳して**イヤホン**へ
- 自分の声（日本語）→ 相手の言語（英語）に訳して**スピーカー**へ
- まずは **英日 / 日英** の双方向に対応（言語方向は入れ替え可能）
- **BYOK（Bring Your Own Key）**: 各自が自分の OpenAI API キーを入力。**サーバー不要**。

> ⚠️ これは「動く土台（scaffold）」です。実機での音声チューニングや、下記「要検証ポイント」の確認が必要です。Android Studio 上ではまだコンパイル未確認なので、依存バージョン等は適宜更新してください。

---

## なぜサーバーが要らないのか（BYOK）

利用者が自分の API キーを入力し、それを**端末内に暗号化保存**（Android Keystore / AES-GCM）。
音声は **端末 → OpenAI に TLS で直結**します。中継サーバも、開発者が鍵を持つ必要もありません。

```
 [相手(英語)] ))) 🎤本体マイク ──24kHz PCM16──┐
                                              │ WebSocket (Bearer: 利用者のAPIキー)
                                      ┌───────▼─────────┐
 [Android: Parley] ──────────────────▶│  OpenAI         │
   端末内に暗号化保存したキーで直接接続 │  /v1/realtime/   │
                  ◀──────────────────│  translations   │
        🎧イヤホン(翻訳音声のみ) ◀──── └─────────────────┘
              ▲  (+ 字幕: transcript.delta)
        [ユーザー(日本語で聞く)]
```

- 翻訳は **1 セッション = 1 方向**。双方向のため `incoming`（相手→自分）と `outgoing`（自分→相手）の 2 セッションを張る。
- ハウリング回避のため、まずは**プッシュトゥトーク（半二重）**。マイク音声は「いま有効な方向」のセッションにだけ流す。

---

## 構成

```
Parley/ (フォルダ名は ホンニャクコンニャク のまま)
├─ app/                       … Android アプリ (Kotlin + Jetpack Compose)
│  └─ src/main/java/jp/kyoto/sync/honnyaku/   ← 内部パッケージ名は honnyaku のまま（実害なし）
│     ├─ MainActivity.kt              キー未設定なら設定画面、設定済みならメイン画面
│     ├─ core/
│     │  ├─ Config.kt                 エンドポイント・音声定数
│     │  ├─ Language.kt               Lang / Conversation
│     │  ├─ SessionBus.kt             Service↔UI を繋ぐ状態
│     │  └─ ApiKeyStore.kt            ★ APIキーを Keystore で暗号化保存
│     ├─ net/
│     │  ├─ TranslationSession.kt     OpenAI translations への WebSocket（生キーで接続）
│     │  └─ RealtimeEvents.kt         イベント種別定義
│     ├─ audio/
│     │  ├─ AudioCapturer.kt          マイク録音(48k→24k)
│     │  ├─ AudioPlayer.kt            再生(イヤホン/スピーカー切替)
│     │  └─ Resampler.kt              PCM16 変換
│     ├─ service/
│     │  └─ TranslationService.kt     ★ 双方向通訳のフォアグラウンドサービス
│     └─ ui/
│        ├─ TranslationScreen.kt      メイン画面（チャット風キャプション＋PTTマイク）
│        ├─ ApiKeySetupScreen.kt      APIキー入力/設定
│        ├─ TranslationViewModel.kt
│        └─ theme/                    ブランド配色（インディゴ→シアン、ダーク基調）
└─ （token-server は廃止しました）
```

---

## セットアップ

### 必要なもの
- Android Studio（最新版推奨）/ JDK 17
- Android 8.0 (API 26) 以上の端末またはエミュレータ
- **OpenAI の API キー**（`gpt-realtime-translate` が使える Tier＋支払い設定）
- **ノイズキャンセリング(ANC)イヤホン推奨**（理由は「ノイズ対策」）

### 手順
1. このフォルダを Android Studio で開く（Gradle 同期）
2. 実機 or エミュレータで Run
3. 初回起動時に **API キーを入力**（端末内に暗号化保存される）
4. マイク等の権限を許可
5. **イヤホンを接続**して「開始」

> 💡 **Gradle wrapper について**: バイナリの `gradle/wrapper/gradle-wrapper.jar` は含めていません。Android Studio で開けば自動補完されます。CLI で使う場合は `gradle wrapper --gradle-version 8.9` を一度実行してください。

### 使い方
- **聞く**: 「開始」後、何もしなければ相手の声がイヤホンに訳されて流れ、画面にも字幕。
- **話す**: 下部の大きなマイクボタンを**押している間**だけ、自分の声がスピーカーから相手言語で流れる。離すと聞き取りに戻る。
- **方向入替**: 停止中に上部の入替ボタンで 自分↔相手 の言語を交換（英日 ⇄ 日英）。
- **設定**: 右上の歯車から API キーを変更/削除。

---

## デザイン
- ブランドカラー: インディゴ `#6D5DF6` → スカイ `#4DA3FF` → シアン `#22D3EE` のグラデーション
- ダーク基調のモダン UI、チャット風の双方向キャプション、ホールド・トゥ・トークの大型マイク（発話中はパルス演出）
- アダプティブアイコン（グラデーション背景＋吹き出し＋音声波）＋ Android 13 のテーマアイコン(monochrome)対応

---

## ノイズ対策（「相手の生声がノイズにならない」について）
アプリは「空気を伝わって直接耳に届く相手の生声」は消せません（ANC の役割）。本アプリは以下で体験を最大化します:
1. **イヤホンには翻訳音声だけを流す**（原音は混ぜない）← 実装済み
2. **ANC イヤホン推奨**で生声を物理的に減衰
3. **低遅延化**（有線イヤホン推奨。Bluetooth は 100–200ms 程度の遅延）
4. **字幕併用**で聞き取りの保険

---

## セキュリティ
- API キーは **Android Keystore** 由来の AES-GCM 鍵で暗号化して保存（平文は保存しない）。
- キーは **端末 → OpenAI へ TLS 直送**のみ。ログ出力や外部送信はしない。
- アンインストールでキーは消える。心配なら OpenAI 側で**利用上限(budget)設定**＋**専用キー**を推奨。

## 料金の目安
`gpt-realtime-translate` は **$0.034 / 分**（音声の実時間、各自の OpenAI アカウントに従量課金）。双方向は 2 セッションぶん。
- 双方向で 1 日 30 分 × 30 日 ≒ 1,800 分 ≒ 約 $61/月（無音時にセッションを閉じれば削減可）

---

## 要検証ポイント（Phase 0 スパイクで先に潰す）
1. **WebSocket で生 API キー接続が通るか**（BYOK 前提。まず PC で `translations` WS に繋ぎ、英→日音声が返るか確認するのが安全）
2. **イベント名/フィールド名**（`session.output_audio.delta` の `delta` など）が実APIと一致するか
3. **24kHz 録音とリサンプル品質**（簡易リサンプラ。必要なら Oboe / ポリフェーズ FIR へ）
4. **Bluetooth 遅延・出力ルーティング**（`preferredDevice` の機種依存）
5. **双方向のハウリング**（PTT で回避。将来 AEC で全二重）
6. **near_field / far_field**（相手が離れている場合）

## 今後の発展
- 全二重（AEC + VAD で PTT 不要に）/ 会話履歴・字幕ログ / 無音検出によるコスト最適化 / iOS 対応

---

## 注意
学習・実験用の雛形です。API キーは各自の責任で管理してください。
