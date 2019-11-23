package com.max.weatherviewer.presentation.map.google

import kotlinx.coroutines.flow.Flow

typealias MapComponent = (Flow<Message>) -> Flow<State>

/*fun <S> S.mapModule(preSelectedLocation: Location?): Kodein.Module where S : Fragment,
                                                                         S : CoroutineScope {
    return Kodein.Module("map") {

        bind<Gson>() with singleton { Gson() }

        bind<Dependencies>() with scoped(fragmentScope).singleton { Dependencies(this@mapModule) }

        bind<Interceptor<Message, State, Command>>() with singleton {
            androidLogger<Message, Command, State>("map") with { _, _, new, _ -> instance<Context>().persist(instance(), new) }
        }

        bind<MapComponent>("map") with scoped(fragmentScope).singleton {

            suspend fun resolve(command: Command) = instance<Dependencies>().resolve(command)

            suspend fun loader() = (preSelectedLocation?.let(::State) ?: instance<Context>().load(instance(), ::State)) to emptySet<Command>()

            val appDependencies = appDependencies(::loader, ::resolve, ::update) {
                interceptor = instance()
            }

            if (false && BuildConfig.DEBUG) {

                debugComponent(ComponentId("Google map"), GsonConverter, appDependencies) {
                    serverSettings {
                        url = URL("http://10.0.2.2:8080")
                    }
                }
            } else {
                component(appDependencies)
            }
        }
    }
}*/

/*private data class Dependencies(val fragment: Fragment)

private suspend fun Dependencies.resolve(command: Command): Set<Message> {
    return when (command) {
        is Command.SelectAndQuit -> command.sideEffect { fragment.navigateToWeatherViewer(location) }
    }
}*/
/*

private fun Fragment.navigateToWeatherViewer(location: Location) {
    findNavController()
        .navigateDefaultAnimated(R.id.weatherViewer, WeatherViewerFragmentArgs(location).toBundle(),
                                 navOptions())
}*/
