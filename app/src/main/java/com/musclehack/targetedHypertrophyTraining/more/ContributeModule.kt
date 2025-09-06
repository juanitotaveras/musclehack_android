package com.musclehack.targetedHypertrophyTraining.more

import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelBuilder
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ContributeModule {
    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    abstract fun contributeFragment(): ContributeFragment
}