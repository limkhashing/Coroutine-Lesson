package com.kslimweb.coroutinestutorial.repository

import androidx.lifecycle.LiveData
import com.kslimweb.coroutinestutorial.api.RetrofitBuilder
import com.kslimweb.coroutinestutorial.models.User
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

object UserRepository {

    var job: CompletableJob? = null

    fun getUser(userId: String): LiveData<User> {
        job = Job()
        return object: LiveData<User>() {
            override fun onActive() {
                // when this LiveData object becomes active (when this method
                // is called), i will get the value for User
                super.onActive()
                job?.let{ theJob ->
                    CoroutineScope(IO + theJob).launch {
                        val user = RetrofitBuilder.apiService.getUser(userId)
                        // with this current Coroutine context
                        // switch over to main thread
                        withContext(Main) {
                            value = user
                            theJob.complete()
                        }
                    }
                }
            }
        }
    }

    fun cancelJobs(){
        job?.cancel()
    }
}