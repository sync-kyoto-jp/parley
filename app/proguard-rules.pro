# ---- OkHttp / Okio ----
-dontwarn okhttp3.**
-dontwarn okio.**
# OkHttp が参照する任意の TLS プロバイダ（未使用でも参照解決の警告を抑止）
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---- Jetpack ViewModel ----
# AndroidViewModel/ViewModel はデフォルトファクトリがリフレクションで
# (Application) コンストラクタを呼ぶため、コンストラクタを保持する。
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }
-keep class * extends androidx.lifecycle.AndroidViewModel { <init>(...); }

# ---- Kotlin coroutines ----
-dontwarn kotlinx.coroutines.**
