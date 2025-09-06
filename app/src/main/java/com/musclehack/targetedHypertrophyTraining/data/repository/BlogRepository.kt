package com.musclehack.targetedHypertrophyTraining.data.repository

import com.musclehack.targetedHypertrophyTraining.blog.Post
import com.musclehack.targetedHypertrophyTraining.data.Result

interface BlogRepository {
    suspend fun getXmlPosts(): Result<List<Post>>
}