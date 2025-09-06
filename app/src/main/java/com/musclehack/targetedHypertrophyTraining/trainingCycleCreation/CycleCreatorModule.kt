package com.musclehack.targetedHypertrophyTraining.trainingCycleCreation

import androidx.lifecycle.ViewModel
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelBuilder
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
abstract class CycleCreatorModule {

    @ContributesAndroidInjector(
        modules = [
            ViewModelBuilder::class
        ]
    )
    internal abstract fun createNewCycleSelectTemplateFragment(): CreateNewCycleSelectTemplateFragment

    @ContributesAndroidInjector(
        modules = [
            ViewModelBuilder::class
        ]
    )
    internal abstract fun createNewCycleNameFragment(): CreateNewCycleNameFragment

    @ContributesAndroidInjector(
        modules = [
            ViewModelBuilder::class
        ]
    )
    internal abstract fun createNewCycleRepRangeFragment(): CreateNewCycleRepRangeFragment

    @ContributesAndroidInjector(
        modules = [
            ViewModelBuilder::class
        ]
    )
    internal abstract fun createNewCycleDurationFragment(): CreateNewCycleDurationFragment

    @ContributesAndroidInjector(
        modules = [
            ViewModelBuilder::class
        ]
    )
    internal abstract fun createCreatingCycleActivity(): CreatingCycleActivity

    @Binds
    @IntoMap
    @ViewModelKey(CycleCreatorViewModel::class)
    @Singleton
    abstract fun bindViewModel(viewModel: CycleCreatorViewModel): ViewModel
}