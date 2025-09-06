package com.musclehack.targetedHypertrophyTraining.testimonials

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.repository.PagerContentsRepository
import com.musclehack.targetedHypertrophyTraining.premium.PagerContents
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_SHOULD_SHOW_TESTIMONIALS_SWIPE_PROMPT
import kotlinx.coroutines.launch
import javax.inject.Inject

class TestimonialsViewModel @Inject constructor(
    private val pagerContentsRepository: PagerContentsRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    val bioItems: LiveData<PagerContents?> by lazy {
        val liveData: MutableLiveData<PagerContents?> = MutableLiveData(null)
        viewModelScope.launch {
            val result = pagerContentsRepository.getTestimonialContents()
            if (result is Result.Success) {
                liveData.value = result.data
            } else {
                // update ui with error
            }
        }
        liveData
    }

    fun onBioPageSelected(position: Int) {
        if (shouldShowSwipePrompt()) {
            sharedPreferences.edit().putBoolean(PREF_SHOULD_SHOW_TESTIMONIALS_SWIPE_PROMPT, false)
                .apply()
        }
    }

    fun shouldShowSwipePrompt() =
        sharedPreferences.getBoolean(PREF_SHOULD_SHOW_TESTIMONIALS_SWIPE_PROMPT, true)
}
