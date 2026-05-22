package com.example.movieapp.data.remote

import com.example.movieapp.data.TokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor
    @Inject
    constructor(
        private val tokenStore: TokenStore,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()

            // Skip auth header for the login endpoint itself
            if (request.url.pathSegments.containsAll(listOf("auth", "login"))) {
                return chain.proceed(request)
            }

            val token =
                tokenStore.get()
                    ?: return chain.proceed(request)

            val authenticatedRequest =
                request
                    .newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()

            return chain.proceed(authenticatedRequest)
        }
    }
