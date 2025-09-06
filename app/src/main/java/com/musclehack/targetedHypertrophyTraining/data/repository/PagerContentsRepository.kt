package com.musclehack.targetedHypertrophyTraining.data.repository

import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.premium.PagerContents

interface PagerContentsRepository {
    suspend fun getAboutContents(): Result<PagerContents>
    suspend fun getTestimonialContents(): Result<PagerContents>
}