package com.kslimweb.coroutinestutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.user.observe(this, Observer {
            println("DEBUG: $it")
        })

        // we still need to trigger the request
        // we only set "1" because only got 1 User in API
        viewModel.setUserId("1")

        // it will get the same object / memory address whenever change activity
        println("DEBUG: ExampleSingleton: ${ExampleSingleton}")
    }

    override fun onDestroy() {
        super.onDestroy()
        // if the job is not null, it will cancel
        viewModel.cancelJobs()
    }
}
