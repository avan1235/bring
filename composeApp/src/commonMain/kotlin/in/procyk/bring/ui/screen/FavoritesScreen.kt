package `in`.procyk.bring.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.favorite_list_elements
import bring.composeapp.generated.resources.favorite_shopping_lists
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.components.*
import `in`.procyk.bring.vm.FavoritesViewModel
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FavoritesScreen(
    vm: FavoritesViewModel
) = AppScreen("screen-favorites") {
    val favorites by vm.favoriteLists.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 24.dp),
    ) {
        stickyHeader(key = "input_header") {
            Text(
                text = stringResource(Res.string.favorite_list_elements),
                modifier = Modifier.padding(horizontal = 12.dp),
                style = BringAppTheme.typography.h4,
            )
        }
        item(key = "input") {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val newFavoriteElementName by vm.newFavoriteElementName.collectAsState()
                AddItemTextField(
                    value = newFavoriteElementName,
                    onValueChange = { vm.onNewFavoriteElementNameChange(it) },
                    onAdd = { vm.onFavoriteElementCreated() },
                    onDone = { vm.onFavoriteElementCreated() },
                    textFieldModifier = Modifier.testTag("text-field-add-favourite-element"),
                    buttonModifier = Modifier.testTag("button-add-favourite-element"),
                )
            }
        }
        item(key = "favorite_elements") {
            val favoriteElements by vm.favoriteElements.collectAsState()
            FlowRowItems(
                items = favoriteElements,
                modifier = Modifier.padding(12.dp),
                name = { it.name },
                id = { it.id },
                trailingIcon = { Icon(Icons.Outlined.Close) }
            ) { vm.onFavoriteElementRemoved(it.id) }
        }
        if (favorites.isNotEmpty()) {
            stickyHeader(key = "favorite_shopping_lists_header") {
                AnimatedVisibility(
                    modifier = Modifier
                        .padding(12.dp)
                        .animateContentSize(),
                    visible = favorites.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Text(
                        text = stringResource(Res.string.favorite_shopping_lists),
                        style = BringAppTheme.typography.h4,
                    )
                }
            }
        }
        items(favorites, key = { it.listId }) { favorite ->
            Row(
                modifier = Modifier
                    .clickable { vm.onNavigateToFavoriteList(favorite) }
                    .padding(top = 6.dp, bottom = 6.dp, end = 12.dp)
                    .fillMaxWidth()
                    .animateItem(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = favorite.listName,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .weight(1f),
                )
                IconButton(
                    variant = IconButtonVariant.PrimaryGhost,
                    onClick = { vm.onFavoriteListShared(favorite) },
                    modifier = Modifier
                        .compactButtonMinSize(),
                ) {
                    Icon(Icons.Default.IosShare)
                }
                IconButton(
                    variant = IconButtonVariant.PrimaryGhost,
                    onClick = { vm.onFavoriteListRemoved(favorite) },
                    modifier = Modifier
                        .compactButtonMinSize(),
                ) {
                    Icon(Icons.Default.Close)
                }
            }
        }
    }
}
