package io.github.xlopec.reader.app.feature.article.details

import android.app.Application
import android.content.Intent
import androidx.core.net.toUri
import io.github.xlopec.tea.data.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class BrowserLauncherImpl(
    private val application: Application,
) : BrowserLauncher {
    override suspend fun launch(url: Url) = withContext(Dispatchers.Main) {
        val intent = Intent(Intent.ACTION_VIEW, url.toString().toUri())
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }

        if (intent.resolveActivity(application.packageManager) != null) {
            application.startActivity(intent)
        }
    }
}
