package com.musclehack.targetedHypertrophyTraining.premium

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.repository.ProductsRepository
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_LAST_PRODUCT_VIEWED
import com.musclehack.targetedHypertrophyTraining.utilities.PREF_SHOULD_SHOW_PRODUCTS_SWIPE_PROMPT
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by juanito on 1/12/2018.
 *
 */

class ProductsViewModel @Inject constructor(
    private val productsRepository: ProductsRepository,
    private val sharedPreferences: SharedPreferences
) :
    ViewModel() {
    val productItems: LiveData<PagerContents?> by lazy {
        val liveData: MutableLiveData<PagerContents?> = MutableLiveData(null)
        viewModelScope.launch {
            val result = productsRepository.getProductsContents()
            if (result is Result.Success) {
                liveData.value = result.data
            } else {
                // update UI with error
            }
        }
        liveData
    }

    fun onProductPageSelected(position: Int) {
        sharedPreferences.edit().putInt(PREF_LAST_PRODUCT_VIEWED, position).apply()

        if (shouldShowSwipePrompt()) {
            sharedPreferences.edit().putBoolean(PREF_SHOULD_SHOW_PRODUCTS_SWIPE_PROMPT, false)
                .apply()
        }
    }

    fun shouldShowSwipePrompt() =
        sharedPreferences.getBoolean(PREF_SHOULD_SHOW_PRODUCTS_SWIPE_PROMPT, true)

    fun getSavedProductPosition(): Int = sharedPreferences.getInt(PREF_LAST_PRODUCT_VIEWED, 0)
}
