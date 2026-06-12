package `in`.procyk.bring.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bring.app.generated.resources.Res
import bring.app.generated.resources.scan
import `in`.procyk.bring.ui.components.AppScreen
import `in`.procyk.bring.ui.components.Icon
import `in`.procyk.bring.ui.components.IconButton
import `in`.procyk.bring.ui.components.Text
import `in`.procyk.bring.vm.RecipiesViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RecipiesScreen(
    padding: PaddingValues,
    vm: RecipiesViewModel,
) = AppScreen("screen-recipies", padding) {
    IconButton(
        onClick = vm::extractFromImages,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Icon(Icons.Outlined.QrCodeScanner)
            Text(stringResource(Res.string.scan))
        }
    }
}
