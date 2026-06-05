# Contributing to Parley

Thank you for your interest in contributing to Parley.

[English](#english) | [日本語](#日本語)

---

## English

### Prerequisites

- Android Studio (latest stable) with JDK 17
- An Android device or emulator running Android 8.0 (API 26) or later

### Building and testing

```sh
./gradlew :app:assembleDebug      # build the debug app
./gradlew :app:testDebugUnitTest  # run unit tests
```

### Submitting changes

1. Fork the repository and create a branch from `main`.
2. Keep each pull request focused on a single change.
3. Make sure the project builds and the unit tests pass.
4. Write a clear description of what changed and why. The repository provides a
   pull request template.

### Code style

- Follow the official Kotlin coding conventions.
- Match the style of the surrounding code.

### Reporting issues

- Use the issue templates (bug report / feature request).
- **Never include secrets** such as your OpenAI API key in issues, logs, or
  pull requests.

### Security

Please do not report security vulnerabilities through public issues. See
[SECURITY.md](SECURITY.md).

### License

By contributing, you agree that your contributions will be licensed under the
[Apache License 2.0](LICENSE).

---

## 日本語

### 前提

- Android Studio（最新安定版）＋ JDK 17
- Android 8.0 (API 26) 以降の実機またはエミュレータ

### ビルドとテスト

```sh
./gradlew :app:assembleDebug      # デバッグビルド
./gradlew :app:testDebugUnitTest  # ユニットテスト
```

### 変更の提出

1. リポジトリをフォークし、`main` からブランチを作成します。
2. 1 つのプルリクエストは 1 つの変更に絞ってください。
3. ビルドが通り、ユニットテストが成功することを確認してください。
4. 何を・なぜ変更したかを明確に記述してください（プルリクエストのテンプレートがあります）。

### コードスタイル

- 公式の Kotlin コーディング規約に従ってください。
- 周囲のコードのスタイルに合わせてください。

### 問題の報告

- Issue テンプレート（バグ報告 / 機能要望）を使用してください。
- OpenAI API キーなどの**秘密情報は絶対に含めないで**ください（Issue・ログ・PR いずれも）。

### セキュリティ

脆弱性は公開 Issue で報告しないでください。[SECURITY.md](SECURITY.md) を参照してください。

### ライセンス

コントリビュートすることで、あなたの貢献が [Apache License 2.0](LICENSE) の下でライセンスされることに同意したものとみなされます。
