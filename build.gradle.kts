// トップレベル build ファイル。各モジュールへのプラグイン宣言のみ行う。
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
