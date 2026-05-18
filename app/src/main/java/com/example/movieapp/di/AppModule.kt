package com.example.movieapp.di

import com.example.movieapp.BuildConfig
import com.example.movieapp.data.remote.AuthApiService
import com.example.movieapp.data.remote.AuthInterceptor
import com.example.movieapp.data.remote.MovieApiService
import com.example.movieapp.data.remote.TokenAuthenticator
import com.example.movieapp.data.remote.UserApiService
import com.example.movieapp.domain.GetMovieByIdUseCase
import com.example.movieapp.domain.GetMoviesUseCase
import com.example.movieapp.data.MovieRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    // ── Unauthenticated client — used only for /auth/refresh ─────────────
    @Provides
    @Singleton
    @NoAuth
    fun provideNoAuthOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Singleton
    @NoAuth
    fun provideNoAuthRetrofit(@NoAuth okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @NoAuth
    fun provideNoAuthApiService(@NoAuth retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    // ── Authenticated client — all other API calls ────────────────────────
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideMovieApiService(retrofit: Retrofit): MovieApiService =
        retrofit.create(MovieApiService::class.java)

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    fun provideGetMoviesUseCase(repository: MovieRepository): GetMoviesUseCase =
        GetMoviesUseCase(repository)

    @Provides
    fun provideGetMovieByIdUseCase(repository: MovieRepository): GetMovieByIdUseCase =
        GetMovieByIdUseCase(repository)
}
