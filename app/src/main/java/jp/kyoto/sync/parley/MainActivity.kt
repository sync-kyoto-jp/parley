package jp.kyoto.sync.parley

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import jp.kyoto.sync.parley.ui.ApiKeySetupScreen
import jp.kyoto.sync.parley.ui.HistoryScreen
import jp.kyoto.sync.parley.ui.TranslationScreen
import jp.kyoto.sync.parley.ui.TranslationViewModel
import jp.kyoto.sync.parley.ui.theme.ParleyTheme

class MainActivity : ComponentActivity() {

    private val vm: TranslationViewModel by viewModels()

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { /* 結果は UI 側で判断。ここでは特に処理しない。 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNeededPermissions()
        setContent {
            ParleyTheme {
                val hasKey by vm.hasApiKey.collectAsState()
                var screen by rememberSaveable { mutableStateOf("main") }

                when {
                    !hasKey -> ApiKeySetupScreen(vm = vm, canCancel = false, onDone = {})
                    screen == "settings" -> ApiKeySetupScreen(
                        vm = vm,
                        canCancel = true,
                        onDone = { screen = "main" },
                    )
                    screen == "history" -> HistoryScreen(vm = vm, onBack = { screen = "main" })
                    else -> TranslationScreen(
                        vm = vm,
                        onOpenSettings = { screen = "settings" },
                        onOpenHistory = { screen = "history" },
                    )
                }
            }
        }
    }

    private fun requestNeededPermissions() {
        val perms = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms += Manifest.permission.BLUETOOTH_CONNECT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms += Manifest.permission.POST_NOTIFICATIONS
        }
        permLauncher.launch(perms.toTypedArray())
    }
}
