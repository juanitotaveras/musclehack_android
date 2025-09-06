package com.musclehack.targetedHypertrophyTraining.workoutTracker.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import com.musclehack.targetedHypertrophyTraining.WorkoutDataProto

/**
 * Created by juanito on 12/8/2017.
 */

/* Ms since Epoch that log was last modified */
/* Recorded weight */
/* Recorded repetitions */
/* Log note, maximum of X characters */
/* Will be other than -1 if we want to substitute this log with an exercise id. */

@Entity(
    tableName = "exerciseLogs", primaryKeys = ["setId", "day"],
    foreignKeys = [ForeignKey(
        entity = ExerciseSet::class,
        parentColumns = ["id"],
        childColumns = ["setId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ExerciseLog(
    var setId: Long,
    var day: Int,
    var logDate: Long?,
    var weight: Double?,
    var reps: Int?,
    var hasNote: Boolean,
    var subName: String?,
    var skip: Boolean
) {
    companion object {
        // TODO: SetID is a required argument
        fun createDefault(setId: Long, day: Int): ExerciseLog {
            // logDate should be time object was first saved to DB
            return ExerciseLog(
                setId, day, null, null, null,
                false, null, false
            )
        }

        fun fromProto(proto: WorkoutDataProto.WorkoutData.ExerciseLog): ExerciseLog {
            return ExerciseLog(
                proto.setId,
                proto.day,
                if (proto.hasLogDate()) proto.logDate else null,
                if (proto.hasWeight()) proto.weight else null,
                if (proto.hasReps()) proto.reps else null,
                proto.hasNote,
                if (proto.hasSubName()) proto.subName else null,
                proto.skip
            )
        }
    }

    fun hasEqualContents(other: ExerciseLog): Boolean {
        return hasNote == other.hasNote && weight == other.weight
                && reps == other.reps && skip == other.skip && subName == other.subName
    }

    fun toProto(): WorkoutDataProto.WorkoutData.ExerciseLog {
        val proto = WorkoutDataProto.WorkoutData.ExerciseLog.newBuilder()
            .setSetId(setId)
            .setDay(day)
            .setHasNote(hasNote)
            .setSkip(skip)
        logDate?.let { proto.setLogDate(it) }
        weight?.let { proto.setWeight(it) }
        reps?.let { proto.setReps(it) }
        subName?.let { proto.setSubName(it) }
        return proto.build()
    }
}
