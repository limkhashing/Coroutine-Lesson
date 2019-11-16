package com.kslimweb.coroutinestutorial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.kslimweb.coroutinestutorial.models.User
import com.kslimweb.coroutinestutorial.repository.UserRepository

class MainViewModel: ViewModel() {

    private val _userId: MutableLiveData<String> = MutableLiveData()

    // switchMap operator is similar with RxJava switchMap
    // it will observing LiveData _userId object
    // when it changes, switchMap will trigger and execute the code
    // Thats why it called Map. It mapping from one object type to another
    val user: LiveData<User> = Transformations
        .switchMap(_userId){
            UserRepository.getUser(it)
        }

    fun setUserId(userId: String) {
        // if userId is the same, don't do anything
        if (_userId.value == userId) {
            return
        }
        _userId.value = userId
    }

    fun cancelJobs() {
        // cancel jobs that are currently running
        UserRepository.cancelJobs()
    }
}