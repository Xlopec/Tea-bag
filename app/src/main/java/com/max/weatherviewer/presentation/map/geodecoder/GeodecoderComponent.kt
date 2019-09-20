package com.max.weatherviewer.presentation.map.geodecoder

import android.location.Geocoder
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.di.fragmentScope
import com.max.weatherviewer.presentation.map.google.MapComponent
import com.oliynick.max.elm.core.component.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

sealed class Message

data class DecodeQuery(val query: String = "") : Message()

data class DecodeLocation(val location: Location) : Message()

data class Address(val address: String) : Message()

//

sealed class State

data class Preview(val address: String? = null) : State()

//

sealed class Command

data class DoDecodeQuery(val query: String = "") : Command()

data class DoDecodeLocation(val location: Location) : Command()

//

typealias GeodecoderComponent = (Flow<Message>) -> Flow<State>

fun geocoderModule(fragment: Fragment, scope: CoroutineScope) = Kodein.Module("geocoder") {

    bind<Geocoder>() with scoped(fragment.fragmentScope).singleton { Geocoder(instance()) }

    bind<Dependencies>() with scoped(fragment.fragmentScope).singleton { Dependencies(fragment, instance()) }

    bind<GeodecoderComponent>() with scoped(fragment.fragmentScope).singleton {

        suspend fun resolve(command: Command) = instance<Dependencies>().resolve(command)

        component(Preview(), ::resolve, ::update).withAndroidLogger("Geocoder").also { geodecoder ->
            scope.bind(instance<MapComponent>("map"), geodecoder) {
                flowOf(DecodeLocation(it.location))
            }
        }
    }
}

@VisibleForTesting
fun update(m: Message, s: State): UpdateWith<State, Command> {
    return when (m) {
        is DecodeQuery -> s command DoDecodeQuery(m.query)
        is DecodeLocation -> s command DoDecodeLocation(m.location)
        is Address -> Preview(m.address).noCommand()
    }
}

private data class Dependencies(val fragment: Fragment,
                                val geocoder: Geocoder)

private suspend fun Dependencies.resolve(command: Command): Set<Message> {
    return when (command) {
        is DoDecodeQuery -> effect { geocoder.fetchAddress(command.query) }
        is DoDecodeLocation -> effect { geocoder.fetchAddress(command.location) }
    }
}

private suspend fun Geocoder.fetchAddress(query: String): Address? {
    return coroutineScope {
        withContext(Dispatchers.IO) {
            getFromLocationName(query, 1)
        }.firstOrNull()?.locality?.let(::Address)
    }
}

private suspend fun Geocoder.fetchAddress(location: Location): Address? {
    //coroutineScope {
    val address =
/*withContext(Dispatchers.Main) { */

        getFromLocation(location.lat, location.lon, 1).firstOrNull()// }
            ?: return null

    return Address(listOfNotNull(address.adminArea,
                                 address.subAdminArea,
                                 address.locality).joinToString())
    // }
}
