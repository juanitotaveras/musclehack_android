package com.musclehack.targetedHypertrophyTraining.premium

class PagerContents {
    private var items: List<Item>? = null
    private var baseFolder: String? = null

    val itemCount: Int
        get() =  items?.size ?: 0

    fun setBaseAssetPath(folderName: String) {
        this.baseFolder = folderName
    }

    fun getItemFile(position: Int): String? {
        return items?.get(position)?.file
    }

    fun getItemTitle(position: Int): String? {
        return items?.get(position)?.title
    }

    fun getItemPath(position: Int): String {
        val file = getItemFile(position)
        return if (baseFolder == null) {
            "file:///android_asset/products/" + file!!
        } else "file:///android_asset/$baseFolder/$file"
    }

    inner class Item {
        var file: String? = null
        var title: String? = null
    }
}
