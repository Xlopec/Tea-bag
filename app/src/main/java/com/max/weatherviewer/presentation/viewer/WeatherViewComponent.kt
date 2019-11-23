package com.max.weatherviewer.presentation.viewer

import com.oliynick.max.elm.core.component.Component


typealias WeatherComponent = Component<Message, State>

/*
fun <S> S.weatherModule(startLocation: Location): Kodein.Module where S : CoroutineScope,
                                                                      S : Fragment {

    return Kodein.Module("weatherModule") {

        bind<Fragment>() with scoped(scope).singleton { this@weatherModule as Fragment }

        bind<Dependencies>() with singleton { Dependencies(instance(), instance(), instance()) }

        bind<WeatherComponent>() with scoped(scope).singleton {

            suspend fun resolver(command: Command) = instance<Dependencies>().resolveEffect(command)

            val appDependencies = appDependencies(State.Loading(startLocation), ::resolver, ::update, Command.LoadWeather(startLocation)) {
                interceptor = androidLogger("WeatherViewer")
            }

            if (false &&  BuildConfig.DEBUG) {

                debugComponent(ComponentId("Weather viewer"), GsonConverter, appDependencies) {
                    serverSettings {
                        url = URL("http://10.0.2.2:8080")
                    }
                }
            } else {
                component(appDependencies)
            }
        }
    }
}
*/

/*private data class Dependencies(
        val locationModel: LocationModel,
        val weatherProvider: WeatherProvider
)*/
/*
private suspend fun Dependencies.resolveEffect(command: Command): Set<Message> {

    suspend fun resolve() = when (command) {
        is Command.LoadWeather -> command.effect { Message.WeatherLoaded(weatherProvider.fetchWeather(l)) }
        is Command.FeedLoaded -> command.effect { Message.WeatherLoaded(data) }
        is Command.SelectLocation -> command.sideEffect { fragment.navigateToMap(withSelectedLocation) }
    }

    return runCatching { resolve() }.getOrElse { th -> effect { Message.OpFuckup(th) } }
}*/

/*
private fun Fragment.navigateToMap(withStartLocation: Location?) {
    findNavController()
        .navigateDefaultAnimated(R.id.mapFragment,
                                 MapFragmentArgs(withStartLocation).toBundle())
}*/
