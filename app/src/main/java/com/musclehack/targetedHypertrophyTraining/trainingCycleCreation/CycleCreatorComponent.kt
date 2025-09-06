package com.musclehack.targetedHypertrophyTraining.trainingCycleCreation

import dagger.Subcomponent

@Subcomponent(modules = [CycleCreatorModule::class])
interface CycleCreatorComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): CycleCreatorComponent
    }

    fun inject(fragment: CreateNewCycleSelectTemplateFragment)
    fun inject(fragment: CreateNewCycleNameFragment)
    fun inject(fragment: CreateNewCycleDurationFragment)
    fun inject(fragment: CreateNewCycleRepRangeFragment)
    fun inject(activity: CreatingCycleActivity)
}