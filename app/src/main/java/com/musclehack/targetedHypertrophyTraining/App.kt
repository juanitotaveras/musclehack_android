package com.musclehack.targetedHypertrophyTraining

import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.DaggerApplicationGraph
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

open class App : DaggerApplication() {
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationGraph.factory().create(applicationContext)
    }
}