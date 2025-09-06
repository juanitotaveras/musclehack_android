package com.musclehack.targetedHypertrophyTraining.exerciseBank

import androidx.lifecycle.ViewModel
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelBuilder
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelKey
import com.musclehack.targetedHypertrophyTraining.workoutTracker.edit.ConfirmDeleteExerciseDialogFragment
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class ExerciseBankModule {
    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    abstract fun exerciseBankMainFragment(): ExerciseBankMainFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    abstract fun exerciseBankSelectionFragment(): ExerciseBankSelectionFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    abstract fun exerciseBankSelectionOverlayFragment(): ExerciseBankSelectionOverlayFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    abstract fun confirmDeleteExerciseDialogFragment(): ConfirmDeleteExerciseDialogFragment

    @Binds
    @IntoMap
    @ViewModelKey(ExerciseBankMainViewModel::class)
    abstract fun bindViewModel(viewModel: ExerciseBankMainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ExerciseBankSelectionViewModel::class)
    abstract fun bindViewModel2(viewModel: ExerciseBankSelectionViewModel): ViewModel
}