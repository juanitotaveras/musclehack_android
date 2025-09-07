package com.musclehack.targetedHypertrophyTraining.workoutTracker.di

import androidx.lifecycle.ViewModel
import com.musclehack.targetedHypertrophyTraining.MainActivity
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelBuilder
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelKey
import com.musclehack.targetedHypertrophyTraining.workoutTracker.edit.ChangeRepRangeDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.edit.ChangeRestTimeDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.edit.ChangeSetExerciseDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.edit.ConfirmDeleteSetDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.edit.CreateNewSetFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.edit.EditSetsBottomSheetDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.edit.EditSetsFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.edit.EditSetsViewModel
import com.musclehack.targetedHypertrophyTraining.workoutTracker.home.ChangeCycleDurationDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.home.ConfirmDeleteCycleDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.home.RenameCycleDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.home.TrackerHomeBottomDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.home.TrackerHomeFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.home.TrackerHomeViewModel
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.ChangeLogExerciseDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.ChooseSubstitutionTypeDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.ConfirmSkipDayDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.EditLogNoteDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.LogPageBottomSheetDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.LogPageFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.PageTitleHelper
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.PreviousLogNoteDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.TimerService
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.TrainingPagerFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.TrainingViewModel
import com.musclehack.targetedHypertrophyTraining.workoutTracker.workouts.ConfirmDeleteWorkoutDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.workouts.CreateNewWorkoutFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.workouts.RenameWorkoutDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.workouts.TrackerWorkoutsBottomSheetDialogFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.workouts.TrackerWorkoutsFragment
import com.musclehack.targetedHypertrophyTraining.workoutTracker.workouts.TrackerWorkoutsViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class TrackerModule {

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun trackerHomeFragment(): TrackerHomeFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun trackerWorkoutsFragment(): TrackerWorkoutsFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun trainingPagerFragment(): TrainingPagerFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun logPageFragment(): LogPageFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun confirmDeleteCycleDialogFragment(): ConfirmDeleteCycleDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun changeCycleDurationDialogFragment(): ChangeCycleDurationDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun renameCycleDialogFragment(): RenameCycleDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun editLogNoteDialogFragment(): EditLogNoteDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun trackerHomeBottomDialogFragment(): TrackerHomeBottomDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun logPageBottomSheetDialogFragment(): LogPageBottomSheetDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun editSetsFragment(): EditSetsFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun editSetsBottomSheetDialogFragment(): EditSetsBottomSheetDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun trackerWorkoutsBottomSheetDialogFragment(): TrackerWorkoutsBottomSheetDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun changeLogExerciseDialogFragment(): ChangeLogExerciseDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun changeSetExerciseDialogFragment(): ChangeSetExerciseDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun changeRepRangeDialogFragment(): ChangeRepRangeDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun changeRestTimeDialogFragment(): ChangeRestTimeDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun confirmDeleteSetDialogFragment(): ConfirmDeleteSetDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun createNewSetFragment(): CreateNewSetFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun createNewWorkoutFragment(): CreateNewWorkoutFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun confirmDeleteWorkoutDialogFragment(): ConfirmDeleteWorkoutDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun renameWorkoutDialogFragment(): RenameWorkoutDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun chooseSubstitutionTypeDialogFragment(): ChooseSubstitutionTypeDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun confirmSkipDayDialogFragment(): ConfirmSkipDayDialogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun prevLogNoteDialogFragment(): PreviousLogNoteDialogFragment

    @ContributesAndroidInjector
    abstract fun provideTimerService(): TimerService

    abstract fun pageTitleHelper(): PageTitleHelper

    @ContributesAndroidInjector
    abstract fun mainActivity(): MainActivity

    @Binds
    @IntoMap
    @ViewModelKey(TrackerHomeViewModel::class)
    abstract fun bindViewModel(viewModel: TrackerHomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TrackerWorkoutsViewModel::class)
    abstract fun bindViewModel2(viewModel: TrackerWorkoutsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TrainingViewModel::class)
    abstract fun bindViewModel3(viewModel: TrainingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditSetsViewModel::class)
    abstract fun bindViewModel5(viewModel: EditSetsViewModel): ViewModel
}