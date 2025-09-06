package com.musclehack.targetedHypertrophyTraining.book

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.repository.BookRepository
import com.musclehack.targetedHypertrophyTraining.premium.PagerContents
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_LAST_BOOK_READ_POSITION
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_LAST_READ_POSITION_Y
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by juanito on 1/9/2018.
 *
 */

class BookViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    val bookItems: LiveData<PagerContents?> by lazy {
        val liveData: MutableLiveData<PagerContents?> = MutableLiveData(null)
        viewModelScope.launch {
            val result = bookRepository.getBookContents()
            if (result is Result.Success) {
                liveData.value = result.data
            } else {
                // update UI with error
            }
        }
        liveData
    }

    fun onBookPageSelected(position: Int) {
        sharedPreferences.edit().putInt(PREF_LAST_BOOK_READ_POSITION, position).apply()
    }

    fun onBookPageScrolled(yPosition: Int) {
        sharedPreferences.edit().putInt(PREF_LAST_READ_POSITION_Y, yPosition).apply()
    }

    fun getSavedPagePosition(): Int = sharedPreferences.getInt(PREF_LAST_BOOK_READ_POSITION, 0)
}