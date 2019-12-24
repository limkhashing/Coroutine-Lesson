package com.kslimweb.coroutinesbeginner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlin.system.measureTimeMillis

const val JOB_TIMEOUT = 1900L

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
//                fakeApiRequest()
//                fakeApiRequestTimeout()
//                fakeApiRequestTimeoutSimple()
//                fakeApiJobParallelRequest()
//                fakeApiJobJoinRequestCaller()
                fakeApiAsyncAwaitDeferredRequest()
            }

        }
    }

    private suspend fun fakeApiRequestTimeoutSimple() {
        coroutineScope {
            val job = launch {

                val result1 = getResult1FromApi() // wait until job is done
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
            delay(JOB_TIMEOUT)
            if(job.isActive){
                job.cancel()
                job.join()
                println("debug: Cancelling job...Job took longer than $JOB_TIMEOUT")
            }
        }
    }

    private suspend fun fakeApiJobJoinRequestCaller() {
        val result = fakeApiJobJoinRequest()
        // waits until all jobs in coroutine scope are complete to return result
        println("debug: result: ${result}")
        setTextOnMainThread(result)
    }

    private suspend fun fakeApiJobJoinRequest(): String {
        var timeElapsed = 0L
        withContext(IO) {

            val time = measureTimeMillis {

                val job1 = launch {
                    println("debug: starting job 1")
                    delay(1000)
                    println("debug: done job 1")
                }

                val job2 = launch {
                    println("debug: staring job 2")
                    delay(1500)
                    println("debug: done job 2")
                }

                val job3 = launch {
                    println("debug: starting job 3")
                    delay(1000)
                    println("debug: done job 3")
                }
                job1.join()
                job2.join()
                job3.join()
            }
            timeElapsed = time
            println("debug: elapsed time: ${timeElapsed}")
        }
        return "Jobs completed within $timeElapsed ms."
    }

    /**
     * Job pattern
     * Job1 and Job2 run in parallel as different coroutines
     * Also see "Deferred, Async, Await" branch(function) for parallel execution
     */
    private fun fakeApiJobParallelRequest() {
        val startTime = System.currentTimeMillis()
        val parentJob = CoroutineScope(IO).launch {
            val job1 = launch {
                val time1 = measureTimeMillis {
                    println("debug: launching job1 in thread: ${Thread.currentThread().name}")
                    val result1 = getResult1FromApi()
                    setTextOnMainThread("Got $result1")
                }
                println("debug: compeleted job1 in $time1 ms.")
            }

            val job2 = launch {
                val time2 = measureTimeMillis {
                    println("debug: launching job2 in thread: ${Thread.currentThread().name}")
                    val result2 = getResult2FromApi()
                    setTextOnMainThread("Got $result2")
                }
                println("debug: compeleted job2 in $time2 ms.")
            }
        }
        parentJob.invokeOnCompletion {
            println("debug: total elapsed time in ${System.currentTimeMillis() - startTime}")
        }
    }

    /**
     * async() is a blocking call (similar to the job pattern with job.join())
     * async() must return the type in Deferred<>
     * async() and await() benefit is can get the result in async { } and is available outside the scoipe
     * whereas result in job pattern is stuck inside coroutine
     *  NOTES:
     *  1) IF you don't call await(), it does not wait for the result
     *  2) Calling await() on both these Deffered values will EXECUTE THEM IN PARALLEL. But the RESULTS won't
     *     be published until the last result is available (in this case that's result2)
     */
    private fun fakeApiAsyncAwaitDeferredRequest() {
        CoroutineScope(IO).launch {
            val executionTime = measureTimeMillis {
                val result1: Deferred<String> = async {
                    println("debug: launching job1: ${Thread.currentThread().name}")
                    getResult1FromApi()
                }

                val result2: Deferred<String> = async {
                    println("debug: launching job2: ${Thread.currentThread().name}")
                    getResult2FromApi()
                }
                setTextOnMainThread("Got ${result1.await()}")
                setTextOnMainThread("Got ${result2.await()}")
            }
            println("debug: job1 and job2 are complete. It took ${executionTime} ms")
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
            val exceptionHandler = CoroutineExceptionHandler{_ , throwable->
                throwable.printStackTrace()
            }
            val job = withTimeoutOrNull(JOB_TIMEOUT) {
                val result1 = getResult1FromApi() // wait until job is done
                setTextOnMainThread("Got $result1")

                val result2 = getResult2FromApi(result1) // wait until job is done
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

            val result2 = getResult2FromApi(result1) // wait until job is done

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

    private suspend fun getResult2FromApi(result1: String = ""): String {
        logThread("getResult2FromApi")
        delay(1000)
        return "Result #2"
    }

    // we can see which thread is being executed on
    private fun logThread(methodName: String){
        println("debug: ${methodName}: ${Thread.currentThread().name}")
    }
}
