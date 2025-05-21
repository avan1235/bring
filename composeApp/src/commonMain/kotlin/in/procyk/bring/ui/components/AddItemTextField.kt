package `in`.procyk.bring.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import `in`.procyk.bring.ui.components.textfield.OutlinedTextField

@Composable
internal fun RowScope.AddItemTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        modifier = modifier
            .weight(1f),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            autoCorrectEnabled = false,
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Text,
            showKeyboardOnFocus = true,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() },
        )
    )
    IconButton(
        onClick = { onAdd() },
        variant = IconButtonVariant.Primary,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
        )
    }
}
