package com.example.movieapp.di

import com.example.movieapp.data.MovieRepositoryImpl
import com.example.movieapp.domain.GetMovieByIdUseCase
import com.example.movieapp.domain.GetMoviesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMovieRepository(): MovieRepositoryImpl = MovieRepositoryImpl()

    @Provides
    fun provideGetMoviesUseCase(
        repository: MovieRepositoryImpl
    ): GetMoviesUseCase = GetMoviesUseCase(repository)

    @Provides
    fun provideGetMovieByIdUseCase(
        repository: MovieRepositoryImpl
    ): GetMovieByIdUseCase = GetMovieByIdUseCase(repository)
}