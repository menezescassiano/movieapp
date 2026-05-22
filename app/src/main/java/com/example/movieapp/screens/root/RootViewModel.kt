package com.example.movieapp.screens.root

import androidx.lifecycle.ViewModel
import com.example.movieapp.data.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class RootViewModel
    @Inject
    constructor(
        sessionManager: SessionManager,
    ) : ViewModel() {
        val logoutEvent: SharedFlow<Unit> = sessionManager.logoutEvent
    }
