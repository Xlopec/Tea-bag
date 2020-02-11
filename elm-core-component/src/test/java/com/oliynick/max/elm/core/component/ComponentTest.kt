package com.oliynick.max.elm.core.component

import core.component.BasicComponentTest
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ComponentTest : BasicComponentTest({ env -> Component(env) })
