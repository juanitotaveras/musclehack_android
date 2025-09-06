package com.musclehack.targetedHypertrophyTraining.workoutTracker.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import com.musclehack.targetedHypertrophyTraining.WorkoutDataProto

@Entity(
    tableName = "exerciseNotes", primaryKeys = ["setId", "day"],
    foreignKeys = [ForeignKey(
        entity = ExerciseLog::class,
        parentColumns = arrayOf("setId", "day"),
        childColumns = arrayOf("setId", "day"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class ExerciseNote(
    val setId: Long,
    val day: Int,
    val note: String,
    val date: Long
) {
    companion object {
        fun fromProto(proto: WorkoutDataProto.WorkoutData.ExerciseNote): ExerciseNote {
            return ExerciseNote(
                proto.setId,
                proto.day,
                proto.note,
                proto.date
            )
        }
    }

    fun toProto(): WorkoutDataProto.WorkoutData.ExerciseNote {
        return WorkoutDataProto.WorkoutData.ExerciseNote.newBuilder()
            .setSetId(setId)
            .setDay(day)
            .setNote(note)
            .setDate(date)
            .build()
    }
}