@file:UseSerializers(URLSerializer::class, LSerializer::class)

package com.max.weatherviewer.app

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.WritableTypeId
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import java.util.*
import kotlin.collections.ArrayList

typealias ScreenId = UUID

@Serializer(forClass = PersistentList::class)
object URLSerializer : KSerializer<PersistentList<*>> {

    override fun deserialize(decoder: Decoder): PersistentList<*> {
        return TODO()
    }

    @ImplicitReflectionSerializer
    override fun serialize(
        encoder: Encoder,
        obj: PersistentList<*>
    ) {
        encoder.encode(obj.toList())
    }

    override val descriptor: SerialDescriptor = StringDescriptor.withName("Fruit")

}

@Serializer(forClass = List::class)
object LSerializer : KSerializer<List<*>> {

    override fun deserialize(decoder: Decoder): PersistentList<*> {
        return TODO()
    }

    @ImplicitReflectionSerializer
    override fun serialize(
        encoder: Encoder,
        obj: List<*>
    ) {
        encoder.encode(obj)
    }

    override val descriptor: SerialDescriptor = StringDescriptor.withName("Fruit")

}

@Serializable
abstract class Screen {
    abstract val id: ScreenId
}

object PersistentListSerializer : JsonSerializer<PersistentList<*>>() {

    override fun serializeWithType(
        value: PersistentList<*>,
        gen: JsonGenerator,
        serializers: SerializerProvider,
        typeSer: TypeSerializer
    ) {

        with(gen) {
            val id = WritableTypeId("Persis", PersistentList::class.java, JsonToken.START_ARRAY).also {
                it.include = WritableTypeId.Inclusion.PAYLOAD_PROPERTY
                it.id = "kotlinx.collections.immutable.PersistentList"
            }
            writeTypePrefix(id)

            serialize(value, gen, serializers)

            writeTypeSuffix(id)

        }

       // super.serializeWithType(value, gen, serializers, typeSer)
    }

    override fun serialize(
        value: PersistentList<*>,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        with(gen) {

            /*  val typeId: WritableTypeId = typeSerializer.typeId(value, START_OBJECT)
              typeSer.writeTypePrefix(gen, typeId)
  */
            writeStartArray()

            for (v in value) {
                serializers.defaultSerializeValue(v, gen)
            }

            writeEndArray()
        }
    }

}

object PersistentListDeserializer : JsonDeserializer<PersistentList<*>>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PersistentList<*> {

        val token = p.currentToken
        //val s = p.valueAsString

        //p.nextValue()

        return p.readValueAs(List::class.java).toPersistentList()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}

@Serializable
data class State(
  //  @JsonSerialize(using = PersistentListSerializer::class)
    val screens: PersistentList<Screen>
) {

    constructor(screen: Screen) : this(persistentListOf(screen))

    init {
        require(screens.isNotEmpty())
    }
}

inline val State.screen: Screen
    get() = screens.last()

inline fun <reified T : Screen> State.updateScreen(
    id: ScreenId?,
    how: (T) -> UpdateWith<T, Command>
): UpdateWith<State, Command> {

    if (id == null) {
        return updateScreen(how)
    }

    val index = screens.indexOfFirst { screen -> screen.id == id && screen is T }

    if (index < 0) {
        return noCommand()
    }

    val (screen, commands) = how(screens[index] as T)

    return copy(screens = screens.set(index, screen)) command commands
}

inline fun <reified T : Screen> State.updateScreen(
    how: (T) -> UpdateWith<T, Command>
): UpdateWith<State, Command> {

    val cmds = mutableSetOf<Command>()
    val scrs = screens.fold(ArrayList<Screen>(screens.size)) { acc, screen ->

        if (screen is T) {
            val (updatedScreen, commands) = how(screen)

            cmds += commands
            acc += updatedScreen
        } else {
            acc += screen
        }

        acc
    }.toPersistentList()

    return copy(screens = scrs) command cmds
}

fun State.swapScreens(
    i: Int,
    j: Int = screens.lastIndex
): State {

    if (i == j) return this

    val tmp = screens[j]

    return copy(screens = screens.set(j, screens[i]).set(i, tmp))
}

fun State.pushScreen(
    screen: Screen
): State = copy(screens = screens.add(screen))

fun State.popScreen(): State = copy(screens = screens.pop())

private fun <T> PersistentList<T>.pop() = if (lastIndex >= 0) removeAt(lastIndex) else this
