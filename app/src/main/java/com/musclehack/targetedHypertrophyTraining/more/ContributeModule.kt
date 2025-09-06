package com.musclehack.targetedHypertrophyTraining.more

import androidx.lifecycle.ViewModel
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelBuilder
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class ContributeModule {
    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    abstract fun contributeFragment(): ContributeFragment
}