package com.musclehack.targetedHypertrophyTraining.data.repository

import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.AppModule.AssetsLocalDataSource
import com.musclehack.targetedHypertrophyTraining.data.source.BookDataSource
import javax.inject.Inject

class DefaultPagerContentsRepository @Inject constructor(
    @AssetsLocalDataSource private val assetsLocalDataSource: BookDataSource
) : PagerContentsRepository {
    override suspend fun getAboutContents() = assetsLocalDataSource.getAboutContents()

    override suspend fun getTestimonialContents() = assetsLocalDataSource.getTestimonialContents()
}