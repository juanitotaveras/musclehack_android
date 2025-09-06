package com.musclehack.targetedHypertrophyTraining.data.repository

import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.AppModule.AssetsLocalDataSource
import com.musclehack.targetedHypertrophyTraining.data.source.BookDataSource
import javax.inject.Inject

class DefaultBookRepository @Inject constructor(
    @AssetsLocalDataSource private val bookLocalDataSource: BookDataSource
) : BookRepository {
    override suspend fun getBookContents() = bookLocalDataSource.getBookContents()
}