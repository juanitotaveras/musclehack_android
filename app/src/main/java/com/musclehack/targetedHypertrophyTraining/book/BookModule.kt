package com.musclehack.targetedHypertrophyTraining.book

import androidx.lifecycle.ViewModel
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelBuilder
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class BookModule {

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun bookPagerFragment(): BookPagerFragment

    @Binds
    @IntoMap
    @ViewModelKey(BookViewModel::class)
    abstract fun bindViewModel(viewModel: BookViewModel): ViewModel
}