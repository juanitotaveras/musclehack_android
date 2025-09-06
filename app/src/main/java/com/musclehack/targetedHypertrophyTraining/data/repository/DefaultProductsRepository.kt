package com.musclehack.targetedHypertrophyTraining.data.repository

import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.AppModule.AssetsLocalDataSource
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.source.BookDataSource
import com.musclehack.targetedHypertrophyTraining.premium.PagerContents
import javax.inject.Inject

class DefaultProductsRepository @Inject constructor(
    @AssetsLocalDataSource private val assetsLocalDataSource: BookDataSource
) : ProductsRepository {
    override suspend fun getProductsContents(): Result<PagerContents> {
        return assetsLocalDataSource.getProductsContents()
    }
}