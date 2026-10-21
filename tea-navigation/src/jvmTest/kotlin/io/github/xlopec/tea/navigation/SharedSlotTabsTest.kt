package io.github.xlopec.tea.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

// Synthetic version of the reader's home screen: every tab renders in one shared
// slot (so the bottom bar survives a tab switch), and each tab lists articles keyed
// by URL with a pagination effect on the last item.
@OptIn(ExperimentalTestApi::class)
class SharedSlotTabsTest {

    @Test
    fun tab_switch_keeps_shared_chrome_and_runs_the_new_tabs_item_effects() = backTest {
        var currentStack by mutableStateOf(stack("feed"))
        var chromeCompositions = 0
        val loadNextRequests = mutableListOf<String>()
        set {
            Box(Modifier.size(400.dp)) {
                PredictiveBackContainer(
                    stack = currentStack,
                    onBack = {},
                    modifier = Modifier.fillMaxSize(),
                    contentKey = { "tabs" },
                ) { tab ->
                    DisposableEffect(Unit) {
                        chromeCompositions++
                        onDispose { }
                    }
                    key(tab.id) {
                        Articles(tab = tab.id, urls = SameArticleInEveryTab) { loadNextRequests += it }
                    }
                }
            }
        }
        settle()
        assertEquals(listOf("feed"), loadNextRequests)

        currentStack = stack("trending")
        settle()

        assertEquals(1, chromeCompositions, "the shared chrome must survive a tab switch")
        assertEquals(listOf("feed", "trending"), loadNextRequests, "the new tab must request its own next page")
    }
}

private val SameArticleInEveryTab = listOf("https://example.com/a")

@Composable
private fun Articles(
    tab: String,
    urls: List<String>,
    onLoadNext: (tab: String) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize()) {
        itemsIndexed(urls, key = { _, url -> url }) { index, _ ->
            LaunchedEffect(index == urls.lastIndex) {
                if (index == urls.lastIndex) onLoadNext(tab)
            }
        }
    }
}
