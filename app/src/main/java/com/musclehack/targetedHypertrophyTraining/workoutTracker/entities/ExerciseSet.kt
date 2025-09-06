package com.musclehack.targetedHypertrophyTraining.workoutTracker.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.musclehack.targetedHypertrophyTraining.WorkoutDataProto

/**
 *
 * Created by juanito on 12/9/2017.
 * Note: The set object should be able to have logs inside of it.
 */

@Entity(
    tableName = "sets", foreignKeys = [ForeignKey(
        entity = Workout::class, parentColumns = ["id"],
        childColumns = ["workoutId"], onDelete = ForeignKey.CASCADE
    )]
)
data class ExerciseSet(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
    var position: Int,
    var cycleId: Long,
    var workoutId: Long,
    var exerciseName: String,
    var lowerReps: Int,
    var higherReps: Int, var restTime: Int
) {
    override fun toString() = id.toString()

    companion object {
        fun createDefault(
            exerciseName: String,
            lowerReps: Int,
            higherReps: Int,
            restTime: Int
        ): ExerciseSet {
            return ExerciseSet(
                exerciseName = exerciseName,
                lowerReps = lowerReps,
                higherReps = higherReps,
                restTime = restTime,
                id = 0,
                position = -1,
                cycleId = -1,
                workoutId = -1
            )
        }

        fun fromProto(proto: WorkoutDataProto.WorkoutData.ExerciseSet): ExerciseSet {
            return ExerciseSet(
                proto.id,
                proto.position,
                proto.cycleId,
                proto.workoutId,
                proto.exerciseName,
                proto.lowerReps,
                proto.higherReps,
                proto.restTime
            )
        }
    }

    fun hasEqualContents(other: ExerciseSet): Boolean {
        return exerciseName == other.exerciseName && lowerReps == other.lowerReps &&
                higherReps == other.higherReps && restTime == other.restTime
                && position == other.position
    }

    fun toProto(): WorkoutDataProto.WorkoutData.ExerciseSet {
        return WorkoutDataProto.WorkoutData.ExerciseSet.newBuilder()
            .setId(id)
            .setPosition(position)
            .setCycleId(cycleId)
            .setWorkoutId(workoutId)
            .setExerciseName(exerciseName)
            .setLowerReps(lowerReps)
            .setHigherReps(higherReps)
            .setRestTime(restTime)
            .build()
    }

    /** Used to populate set_list_item in SetsAdapter */
    fun getExercisePositionText() = "${position + 1}."

    /** Used by BottomSheet in Edit Sets. [would be nice to move this to a presenter.] */
    fun getName() = "${position + 1}. $exerciseName"

    fun getLowerRepsText() = lowerReps.toString()
    fun getHigherRepsText() = higherReps.toString()
    fun getRestTimeText() = restTime.toString()
}

