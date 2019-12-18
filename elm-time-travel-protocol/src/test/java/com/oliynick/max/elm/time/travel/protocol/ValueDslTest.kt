package com.oliynick.max.elm.time.travel.protocol

import core.data.*
import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import protocol.*
import java.net.URL
import java.util.*

@RunWith(JUnit4::class)
class ValueDslTest {

    private val testUser = User(
        RandomId(),
        Name("John"),
        listOf(
            Photo("https://www.google.com"),
            Photo("https://www.google1.com"),
            Photo("https://www.google3.com")
        )
    )

    @Test
    fun `test DSL based conversion to a Value returns an expected Value instance`() {

        val actual = testUser toRef {
            ref {
                "id" of id.ref {
                    "value" of id.uuid.toString()
                }
                "name" of name.ref {
                    "value" of name.value
                }
                "photos".of(photos) { wrap(url.toExternalForm()) }
                "avatar" of avatar?.toExternalForm()
            }
        }

        val expected = Ref(
            RemoteType(User::class.java),
            setOf(
                Property(
                    RemoteType(Id::class.java),
                    "id",
                    Ref(
                        RemoteType(Id::class.java),
                        setOf(
                            Property(
                                RemoteType(String::class.java),
                                "value",
                                wrap(testUser.id.uuid.toString())
                            )
                        )
                    )
                ),
                Property(
                    RemoteType(Name::class.java),
                    "name",
                    Ref(
                        RemoteType(Name::class.java),
                        setOf(
                            Property(
                                RemoteType(String::class.java),
                                "value",
                                wrap(testUser.name.value)
                            )
                        )
                    )
                ),
                Property(
                    RemoteType(testUser.photos::class.java),
                    "photos",
                    CollectionWrapper(
                        testUser.photos.map {
                            wrap(it.url.toExternalForm())
                        }
                    )
                ),
                Property(
                    RemoteType(String::class.java),
                    "avatar",
                    wrap(String::class.java)
                )
            )
        )

        actual shouldBe expected
    }

    @Test
    fun `test dsl based conversion from a Value returns initial instance`() {

        val userValue = testUser toRef {
            ref {
                "id" of id.ref {
                    "value" of id.uuid.toString()
                }
                "name" of name.value
                "photos".of(photos) { wrap(url.toExternalForm()) }
                "avatar" of avatar?.toExternalForm()
            }
        }

        val idMapper : (Ref) -> Id = { ref -> ref nonNull { Id("value" nonNull UUID::fromString) } }

        val actualUser = userValue nonNull {
            User(
                id = "id" nonNull idMapper,
                name = "name" nonNull ::Name,
                photos = "photos" nonNull { photos: CollectionWrapper -> photos.value.map { v -> Photo(URL((v as StringWrapper).value)) } },
                avatar = "avatar" nullable ::Avatar
            )
        }

        actualUser shouldBe testUser
    }

}