package com.max.reader.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import com.max.reader.BuildConfig
import com.max.reader.app.ui.misc.LocalLogCompositions
import com.max.reader.app.ui.screens.article.ArticleDetailsScreen
import com.max.reader.app.ui.screens.home.HomeScreen
import com.max.reader.app.ui.screens.suggest.SuggestScreen
import com.max.reader.app.ui.theme.AppTheme
import com.oliynick.max.reader.app.*
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsState
import com.oliynick.max.reader.app.feature.article.list.ArticlesState
import com.oliynick.max.reader.app.feature.navigation.Pop
import com.oliynick.max.reader.app.feature.navigation.currentTab
import com.oliynick.max.reader.app.feature.settings.SettingsScreen
import com.oliynick.max.reader.app.feature.suggest.SuggestState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun AppView(
    component: (Flow<Message>) -> Flow<AppState>
) {
    val messages = remember { MutableSharedFlow<Message>() }
    val stateFlow = remember { component(messages) }
    val appState = stateFlow.collectAsNullableState(context = Dispatchers.Main).value ?: return
    val scope = rememberCoroutineScope()
    val messageHandler = remember { scope.dispatcher(messages) }

    AppView(appState, messageHandler)
}

@Composable
fun AppView(
    appState: AppState,
    onMessage: (Message) -> Unit,
) {
    AppTheme(
        isDarkModeEnabled = appState.settings.appDarkModeEnabled
    ) {

        BackHandler {
            onMessage(Pop)
        }

        CompositionLocalProvider(LocalLogCompositions provides BuildConfig.DEBUG) {
            when (val screen = appState.screen) {
                is FullScreen -> FullScreen(screen, onMessage)
                is TabScreen -> TabScreen(appState, screen, onMessage)
                is NestedScreen -> TabScreen(appState, appState.currentTab, onMessage) {
                    TODO("Not implemented yet")
                }
            }
        }
    }
}

@Composable
private fun TabScreen(
    appState: AppState,
    screen: TabScreen,
    onMessage: (Message) -> Unit,
    content: (@Composable (innerPadding: PaddingValues) -> Unit)? = null
) {
    when (screen) {
        is ArticlesState -> HomeScreen(screen, onMessage, content)
        is SettingsScreen -> HomeScreen(appState, onMessage)
        else -> error("unhandled branch $screen")
    }
}

@Composable
private fun FullScreen(
    screen: FullScreen,
    onMessage: (Message) -> Unit,
) {
    when (screen) {
        is ArticleDetailsState -> ArticleDetailsScreen(screen, onMessage)
        is SuggestState -> SuggestScreen(screen, onMessage)
    }
}

private fun CoroutineScope.dispatcher(
    messages: FlowCollector<Message>,
): (Message) -> Unit =
    { message -> launch { messages.emit(message) } }

@Composable
private fun <T : R, R> Flow<T>.collectAsNullableState(
    context: CoroutineContext = EmptyCoroutineContext,
): State<R?> = collectAsState(context = context, initial = null)
