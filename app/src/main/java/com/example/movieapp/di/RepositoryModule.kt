package com.example.movieapp.di

import com.example.movieapp.data.AuthRepository
import com.example.movieapp.data.AuthRepositoryImpl
import com.example.movieapp.data.ContentResolverUriReader
import com.example.movieapp.data.MovieRepository
import com.example.movieapp.data.MovieRepositoryImpl
import com.example.movieapp.data.TmdbRepository
import com.example.movieapp.data.TmdbRepositoryImpl
import com.example.movieapp.data.UriReader
import com.example.movieapp.data.UserRepository
import com.example.movieapp.data.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMovieRepository(impl: MovieRepositoryImpl): MovieRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUriReader(impl: ContentResolverUriReader): UriReader

    @Binds
    @Singleton
    abstract fun bindTmdbRepository(impl: TmdbRepositoryImpl): TmdbRepository
}
