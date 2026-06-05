# Parley — Google Play 掲載情報 / Store Listing

最終更新日 / Last updated: 2026-06-05

このドキュメントは Google Play Console に入力するための掲載文・回答案・手順をまとめたものです。
（実際の申告内容はご自身でご確認ください。）

---

## 1. 基本情報 / Basics

| 項目 | 値 |
|---|---|
| アプリ名 / App name | Parley |
| パッケージ名 / Package | `jp.kyoto.sync.parley` |
| 既定の言語 / Default language | 日本語 (ja-JP)（英語も追加推奨） |
| アプリ種別 | アプリ（ゲームではない） |
| 料金 | 無料（アプリ内課金なし。利用には各自の OpenAI API キーが必要） |
| カテゴリ / Category | ツール / Tools |
| メール連絡先 | okada.tomoyuki@sync.kyoto.jp |
| プライバシーポリシー URL | https://github.com/sync-kyoto-jp/parley/blob/main/docs/privacy-policy.md |

---

## 2. ストアの掲載文（日本語）

**簡単な説明（短い説明 / 最大 80 文字）**

> リアルタイム双方向の音声翻訳。相手の声はイヤホンへ、あなたの声はスピーカーへ訳して届けます。

**詳しい説明（最大 4000 文字）**

> Parley は、異なる言語を話す二人の会話をリアルタイムに翻訳する音声翻訳アプリです。OpenAI の gpt-realtime-translate を使い、相手の発話をあなたの言語に訳してイヤホンへ、あなたの発話を相手の言語に訳してスピーカーへ届けます。
>
> ■ 主な機能
> ・12言語の双方向リアルタイム翻訳（英語・フランス語・ドイツ語・ヒンディー語・インドネシア語・イタリア語・日本語・韓国語・ポルトガル語・ロシア語・スペイン語・ベトナム語）
> ・相手の声はイヤホンへ、あなたの声はスピーカーへ、と出力を自動で振り分け
> ・ハウリングを防ぐプッシュトゥトーク（押している間だけ話す）
> ・訳文に加えて原文も表示するバイリンガル字幕
> ・会話履歴の保存・閲覧・削除
> ・原文字幕用の文字起こしモデルを選択可能
>
> ■ サーバー不要（BYOK）
> 開発者のサーバーはありません。各利用者がご自身の OpenAI API キーを入力し、音声は端末から OpenAI へ直接送信されます。API キーは端末内に暗号化して保存されます。
>
> ■ ご利用に必要なもの
> ・gpt-realtime-translate を利用でき、支払い設定済みの OpenAI API キー（利用料金はご自身の OpenAI アカウントに課金されます）
> ・イヤホン（ノイズキャンセリング推奨）
>
> ■ プライバシー
> 開発者は利用者のデータを収集・保存しません。音声は翻訳のためにのみ OpenAI へ送信されます。広告・解析・トラッキングはありません。

---

## 3. Store listing (English)

**Short description (max 80 chars)**

> Real-time two-way speech translation. Their voice in your ear; yours aloud.

**Full description (max 4000 chars)**

> Parley translates a conversation between two people speaking different languages in real time. Powered by OpenAI's gpt-realtime-translate, it speaks the other person's words into your earphone in your language, and your words to the speaker in theirs.
>
> Features
> - Two-way real-time translation across 12 languages: English, French, German, Hindi, Indonesian, Italian, Japanese, Korean, Portuguese, Russian, Spanish, Vietnamese.
> - Earphone / speaker routing: the other person's speech goes to your earphone; your speech is spoken aloud for them.
> - Push-to-talk to avoid acoustic feedback.
> - Bilingual captions: the translation plus the original text.
> - Conversation history you can review and delete.
> - Selectable transcription model for the original-language captions.
>
> No server (BYOK)
> There is no developer server. You provide your own OpenAI API key, and audio is sent directly from your device to OpenAI. The key is stored encrypted on your device.
>
> Requirements
> - An OpenAI API key with access to gpt-realtime-translate and billing enabled (usage is billed to your own OpenAI account).
> - Earphones (noise-cancelling recommended).
>
> Privacy
> The developer does not collect or store your data. Audio is sent to OpenAI only for translation. No ads, analytics, or tracking.

---

## 4. データセーフティ フォーム回答 / Data safety

詳細な根拠は [docs/play-data-safety.md](play-data-safety.md) を参照。フォームの要点：

- データを収集または共有しますか？ → **はい**（音声を翻訳のため OpenAI へ送信）
- 転送中の暗号化 → **はい**（TLS）
- 削除リクエスト手段 → **あり**（端末内：アプリ内削除＋アンインストール）
- 申告するデータ種別 → **音声（録音）/ Voice or sound recordings**
  - 収集：はい ／ 共有：いいえ（OpenAI はサービスプロバイダ）／ 目的：アプリの機能 ／ 必須
- 解析・広告・トラッキング → **なし**

---

## 5. コンテンツのレーティング回答 / Content rating (IARC)

- アプリのカテゴリ：ユーティリティ / 生産性 / コミュニケーション / その他
- 暴力・性的表現・下品な表現・規制物質・ギャンブル・恐怖 → すべて **いいえ**
- ユーザー同士のオンライン交流／コンテンツ共有機能 → **いいえ**（その場の二者の発話を訳すのみ。アカウントやオンライン投稿機能なし）
- 位置情報の共有 → **いいえ**
- 想定レーティング → **全年齢対象 / Everyone (3+)**

## 6. その他のアプリのコンテンツ申告

- 広告（アプリに広告を含むか）→ **いいえ**
- 対象年齢 / Target audience → 13歳以上（子供向けではない）
- データセーフティ → 上記 §4
- 政府関連アプリ／金融商品 → いいえ
- ニュースアプリ → いいえ

---

## 7. アセット（製品版で必要・内部テストでは任意）

- アプリアイコン：512×512 PNG（32bit, アルファ付き）。Android Studio の Image Asset 等で書き出す。
- フィーチャーグラフィック：1024×500 PNG/JPG。
- スマートフォンのスクリーンショット：2〜8枚（例：メイン画面・言語選択・履歴・設定）。

---

## 8. 内部テスト公開の手順 / Internal testing

1. Play Console →「アプリを作成」：名前 Parley、既定言語、アプリ、無料、各宣言にチェック。
2. 左メニュー「アプリのコンテンツ」の必須タスクを完了：
   - プライバシーポリシー URL（§1）
   - 広告：なし
   - アプリのアクセス権（特別なログイン不要なら「すべての機能が制限なく利用可能」。ただし OpenAI API キーが必要な旨を補足説明欄に記載推奨）
   - コンテンツのレーティング（§5）
   - 対象年齢（§6）
   - データセーフティ（§4）
3. 「テスト」→「内部テスト」→「新しいリリースを作成」：
   - `app/build/outputs/bundle/release/app-release.aab` をアップロード
   - リリース名・リリースノートを入力 → 保存 → 審査 → 内部テストへ公開
4. 「テスター」タブで内部テスター（自分の Google アカウント等）のメールリストを作成 → 表示される opt-in URL を端末で開いてインストール。

> 注：内部テストでは §2・§7 のストア掲載文・グラフィックは必須ではありません（製品版で必要）。
