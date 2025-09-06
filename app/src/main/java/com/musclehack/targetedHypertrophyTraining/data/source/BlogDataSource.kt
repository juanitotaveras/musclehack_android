package com.musclehack.targetedHypertrophyTraining.data.source

import com.musclehack.targetedHypertrophyTraining.blog.Post
import com.musclehack.targetedHypertrophyTraining.data.Result

interface BlogDataSource {
    suspend fun getXmlForPosts(): Result<List<Post>>
}