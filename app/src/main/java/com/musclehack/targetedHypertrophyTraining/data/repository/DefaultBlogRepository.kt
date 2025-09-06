package com.musclehack.targetedHypertrophyTraining.data.repository

import com.musclehack.targetedHypertrophyTraining.blog.Post
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.AppModule.BlogRemoteDataSource
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.source.BlogDataSource
import javax.inject.Inject

class DefaultBlogRepository @Inject constructor(
    @BlogRemoteDataSource private val blogRemoteDataSource: BlogDataSource
) : BlogRepository {
    override suspend fun getXmlPosts(): Result<List<Post>> {
        return blogRemoteDataSource.getXmlForPosts()
    }
}