package com.example.movieapp

import android.app.Application
import com.example.movieapp.data.AuthRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MovieApp : Application() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            authRepository.restoreToken()
        }
    }
}
