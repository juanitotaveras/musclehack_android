package com.musclehack.targetedHypertrophyTraining.trainingCycleCreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclehack.targetedHypertrophyTraining.data.repository.CycleCreationRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import javax.inject.Inject

enum class CycleTemplates0 {
    Blank, FiveDaySplit, ThreeDayFull, ThreeDaySplit
}

class CycleCreatorViewModel @Inject constructor(private val creationRepository: CycleCreationRepository) :
    ViewModel() {
    var templateSelection: CycleTemplates0? = null
    var lowerReps: Int? = null
    var higherReps: Int? = null
    var newCycleNumWeeks: Int? = null
    var newCycleName: String? = null

    fun create(): Deferred<Unit> {
        if (newCycleName != null && newCycleNumWeeks != null && lowerReps != null
            && higherReps != null
        ) {
            val cycleName: String = if (newCycleName.isNullOrEmpty()) "Untitled" else newCycleName!!
            val maxReps = higherReps ?: 12
            val minReps = lowerReps ?: 8
            val numWeeks = newCycleNumWeeks ?: 10
            when (templateSelection) {
                CycleTemplates0.ThreeDayFull -> {
                    return viewModelScope.async {
                        creationRepository.createThreeDayFull(
                            higherReps = maxReps,
                            lowerReps = minReps, newCycleDuration = numWeeks,
                            newCycleName = cycleName
                        )
                    }
                }

                CycleTemplates0.FiveDaySplit -> {
                    return viewModelScope.async {
                        creationRepository.createFiveDaySplit(
                            higherReps = maxReps,
                            lowerReps = minReps, newCycleDuration = numWeeks,
                            newCycleName = cycleName
                        )

                    }
                }

                CycleTemplates0.ThreeDaySplit -> {
                    return viewModelScope.async {
                        creationRepository.createThreeDaySplit(
                            higherReps = maxReps,
                            lowerReps = minReps, newCycleDuration = numWeeks,
                            newCycleName = cycleName
                        )

                    }
                }

                else -> {
                    //TODO: Log error!
                }
            }

        } else if (newCycleName != null && newCycleNumWeeks != null) {
            // create blank cycle
            if (templateSelection == CycleTemplates0.Blank) {
                val cycleName: String =
                    if (newCycleName.isNullOrEmpty()) "Untitled" else newCycleName!!
                return viewModelScope.async {
                    creationRepository.createBlank(
                        newCycleName = cycleName,
                        numWeeks = newCycleNumWeeks!!
                    )
                }
            }
        }
        return viewModelScope.async {}
    }
}