/*
 * Copyright (C) 2019 Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package core.misc

import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand

@Suppress("RedundantSuspendModifier")
suspend fun <C> throwingResolver(c: C): Nothing {
    throw IllegalStateException("Unexpected command $c")
}

fun <M, S> throwingUpdater(
    m: M,
    s: S
): Nothing {
    throw IllegalStateException("message=$m, state=$s")
}

fun messageAsStateUpdate(message: String, @Suppress("UNUSED_PARAMETER") state: String): UpdateWith<String, Set<String>> {
    return message.noCommand()
}

fun messageAsCommandUpdate(message: String, @Suppress("UNUSED_PARAMETER") state: String): UpdateWith<String, String> {
    return state.command(message)
}
