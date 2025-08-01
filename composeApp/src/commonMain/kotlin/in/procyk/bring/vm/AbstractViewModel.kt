package `in`.procyk.bring.vm

import androidx.compose.ui.platform.Clipboard
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import bring.composeapp.generated.resources.Res
import bring.composeapp.generated.resources.favorites
import bring.composeapp.generated.resources.settings
import `in`.procyk.bring.*
import `in`.procyk.bring.ComposeAppConfig.CLIENT_HOST
import `in`.procyk.bring.ComposeAppConfig.CLIENT_HTTP_PROTOCOL
import `in`.procyk.bring.ComposeAppConfig.CLIENT_PORT
import `in`.procyk.bring.ComposeAppConfig.CLIENT_WS_PROTOCOL
import `in`.procyk.bring.ui.components.snackbar.SnackbarHostState
import `in`.procyk.bring.vm.AbstractViewModel.Context
import io.github.xxfast.kstore.KStore
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.rpc.annotations.Rpc
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kotlin.uuid.Uuid


abstract class AbstractViewModel(
    val context: Context,
) : ViewModel() {
    class Context(
        val navController: NavHostController,
        val snackbarHostState: SnackbarHostState,
        val store: KStore<BringStore>,
        val clipboard: Clipboard,
        private val appScope: CoroutineScope,
    ) {
        val storeFlow: StateFlow<BringStore> =
            store.updates.filterNotNull()
                .stateIn(appScope, SharingStarted.Eagerly, BringStore.Default)

        private val _topBarText = MutableStateFlow(ComposeAppConfig.APP_NAME)
        val topBarText: StateFlow<String> = _topBarText.asStateFlow()

        val navBarTarget = navController.currentBackStackEntryFlow.map { entry ->
            when {
                entry.navigatesFrom<Screen.EditList>() -> NavBarTarget.Main
                entry.navigatesFrom<Screen.CreateList>() -> NavBarTarget.Main
                entry.navigatesFrom<Screen.Settings>() -> NavBarTarget.Settings
                entry.navigatesFrom<Screen.Favorites>() -> NavBarTarget.Favourites
                else -> NavBarTarget.Main
            }

        }.stateIn(
            appScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = 1_000),
            NavBarTarget.Main
        )

        fun onNavBarTargetSelected(target: NavBarTarget) {
            val current = navBarTarget.value
            when (target) {
                NavBarTarget.Favourites if target != current -> navigateFavourites()
                NavBarTarget.Settings if target != current -> navigateSettings()
                NavBarTarget.Main -> when {
                    navController.currentBackStackEntry.navigatesFrom<Screen.EditList>() ->
                        navigateCreateList(cleanLastListId = true)

                    target != current -> navigateList()
                    else -> {}
                }

                else -> {}
            }
        }

        fun showSnackbar(resource: StringResource) {
            appScope.launch {
                snackbarHostState.showSnackbar(getString(resource))
            }
        }

        fun navigateList() {
            appScope.launch {
                when (val lastListId = storeFlow.value.lastListId) {
                    null -> navigateCreateList(cleanLastListId = false)
                    else -> navigateEditList(lastListId, fetchSuggestions = false)
                }
            }
        }

        fun navigateFavourites() {
            appScope.launch {
                updateListLocationPresentation(null)
                _topBarText.value = getString(Res.string.favorites)
                withContext(Dispatchers.Main) {
                    navController.navigate(Screen.Favorites)
                }
            }
        }

        fun navigateSettings() {
            appScope.launch {
                updateListLocationPresentation(null)
                _topBarText.value = getString(Res.string.settings)
                withContext(Dispatchers.Main) {
                    navController.navigate(Screen.Settings)
                }
            }
        }

        fun navigateEditList(listId: String, fetchSuggestions: Boolean) {
            appScope.launch {
                updateListLocationPresentation(listId)
                store.update { it?.copy(lastListId = listId) }
                withContext(Dispatchers.Main) {
                    navController.navigate(Screen.EditList(listId, fetchSuggestions))
                }
            }
        }

        fun navigateEditList(listId: Uuid, fetchSuggestions: Boolean) =
            navigateEditList(listId.toHexDashString(), fetchSuggestions)

        fun navigateCreateList(cleanLastListId: Boolean) {
            appScope.launch {
                updateListLocationPresentation(null)
                if (cleanLastListId) {
                    store.update { it?.copy(lastListId = null) }
                }
                withContext(Dispatchers.Main) {
                    navController.navigate(Screen.CreateList)
                }
                _topBarText.value = ComposeAppConfig.APP_NAME
            }
        }

        fun updateListName(name: String) {
            _topBarText.value = name
        }
    }

    protected val httpClient = HttpClient {
        defaultRequest {
            url(scheme = CLIENT_HTTP_PROTOCOL, host = CLIENT_HOST, port = CLIENT_PORT)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    protected inline fun <@Rpc reified T : Any> durableRpcService(path: String): DurableRpcService<T> =
        DurableRpcService(viewModelScope, httpClient, path) {
            url(scheme = CLIENT_WS_PROTOCOL, host = CLIENT_HOST, port = CLIENT_PORT, path = path)
        }

    protected inline fun updateConfig(crossinline f: (BringStore) -> BringStore) {
        viewModelScope.launch {
            context.store.update { it?.let(f) }
        }
    }

    protected fun <T> Flow<T>.state(
        initialValue: T,
        started: SharingStarted = SharingStarted.WhileSubscribed(stopTimeoutMillis = 1_000),
    ): StateFlow<T> = stateIn(viewModelScope, started, initialValue)

    protected fun <T> Flow<T>.eagerState(
        initialValue: T,
    ): StateFlow<T> = state(initialValue, SharingStarted.Eagerly)

    protected val store: BringStore
        get() = storeFlow.value

    protected val storeFlow: StateFlow<BringStore> = context.storeFlow
}

internal inline fun <reified T : Screen> NavBackStackEntry?.navigatesFrom(): Boolean =
    this?.destination?.route?.split('/')?.getOrNull(0) == T::class.qualifiedName

enum class NavBarTarget {
    Main, Favourites, Settings;
}

internal expect fun updateListLocationPresentation(listId: String?)

internal expect suspend fun onShareList(listId: String, context: Context)
