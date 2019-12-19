package com.max.weatherviewer

//import com.max.weatherviewer.presentation.reduce
import com.google.gson.*
import com.max.weatherviewer.app.State
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.domain.Description
import com.max.weatherviewer.domain.Title
import com.max.weatherviewer.screens.feed.Feed
import com.max.weatherviewer.screens.feed.FeedLoading
import com.max.weatherviewer.screens.feed.LoadCriteria
import com.max.weatherviewer.screens.feed.Preview
import com.oliynick.max.elm.time.travel.gsonSerializer
import com.oliynick.max.elm.time.travel.registerReflectiveTypeAdapter
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.junit.Test
import protocol.NotifyComponentAttached
import java.lang.reflect.Type
import java.net.URL
import java.util.*


class ReducerTest {

    @Test
    fun testReducer() {

        val ser = gsonSerializer {

            /*Feed::class.sealedSubclasses.forEach {
                registerTypeAdapter(it.java, polyAdapter(it.java))
            }*/

            registerTypeHierarchyAdapter(
                PersistentList::class.java,
                object : com.google.gson.JsonDeserializer<PersistentList<*>>, com.google.gson.JsonSerializer<PersistentList<*>> {
                    override fun deserialize(
                        json: JsonElement,
                        typeOfT: Type?,
                        context: JsonDeserializationContext
                    ): PersistentList<*> {
                        return json.asJsonArray.map {
                            // fixme workaround

                            it.asJsonObject.run {
                                context.deserialize<Any?>(this["value"], Class.forName(this["type"].asString))
                            }


                        }.toPersistentList()
                    }

                    override fun serialize(
                        src: PersistentList<*>,
                        typeOfSrc: Type?,
                        context: JsonSerializationContext
                    ): JsonElement {
                        return JsonArray().apply {
                            for (v in src) {
                                add(JsonObject().also {
                                    it.addProperty("type", v!!::class.java.name)
                                    it.add("value", context.serialize(v))
                                }
                                )
                            }
                        }
                    }

                })

            //registerAdapter<NotifyComponentAttached>()

            /* RuntimeTypeAdapterFactory.of(State::class.java)
                 .also { registerTypeAdapterFactory(it) }*/

            registerReflectiveTypeAdapter<Feed>()
            registerReflectiveTypeAdapter<LoadCriteria>()


        }

        val state = State(
            persistentListOf(
                Preview(
                    UUID.randomUUID(),
                    LoadCriteria.Favorite,
                    listOf(
                        Article(
                            URL("http://www.google.com"),
                            Title("test"),
                            null,
                            Description("test"),
                            null,
                            Date(),
                            false
                        )
                    )
                ),
                FeedLoading(
                    UUID.randomUUID(),
                    LoadCriteria.Trending
                )
            )
        )

        /*  val simpleModule = SimpleModule(
              "SimpleModule",
              Version(1, 0, 0, null)
          )

          simpleModule.addSerializer(PersistentList::class.java, PersistentListSerializer)
          simpleModule.addDeserializer(PersistentList::class.java, PersistentListDeserializer)
         //     .registerSubtypes(String::class.java)

          val mapper = ObjectMapper().enableDefaultTyping(
              ObjectMapper.DefaultTyping.EVERYTHING,
              JsonTypeInfo.As.PROPERTY
          ).registerModule(simpleModule)
              .registerModule(KotlinModule())





          val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(state)


          println(json)
  */
        // val value = mapper.readValue(json, State::class.java)


        val gsonJson = ser.run { state.toJson() }

        val msg = NotifyComponentAttached(state)

        val msgJson = ser.run { msg.toJson() }

        println(msgJson)

        val mm = ser.run { msgJson.fromJson(NotifyComponentAttached::class.java) }
        println(mm)

        /* assertTrue(
             reduce(
                 State.Loading,
                 Command.LoadWeather
             ) == State.Loading)

         val th = RuntimeException("foo")

         assertTrue(
             reduce(
                 State.Loading,
                 Command.FeedLoadFailure(th)
             ) == State.LoadFailure(th))

         val weather = Weather(Location(30.0, 30.0), Wind(10.0, 30.0))

         assertTrue(
             reduce(
                 State.Loading,
                 Command.FeedLoaded(weather)
             ) == State.Preview(weather))*/
    }
}