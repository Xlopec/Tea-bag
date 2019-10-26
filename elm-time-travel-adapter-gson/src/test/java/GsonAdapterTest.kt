import com.oliynick.max.elm.time.travel.gson.gson
import core.data.Id
import core.data.Name
import core.data.User
import core.data.photo
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import protocol.*
import java.util.*
import kotlin.test.assertEquals

@RunWith(JUnit4::class)
class GsonAdapterTest {

    private val gson = gson()

    private val testUser = User(
        Id(UUID.randomUUID()),
        Name("John"),
        listOf(
            photo("https://www.google.com"),
            photo("https://www.google.com1"),
            photo("https://www.google.com2")
        )
    )

    private val testConverters = converters {
        +URLConverter
        +UUIDConverter
    }

    @Test
    fun `test initial value is same as parsed one`() {

        val initialValue = SomeTestCommand(SomeTestString("hello")/*, listOf(1234)*/, emptyList()).toValue(testConverters)
        val json = gson.toJson(initialValue)
        val fromJsonValue = gson.fromJson(json, Value::class.java)

        assertEquals(initialValue, fromJsonValue)
    }

    @Test
    fun `test initial command 'ApplyMessage' is same as parsed one`() {

        val initialValue = testUser.toValue(testConverters)
        val initialApplyCmd = ApplyMessage(initialValue)

        val cmdJson = gson.toJson(initialApplyCmd)
        val fromJson = gson.fromJson(cmdJson, ApplyMessage::class.java)

        assertEquals(initialApplyCmd, fromJson)
    }

    @Test
    fun `test initial command 'ApplyState' is same as parsed one`() {

        val initialValue = testUser.toValue(testConverters)
        val initialApplyCmd = ApplyState(initialValue)

        val applyMessageJson = gson.toJson(initialApplyCmd)
        val fromJson = gson.fromJson(applyMessageJson, ApplyState::class.java)

        assertEquals(initialApplyCmd, fromJson)
    }

    @Test
    fun `test initial command 'NotifyComponentSnapshot' is same as parsed one`() {

        val initialValue = testUser.toValue(testConverters)
        val initialApplyCmd = NotifyComponentSnapshot(initialValue, initialValue, initialValue)

        val applyMessageJson = gson.toJson(initialApplyCmd)
        val fromJson = gson.fromJson(applyMessageJson, NotifyComponentSnapshot::class.java)

        assertEquals(initialApplyCmd, fromJson)
    }

    @Test
    fun `test initial message 'NotifyComponentAttached' is same as parsed one`() {

        val initialValue = testUser.toValue(testConverters)
        val initialApplyCmd = NotifyComponentAttached(initialValue)

        val applyMessageJson = gson.toJson(initialApplyCmd)
        val fromJson = gson.fromJson(applyMessageJson, NotifyComponentAttached::class.java)

        assertEquals(initialApplyCmd, fromJson)
    }

    @Test
    fun `test initial message 'ActionApplied' is same as parsed one`() {

        val initialApplyCmd = ActionApplied(UUID.randomUUID())
        val applyMessageJson = gson.toJson(initialApplyCmd)
        val fromJson = gson.fromJson(applyMessageJson, ActionApplied::class.java)

        assertEquals(initialApplyCmd, fromJson)
    }

}