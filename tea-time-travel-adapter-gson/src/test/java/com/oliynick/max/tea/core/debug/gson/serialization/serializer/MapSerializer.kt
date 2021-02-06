/*
 * Copyright (C) 2021. Maksym Oliinyk.
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

package com.oliynick.max.tea.core.debug.gson.serialization.serializer

import com.google.gson.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object MapDeserializer : JsonDeserializer<Map<*, *>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Map<*, *> {

        val genericKeyArgType = (typeOfT as ParameterizedType).actualTypeArguments[1] as Class<*>

        return json.asJsonObject.entrySet()
            .associate { (k, v) ->
                (if (k == "null") null else k) to context.deserialize<Any?>(
                    v,
                    genericKeyArgType
                )
            }
    }

}
