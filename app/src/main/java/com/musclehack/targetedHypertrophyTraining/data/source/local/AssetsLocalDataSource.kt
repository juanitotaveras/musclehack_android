package com.musclehack.targetedHypertrophyTraining.data.source.local

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.source.BookDataSource
import com.musclehack.targetedHypertrophyTraining.premium.PagerContents
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class AssetsLocalDataSource internal constructor(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BookDataSource {
    override suspend fun getBookContents() = parsePagerContents("book")

    override suspend fun getProductsContents() = parsePagerContents("products")

    override suspend fun getAboutContents() = parsePagerContents("about")

    override suspend fun getTestimonialContents() = parsePagerContents("testimonials")

    private suspend fun parsePagerContents(baseFolder: String): Result<PagerContents> {
        return withContext(ioDispatcher) {
            val gson = Gson()
            val contents: PagerContents?
            val assetManager = context.assets
            val reader = BufferedReader(
                InputStreamReader(assetManager.open("$baseFolder/contents.json"))
            )
            contents = gson.fromJson(reader, PagerContents::class.java)

            contents?.setBaseAssetPath(baseFolder)

            if (contents == null) {
                val e = Exception("Exception parsing JSON.")
                Log.e(javaClass.simpleName, "Exception parsing JSON", e)
                Result.Error(e)
            } else {
                Result.Success(contents)
            }
        }
    }
}