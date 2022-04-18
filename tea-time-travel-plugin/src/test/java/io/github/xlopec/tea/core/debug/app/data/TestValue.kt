package io.github.xlopec.tea.core.debug.app.data

import com.google.gson.internal.LazilyParsedNumber
import io.github.xlopec.tea.core.debug.app.domain.CollectionWrapper
import io.github.xlopec.tea.core.debug.app.domain.NumberWrapper
import io.github.xlopec.tea.core.debug.app.domain.Property
import io.github.xlopec.tea.core.debug.app.domain.Ref
import io.github.xlopec.tea.core.debug.app.domain.StringWrapper
import io.github.xlopec.tea.core.debug.app.domain.Type

val TestUserValue = Ref(
    Type.of("com.max.oliynick.Test"),
    setOf(
        Property("name", StringWrapper("Max")),
        Property("surname", StringWrapper("Oliynick")),
        Property(
            "contacts", Ref(
                Type.of("com.max.oliynick.Contact"),
                setOf(
                    Property(
                        "site", Ref(
                            Type.of("java.util.URL"),
                            setOf(
                                Property("domain", StringWrapper("google")),
                                // LazilyParsedNumber is workaround since Number != LazilyParsedNumber,
                                // LazilyParsedNumber might be compared only with other lazily parsed numbers
                                Property("port", NumberWrapper(LazilyParsedNumber(8080.toString()))),
                                Property("protocol", StringWrapper("https"))
                            ),
                        )
                    ),
                )
            )
        ),
        Property("position", StringWrapper("Developer")),
    )
)

val TestAppStateValue =
    Ref(
        Type.of("app.State"),
        setOf(
            Property(
                "users",
                CollectionWrapper(
                    listOf(
                        TestUserValue,
                        TestUserValue,
                        TestUserValue,
                        TestUserValue,
                        TestUserValue,
                    )
                )
            )
        )
    )