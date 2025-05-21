package `in`.procyk.bring.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import bring.composeapp.generated.resources.*
import `in`.procyk.bring.ui.components.AppScreen
import `in`.procyk.bring.ui.components.Button
import `in`.procyk.bring.ui.components.ButtonVariant
import `in`.procyk.bring.ui.components.Text
import `in`.procyk.bring.ui.components.textfield.OutlinedTextField
import `in`.procyk.bring.vm.CreateListScreenViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CreateListScreen(
    vm: CreateListScreenViewModel,
) = AppScreen {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            val name by vm.name.collectAsState()
            OutlinedTextField(
                value = name,
                onValueChange = vm::onNameChange,
                label = { Text(stringResource(Res.string.create_list)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    autoCorrectEnabled = false,
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Text,
                    showKeyboardOnFocus = true,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { vm.onCreateShoppingList() },
                ),
                modifier = Modifier.widthIn(max = 720.dp),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = vm::onCreateShoppingList,
                variant = ButtonVariant.Primary,
            ) {
                Text(stringResource(Res.string.create))
            }

            Spacer(Modifier.height(64.dp))

            val listId by vm.listId.collectAsState()
            val isListIdValid by vm.isListIdValid.collectAsState()
            OutlinedTextField(
                value = listId,
                onValueChange = vm::onListIdChange,
                label = { Text(stringResource(Res.string.join_list)) },
                isError = !isListIdValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    autoCorrectEnabled = false,
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Text,
                    showKeyboardOnFocus = true,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { vm.onJoinShoppingList() },
                ),
                modifier = Modifier.widthIn(max = 720.dp),
            )
            Spacer(Modifier.height(16.dp))
            val isJoinShoppingListEnabled by vm.isJoinShoppingListEnabled.collectAsState()
            Button(
                onClick = vm::onJoinShoppingList,
                variant = ButtonVariant.PrimaryGhost,
                enabled = isJoinShoppingListEnabled,
            ) {
                Text(stringResource(Res.string.join))
            }
        }
    }
}
