package com.oliynick.max.tea.core.component

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

private fun CheckingResolver(): (suspend (Char) -> Int) = {
    throw RuntimeException("kek")
}

suspend fun throwingFun(ch: Char): Int = throw RuntimeException("error")

suspend fun main() {

    val r = CheckingResolver()

    try {
        channelFlow<Int> {

            channel.invokeOnClose {
                println("close $it")

                //if (it != null) {

                //     throw it
                // }


            }

            flowOf('a', 'b', 'c', 'd').collect { ch ->

                send(ch.toInt())

                // fire and forget
                this@channelFlow.launch(Dispatchers.IO) {
                    throwingFun(ch)
                }
            }

        }.collect {
            println("Got $it")
        }
    } catch (th: Throwable) {
        println("Intercepted $th")
    }

    println("done")
}

