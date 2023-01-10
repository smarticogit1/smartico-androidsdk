package com.smartico.androidsdk

/*
create aar with ./gradlew build
 */
class SmarticoSdk private constructor() {
    companion object {
        val instance = SmarticoSdk()
    }

    fun start() {
        println("----->test smartico sdk")
    }
}