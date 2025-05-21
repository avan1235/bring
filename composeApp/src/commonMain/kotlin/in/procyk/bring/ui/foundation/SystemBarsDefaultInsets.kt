
package `in`.procyk.bring.ui.foundation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable

val WindowInsets.Companion.systemBarsForVisualComponents: WindowInsets
    @Composable
    get() = statusBars
