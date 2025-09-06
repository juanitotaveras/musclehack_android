package com.musclehack.targetedHypertrophyTraining.data.repository

import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.premium.PagerContents

interface ProductsRepository {
    suspend fun getProductsContents(): Result<PagerContents>
}