package com.musclehack.targetedHypertrophyTraining.about

import androidx.lifecycle.ViewModel
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelBuilder
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class AboutModule {
    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    abstract fun aboutFragment(): AboutFragment

    @Binds
    @IntoMap
    @ViewModelKey(AboutViewModel::class)
    abstract fun bindViewModel(viewModel: AboutViewModel): ViewModel
}