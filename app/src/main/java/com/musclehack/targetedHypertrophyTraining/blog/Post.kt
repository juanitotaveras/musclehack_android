package com.musclehack.targetedHypertrophyTraining.blog

import android.graphics.Bitmap
import java.util.Date

class Post(
    val title: String, val link: String, val pubDate: Date, val description: String,
    val imageURL: String
) {
    var thumbnail: Bitmap? = null
    override fun toString(): String {
        return "$title, \n$link, \n$pubDate, \n$description \n$imageURL"
    }

    override fun equals(other: Any?): Boolean {
        val otherPost = other as Post
        return this.title == otherPost.title && this.link == otherPost.link
    }

    fun hasEqualContents(other: Post): Boolean {
        return title == other.title && link == other.link && description == other.description
    }

    fun noDate(): Boolean {
        return this.pubDate.time == 0L
    }

    override fun hashCode(): Int {
        var result = 1
        result += title.hashCode() * 4
        result *= link.hashCode()
        result += pubDate.hashCode()
        return result
    }
}
