package com.musclehack.targetedHypertrophyTraining.premium

/**
 * Created by juanito on 1/11/2018.
 *
 */

class PagerContents {
    private var items: List<Item>? = null
    private var baseFolder: String? = null

    val itemCount: Int
        get() = items!!.size

    fun setBaseAssetPath(folderName: String) {
        this.baseFolder = folderName
    }

    fun getItemFile(position: Int): String? {
        return items!![position].file
    }

    fun getItemTitle(position: Int): String? {
        return items!![position].title
    }

    fun getItemPath(position: Int): String {
        val file = getItemFile(position)
        return if (baseFolder == null) {
            "file:///android_asset/products/" + file!!
        } else "file:///android_asset/$baseFolder/$file"
    }

    internal class Item {
        var file: String? = null
        var title: String? = null
    }
}
