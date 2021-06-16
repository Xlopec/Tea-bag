package com.oliynick.max.app

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}