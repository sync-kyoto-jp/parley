# Security Policy

[English](#english) | [日本語](#日本語)

---

## English

### Reporting a vulnerability

If you discover a security vulnerability in Parley, please report it privately
by email to **okada.tomoyuki@sync.kyoto.jp**. Do not open a public issue for
security reports.

Please include:

- A description of the vulnerability and its impact
- Steps to reproduce, if possible

We will make a best effort to acknowledge your report and address valid issues.

### A note on API keys (BYOK)

Parley uses your own OpenAI API key, stored encrypted on your device. Never
share your API key and never include it in bug reports or logs. If you believe
your key has been exposed, revoke it in the OpenAI dashboard.

---

## 日本語

### 脆弱性の報告

Parley にセキュリティ上の脆弱性を発見した場合は、公開 Issue ではなく
**okada.tomoyuki@sync.kyoto.jp** 宛にメールで非公開で報告してください。

以下を含めてください：

- 脆弱性の内容と影響
- 可能であれば再現手順

いただいた報告には可能な範囲で確認・対応します。

### API キーについて（BYOK）

Parley は利用者自身の OpenAI API キーを端末内に暗号化して使用します。API キーは
決して共有せず、バグ報告やログにも含めないでください。漏洩が疑われる場合は OpenAI
のダッシュボードでキーを無効化してください。
