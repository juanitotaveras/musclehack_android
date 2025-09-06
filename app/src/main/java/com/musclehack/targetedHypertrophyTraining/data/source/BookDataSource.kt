package com.musclehack.targetedHypertrophyTraining.data.source


import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.premium.PagerContents

interface BookDataSource {
    suspend fun getBookContents(): Result<PagerContents>
    suspend fun getProductsContents(): Result<PagerContents>
    suspend fun getAboutContents(): Result<PagerContents>
    suspend fun getTestimonialContents(): Result<PagerContents>
}