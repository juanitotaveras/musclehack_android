package com.musclehack.targetedHypertrophyTraining.utilities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.res.ResourcesCompat
import com.musclehack.targetedHypertrophyTraining.R

class BrowserUtils {
    companion object {
        fun openUrlInChromeCustomTabs(context: Context?, url: String) {
            val uri = Uri.parse(url)

            // create an intent builder
            val intentBuilder = CustomTabsIntent.Builder()

            context?.let { ctxt ->
                // set toolbar colors
                intentBuilder.setToolbarColor(getColor(ctxt, R.color.colorPrimary01))
                intentBuilder.setSecondaryToolbarColor(getColor(ctxt, R.color.colorPrimary01))

                // set start and exit animations
                intentBuilder.setStartAnimations(ctxt, R.anim.slide_in, R.anim.slide_out)
                intentBuilder.setExitAnimations(
                    ctxt, android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )

                // build custom tabs intent
                val customTabsIntent = intentBuilder.build()

                // launch the url
                try {
                    customTabsIntent.launchUrl(context, uri)
                } catch (e: ActivityNotFoundException) {
                    openUrlInAnyBrowser(url, context)
                }

            }
        }

        fun openUrlInChromeCustomTabs(activity: Activity, url: String) {
            val uri = Uri.parse(url)

            // create an intent builder
            val intentBuilder = CustomTabsIntent.Builder()

            val ctxt = activity.applicationContext
            // set toolbar colors
            intentBuilder.setToolbarColor(getColor(ctxt, R.color.colorPrimary01))
            intentBuilder.setSecondaryToolbarColor(getColor(ctxt, R.color.colorPrimary01))

            // set start and exit animations
            intentBuilder.setStartAnimations(ctxt, R.anim.slide_in, R.anim.slide_out)
            intentBuilder.setExitAnimations(
                ctxt, android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )

            // build custom tabs intent
            val customTabsIntent = intentBuilder.build()

            // launch the url
            try {
                customTabsIntent.launchUrl(activity, uri)
            } catch (e: ActivityNotFoundException) {
                openUrlInAnyBrowser(url, activity)
            }


        }

        private fun getColor(context: Context, resID: Int): Int {
            return ResourcesCompat.getColor(context.resources, resID, null)
        }

        fun isChromeAvailable(context: Context?): Boolean {
            val chromePackage = "com.android.chrome"
            val packageManager = context?.packageManager
            val mgr = packageManager
            if (mgr != null) {
                try {
                    return mgr.getApplicationInfo(chromePackage, 0).enabled
                } catch (e: Exception) { //android.content.pm.PackageManager$NameNotFoundException
                    Log.e(javaClass.simpleName, "Browser not found exception")
                    return false
                }

            }
            return false
        }

        fun openUrlInChrome(context: Context?, url: String) {
            if (context == null)
                return
            try {
                try {
                    val uri = Uri.parse("googlechrome://navigate?url=$url")
                    val i = Intent(Intent.ACTION_VIEW, uri)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(i)
                } catch (e: ActivityNotFoundException) {
                    val uri = Uri.parse(url)
                    // Chrome is probably not installed
                    // OR not selected as default browser OR if no Browser is selected as default browser
                    val i = Intent(Intent.ACTION_VIEW, uri)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(i)
                }

            } catch (ex: Exception) {
                //Timber.e(ex, null)
                Log.e(javaClass.simpleName, ex.toString())
            }
        }

        fun openUrlInAnyBrowser(url: String, ctxt: Context) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctxt.startActivity(intent)
        }
    }
}