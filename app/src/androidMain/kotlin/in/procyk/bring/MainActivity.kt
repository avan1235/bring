package `in`.procyk.bring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import `in`.procyk.bring.vm.PlatformContext
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FileKit.init(this)
        val platformContext = PlatformContext(this)
        setContent {
            BringApp(platformContext)
        }
    }
}
