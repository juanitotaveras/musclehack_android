package com.musclehack.targetedHypertrophyTraining.data.source.remote

import android.util.Log
import android.util.Xml
import com.musclehack.targetedHypertrophyTraining.blog.Post
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.source.BlogDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class BlogRemoteDataSource internal constructor(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BlogDataSource {
    private var urlLink = "https://musclehack.com/feed"

    @Suppress("BlockingMethodInNonBlockingContext")
    // Suppressing warning because we are calling it with ioDispatcher
    override suspend fun getXmlForPosts(): Result<List<Post>> {
        // Fetching posts from server
        return withContext(ioDispatcher) {
            var inputStream: InputStream? = null
            try {
                val url = URL(urlLink)
                inputStream = url.openConnection().getInputStream()
                val xmlPosts = parseFeed(inputStream)
                if (xmlPosts.isEmpty()) {
                    //return InternetConnectionErrorEvent()
                    // TODO: Make exception
                    inputStream.close()
                    return@withContext Result.Error(Exception("InternetConnectionError"))
                }
                return@withContext Result.Success(xmlPosts)
            } catch (e: IOException) {
                return@withContext Result.Error(Exception("InternetConnectionError"))
            } catch (e: XmlPullParserException) {
                Log.e("x", "Error--", e)
                return@withContext Result.Error(Exception("InternetConnectionError"))
            } catch (e: Exception) {
                return@withContext Result.Error(Exception("Something else happened..."))
            } finally {
                inputStream?.close()
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parseFeed(inputStream: InputStream): ArrayList<Post> {
        var title: String? = null
        var link: String? = null
        var description: String? = null
        var imageUrl: String? = null
        var pubDate: Date? = null
        var isItem = false
        val posts = ArrayList<Post>()

        try {
            val xmlPullParser = Xml.newPullParser()
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            xmlPullParser.setInput(inputStream, "UTF-8")

            xmlPullParser.nextTag()
            var count = 0
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                val eventType = xmlPullParser.eventType

                val name = xmlPullParser.name ?: continue

                if (eventType == XmlPullParser.END_TAG) {
                    if (name.equals("item", ignoreCase = true)) {
                        isItem = false
                    }
                    continue
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if (name.equals("item", ignoreCase = true)) {
                        isItem = true
                        continue
                    }
                }

                var result = ""
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.text
                    xmlPullParser.nextTag()
                }
                count++

                when {
                    name.equals("title", ignoreCase = true) -> title = result
                    name.equals("link", ignoreCase = true) && count > 2 -> link = result
                    name.equals("pubDate", ignoreCase = true) -> {
                        pubDate = SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.UK)
                            .parse(result)
                    }

                    name.equals("description", ignoreCase = true) && count > 4 ->
                        description = result

                    name.equals("media:thumbnail", ignoreCase = true) -> {
                        imageUrl = xmlPullParser.getAttributeValue(null, "url")
                    }
                }

                if (title != null && link != null && description != null && imageUrl != null
                    && pubDate != null
                ) {
                    if (isItem) {
                        val post = Post(title, link, pubDate, description, imageUrl)
                        posts.add(post)
                    }
                    title = null
                    link = null
                    description = null
                    imageUrl = null
                    isItem = false
                }
            }
            return posts
        } catch (e: Exception) {
            Log.e("x", e.toString())
            return posts
        }
    }
}