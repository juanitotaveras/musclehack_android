package com.musclehack.targetedHypertrophyTraining.exerciseBank.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.musclehack.targetedHypertrophyTraining.WorkoutDataProto

/**
 * A user-created exercise to be inserted into the Exercise Bank.
 */
@Entity(tableName = "userExercises")
data class UserExercise(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
    val name: String
) {

    override fun toString() = name

    companion object {
        fun fromProto(proto: WorkoutDataProto.WorkoutData.UserExercise): UserExercise {
            return UserExercise(
                proto.id,
                proto.name
            )
        }
    }

    fun areContentsEqual(other: UserExercise) =
        id == other.id && name == other.name

    fun toProto(): WorkoutDataProto.WorkoutData.UserExercise {
        return WorkoutDataProto.WorkoutData.UserExercise.newBuilder()
            .setId(id)
            .setName(name)
            .build()
    }
}