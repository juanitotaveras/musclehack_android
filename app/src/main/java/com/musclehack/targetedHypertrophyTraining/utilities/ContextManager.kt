package com.musclehack.targetedHypertrophyTraining.utilities

import android.content.Context
import android.content.res.Resources

class ContextManager(val ctxt: Context) {
    val res: Resources = ctxt.resources
    val packageName: String = ctxt.packageName
}