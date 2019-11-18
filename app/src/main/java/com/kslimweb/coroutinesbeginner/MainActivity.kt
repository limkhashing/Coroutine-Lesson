package com.kslimweb.coroutinesbeginner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
//            fakeApiRequest() // need to be called from suspend function

            // Coroutines is like a job
            // coroutines got a property call scoping. Coroutines Scope
            // is a way to organize coroutines into groupings
            // IO can be network request or database request
            // Main is doing work on Main Thread
            // Default is for doing any heavy computation work
            // IO, Main, Default
            CoroutineScope(IO).launch {
                fakeApiRequest()
//                fakeApiRequestTimeout()
            }
        }
    }

    private fun setNewText(input: String){
        val newText = text.text.toString() + "\n$input"
        text.text = newText
    }
    private suspend fun setTextOnMainThread(input: String) {
        // we can do with CoroutineScope(IO) too
        // Switch the Context of the Coroutines to whatever mentioned in parameter
        // Coroutines is thread independent
        withContext (Main) {
            setNewText(input)
        }
    }

    private suspend fun fakeApiRequestTimeout() {
        withContext(IO) {

            val job = withTimeoutOrNull(1900L) {
                val result1 = getResult1FromApi() // wait until job is done
                setTextOnMainThread("Got $result1")

                val result2 = getResult2FromApi() // wait until job is done
                setTextOnMainThread("Got $result2")

            } // waiting for job to complete...

            if (job == null) {
                val cancelMessage = "Cancelling job...Job took longer than 1900 ms"
                println("debug: $cancelMessage")
                setTextOnMainThread(cancelMessage)
            }
        }
    }

    private suspend fun fakeApiRequest() {
        logThread("fakeApiRequest")

        // suspend function need to call from another suspend function
        val result1 = getResult1FromApi() // wait until job is done
        println("debug: $result1")

        // it will not work as we are doing task in background thread
        // text.text = result1

        if (result1 == "Result #1") {
            setTextOnMainThread("Got $result1")

            val result2 = getResult2FromApi() // wait until job is done

            if (result2 == "Result #2") {
                setTextOnMainThread("Got $result2")
            } else {
                setTextOnMainThread("Couldn't get Result #2")
            }
        } else {
            setTextOnMainThread("Couldn't get Result #1")
        }
    }

    // A suspending function is simply a function that can be paused and resumed at a later time.
    // They can execute a long running operation and wait for it to complete without blocking.
    // suspend keyword used and called within Coroutines
    private suspend fun getResult1FromApi(): String {
        logThread("getResult1FromApi")
        delay(1000) // Does not block thread. Just suspends the coroutine inside the thread
//        Thread.sleep(1000) // sleep the entire thread and all the coroutine insde the thread will disable
        return "Result #1"
    }

    private suspend fun getResult2FromApi(): String {
        logThread("getResult2FromApi")
        delay(1000)
        return "Result #2"
    }

    // we can see which thread is being executed on
    private fun logThread(methodName: String){
        println("debug: ${methodName}: ${Thread.currentThread().name}")
    }
}
