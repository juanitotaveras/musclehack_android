package com.musclehack.targetedHypertrophyTraining.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.repository.PagerContentsRepository
import com.musclehack.targetedHypertrophyTraining.premium.PagerContents
import kotlinx.coroutines.launch
import javax.inject.Inject

class AboutViewModel @Inject constructor(
    private val pagerContentsRepository: PagerContentsRepository
) : ViewModel() {
    val bioItems: LiveData<PagerContents?> by lazy {
        val liveData: MutableLiveData<PagerContents?> = MutableLiveData(null)
        viewModelScope.launch {
            val result = pagerContentsRepository.getAboutContents()
            if (result is Result.Success) {
                liveData.value = result.data
            } else {
                // update ui with error
            }
        }
        liveData
    }
}