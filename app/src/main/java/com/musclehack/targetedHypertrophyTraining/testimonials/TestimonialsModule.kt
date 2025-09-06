package com.musclehack.targetedHypertrophyTraining.testimonials

import androidx.lifecycle.ViewModel
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelBuilder
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class TestimonialsModule {
    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    abstract fun testimonialsFragment(): TestimonialsFragment

    @Binds
    @IntoMap
    @ViewModelKey(TestimonialsViewModel::class)
    abstract fun bindViewModel(viewModel: TestimonialsViewModel): ViewModel
}
