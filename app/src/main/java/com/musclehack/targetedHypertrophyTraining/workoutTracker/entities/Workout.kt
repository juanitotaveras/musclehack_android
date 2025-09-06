/*
Author: Juanito Taveras
Created: 11/24/17
Modified: 11/24/17
 */

package com.musclehack.targetedHypertrophyTraining.workoutTracker.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.musclehack.targetedHypertrophyTraining.WorkoutDataProto

@Entity(
    tableName = "workouts", foreignKeys = [ForeignKey(
        entity = Cycle::class, parentColumns = ["id"],
        childColumns = ["cycleId"], onDelete = ForeignKey.CASCADE
    )]
)
data class Workout(
    var name: String,
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long,
    var position: Int,
    val repeats: Int,
    var lastDayViewed: Int,
    val lastSetViewed: Int,
    @ColumnInfo(name = "cycleId") var cycleId: Long
) {
    companion object {
        fun createDefault(name: String, frequency: Int, position: Int): Workout {
            return Workout(
                name = name, repeats = frequency, id = 0, position = position,
                lastDayViewed = -1, lastSetViewed = -1, cycleId = -1
            )
        }

        fun fromProto(proto: WorkoutDataProto.WorkoutData.Workout): Workout {
            return Workout(
                proto.name,
                proto.id,
                proto.position,
                proto.repeats,
                proto.lastDayViewed,
                proto.lastSetViewed,
                proto.cycleId
            )
        }
    }

    fun areContentsEqual(other: Workout): Boolean {
        return id == other.id && name == other.name && position == other.position
    }

    fun toProto(): WorkoutDataProto.WorkoutData.Workout {
        return WorkoutDataProto.WorkoutData.Workout.newBuilder()
            .setName(name)
            .setId(id)
            .setPosition(position)
            .setRepeats(repeats)
            .setLastDayViewed(lastDayViewed)
            .setLastSetViewed(lastSetViewed)
            .setCycleId(cycleId)
            .build()
    }
}