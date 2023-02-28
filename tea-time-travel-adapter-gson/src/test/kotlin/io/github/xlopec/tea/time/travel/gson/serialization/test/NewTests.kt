package io.github.xlopec.tea.time.travel.gson.serialization.test

import io.github.xlopec.tea.time.travel.gson.Gson
import io.github.xlopec.tea.time.travel.gson.metadata.MetadataLookupAdapterFactory
import io.github.xlopec.tea.time.travel.gson.serialization.serializer.PersistentListSerializer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

data class Project(
    val attachedTo: User?,
    val id: Int,
    val days: Int,
    val coordinatorName: String?,
    val peopleIncluded: PersistentList<String>,
)

data class Coordinator(
    val id: Int,
    val who: String,
)

interface User {

}

data class Admin(val name: String) : User
data class Moderator(val name: String) : User

@RunWith(JUnit4::class)
class NewTests {

    @Test
    fun f() {


        val gson = Gson {
            setPrettyPrinting()
            registerTypeHierarchyAdapter(PersistentList::class.java, PersistentListSerializer)
            registerTypeAdapterFactory(MetadataLookupAdapterFactory)
            //registerTypeHierarchyAdapter(User::class.java, ReflectiveSerializer<User>("__type"))
        }
        val treeJson = gson.toJson(
            Project(
                attachedTo = Admin("Max"),
                id = 123,
                days = 10,
                coordinatorName = null,
                peopleIncluded = persistentListOf("Max", "Nick", "James")
            )
        )

        println(treeJson)

        val from = gson.fromJson(treeJson, Project::class.java)

        println(from)
    }
}
