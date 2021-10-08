package com.oliynick.max.reader.app

fun PlatformEnv(
    closeCommandsFlow: CloseCommandsSink
): PlatformEnv = object : PlatformEnv {

    override val closeCommands: CloseCommandsSink = closeCommandsFlow

}

/*actual fun Environment(
    platform: PlatformEnv
): Environment {

    return object : Environment,
        AppModule<Environment> by AppModule(platform),
        ArticlesModule<Environment> by ArticlesModule(),
        ArticleDetailsModule<Environment> by ArticleDetailsModule(),
        NewsApi<Environment> by NewsApi(),
        NewsApiEnv by NewsApiEnv(),
        LocalStorage by LocalStorage(platform),
        ArticleDetailsEnv by ArticleDetailsEnv(platform),
        ArticlesEnv by ArticlesEnv(platform),
        CoroutineScope by platform.scope {
    }
}*/

actual interface PlatformEnv {
    actual val closeCommands: CloseCommandsSink
}