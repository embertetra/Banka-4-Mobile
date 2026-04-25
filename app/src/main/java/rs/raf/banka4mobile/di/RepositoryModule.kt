package rs.raf.banka4mobile.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rs.raf.banka4mobile.data.repository.AuthRepositoryImpl
import rs.raf.banka4mobile.data.repository.HomeRepositoryImpl
import rs.raf.banka4mobile.data.repository.ExchangeRepositoryImpl
import rs.raf.banka4mobile.domain.repository.AuthRepository
import rs.raf.banka4mobile.domain.repository.HomeRepository
import rs.raf.banka4mobile.domain.repository.ExchangeRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindExchangeRepository(
        exchangeRepositoryImpl: ExchangeRepositoryImpl
    ): ExchangeRepository

    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        homeRepositoryImpl: HomeRepositoryImpl
    ): HomeRepository
}