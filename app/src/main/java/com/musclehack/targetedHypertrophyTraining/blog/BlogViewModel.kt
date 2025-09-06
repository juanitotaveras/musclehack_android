package com.musclehack.targetedHypertrophyTraining.blog

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclehack.targetedHypertrophyTraining.Event
import com.musclehack.targetedHypertrophyTraining.data.Result
import com.musclehack.targetedHypertrophyTraining.data.repository.BlogRepository
import com.musclehack.targetedHypertrophyTraining.utilities.EXTRA_BLOG_LINK
import kotlinx.coroutines.launch
import javax.inject.Inject

class BlogViewModel @Inject constructor(
    private val blogRepository: BlogRepository,
    private val appContext: Context,
    private val preferences: SharedPreferences
) :
    ViewModel() {
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts
    private var notificationLink: String? = null

    /** Events */
    private val _postClickedEvent = MutableLiveData<Event<String>>()
    val postClickedEvent: LiveData<Event<String>> = _postClickedEvent
    private val _openNotificationLinkEvent = MutableLiveData<Event<String>>()
    val openNotificationLinkEvent: LiveData<Event<String>> = _openNotificationLinkEvent

    /** Initialize viewModel by fetching posts. */
    fun start() {
        if (_posts.value == null) {
            viewModelScope.launch {
                val result = blogRepository.getXmlPosts()
                if (result is Result.Success) {
                    _posts.value = result.data
                } else {
                    // log error here
                }
            }
        }
    }

    fun forceRefresh() {
        viewModelScope.launch {
            val result = blogRepository.getXmlPosts()
            if (result is Result.Success) {
                _posts.value = result.data
            } else {
                // log error here
            }
        }
    }

    fun onPostClicked(link: String) {
        _postClickedEvent.value = Event(link)
    }

    /** This will be true if the user has tapped on a notification, and
     * we have not yet asked them if they would like to open it. */
    fun hasNotificationLinkReady(): Boolean {
        return preferences.getString(EXTRA_BLOG_LINK, null) != null
    }

    /** This method will be called right after our dialog has been shown.
     * We will clear our shared prefs so we don't accidentally show this again. */
    fun notificationDialogHasBeenShown() {
        notificationLink = preferences.getString(EXTRA_BLOG_LINK, null)
        preferences.edit().remove(EXTRA_BLOG_LINK).apply()
    }

    /** User wants to open the notification link. */
    fun onConfirmOpenNotificationLinkClicked() {
        if (notificationLink != null) {
            _openNotificationLinkEvent.value = Event(notificationLink!!)
        } else {
            // possibly show error
        }
    }

    /** User has declined to see the notification. */
    fun onCancelOpenNotificationLinkClicked() {
        notificationLink = null
    }

    /** Experimental: Bypass asking the user if they want to open the link.
     * Instead, just open it immediately using Chrome Custom Tabs. */
    fun openNotificationLinkWithoutShowingDialog() {
        notificationLink = preferences.getString(EXTRA_BLOG_LINK, null)
        preferences.edit().remove(EXTRA_BLOG_LINK).apply()
        if (notificationLink != null) {
            _openNotificationLinkEvent.value = Event(notificationLink!!)
        } else {
            // possibly show error
        }
    }
}