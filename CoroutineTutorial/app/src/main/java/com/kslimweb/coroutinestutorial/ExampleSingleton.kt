package com.kslimweb.coroutinestutorial

import com.kslimweb.coroutinestutorial.models.User

object ExampleSingleton {

    // lazy initializer only initialize this once
    // and only initialize when it called the first time
    val singletonUser: User by lazy {
        User("mitchelltabian@gmail.com", "mitch", "some_image_url.png")
    }
}
