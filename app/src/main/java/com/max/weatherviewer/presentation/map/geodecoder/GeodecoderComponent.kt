package com.max.weatherviewer.presentation.map.geodecoder

import com.max.weatherviewer.BuildConfig
import com.max.weatherviewer.api.weather.Location
import com.oliynick.max.elm.core.actor.component
import com.oliynick.max.elm.core.component.*
import com.oliynick.max.elm.time.travel.debugComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import protocol.ComponentId
import java.net.URL
import com.max.weatherviewer.presentation.map.google.State as MapState

typealias GeodecoderComponent = Component<Message, State>

/*
fun <S> S.geocoderModule(): Kodein.Module where S : CoroutineScope,
                                                S : Fragment {

    return Kodein.Module("geocoder") {

        bind<Geocoder>() with scoped(fragmentScope).singleton { Geocoder(instance()) }

        bind<Dependencies>() with scoped(fragmentScope).singleton { Dependencies(this@geocoderModule, instance()) }

        bind<GeodecoderComponent>() with scoped(fragmentScope).singleton {

            suspend fun resolve(command: Command) = instance<Dependencies>().resolve(command)

            val appDependencies = appDependencies(Preview(), ::resolve, ::update) {
                interceptor = androidLogger("Geocoder")
            }

            if (false &&  BuildConfig.DEBUG) {

                debugComponent(ComponentId("Geocoder"), GsonConverter, appDependencies) {
                    serverSettings {
                        url = URL("http://10.0.2.2:8080")
                    }
                }
            } else {
                component(appDependencies)
            }.also { geodecoder -> bind(instance<MapComponent>("map"), geodecoder, ::mapStateToMessages) }
        }
    }
}
*/

private fun mapStateToMessages(state: MapState): Flow<Message> = flowOf(DecodeLocation(state.location))

/*
private data class Dependencies(val fragment: Fragment,
                                val geocoder: Geocoder)

private suspend fun Dependencies.resolve(command: Command): Set<Message> {

    suspend fun resolve(): Set<Message> {
        return when (command) {
            is DoDecodeLocation -> effect { geocoder.fetchAddress(command.location) }
        }
    }

    return runCatching { resolve() }.getOrElse { emptySet() }
}

private suspend fun Geocoder.fetchAddress(location: Location): Address? {
    return withContext(Dispatchers.IO) {

        getFromLocation(location.lat, location.lon, 1)
            .firstOrNull()
            ?.let {
                Address(listOfNotNull(it.adminArea,
                                      it.subAdminArea,
                                      it.locality).joinToString())
            }
    }
}
*/
