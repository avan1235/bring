package `in`.procyk.bring.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import `in`.procyk.bring.ui.components.AppScreen
import `in`.procyk.bring.vm.RecipiesViewModel

@Composable
internal fun RecipiesScreen(
    padding: PaddingValues,
    vm: RecipiesViewModel,
) = AppScreen("screen-recipies", padding) {
    Text("Recipies")
}
