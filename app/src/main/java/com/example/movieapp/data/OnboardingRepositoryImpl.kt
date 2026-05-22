package com.example.movieapp.data

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val PREFS_NAME = "movie_app_prefs"
private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

class OnboardingRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : OnboardingRepository {
        private val prefs by lazy {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        override fun isCompleted(): Boolean = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

        override fun markCompleted() {
            prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETED, true) }
        }
    }
