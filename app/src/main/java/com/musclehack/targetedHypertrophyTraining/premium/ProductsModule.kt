package com.musclehack.targetedHypertrophyTraining.premium

import androidx.lifecycle.ViewModel
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelBuilder
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class ProductsModule {
    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun productsPagerFragment(): ProductsPagerFragment

    @Binds
    @IntoMap
    @ViewModelKey(ProductsViewModel::class)
    abstract fun bindViewModel(viewModel: ProductsViewModel): ViewModel
}