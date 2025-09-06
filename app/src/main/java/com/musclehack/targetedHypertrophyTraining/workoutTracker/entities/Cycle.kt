package com.musclehack.targetedHypertrophyTraining.workoutTracker.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.musclehack.targetedHypertrophyTraining.WorkoutDataProto
import java.util.*

/**
 * Author: Juanito Taveras
 * Created: 11/24/17
 * Modified: 11/24/17 11/25/17 12/26/17
 */

@Entity(tableName = "cycles")
data class Cycle(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id")
    val id: Long,
    var name: String,
    var position: Int,
    val dateCreated: Long,
    var dateLastLogged: Long,
    var numWeeks: Int
) {

    override fun toString() = name

    companion object {
        fun createDefault(name: String, numWeeks: Int): Cycle {
            return Cycle(/*Arbitrary default*/id = 0,
                name = name,
                position = 0,
                dateCreated = Calendar.getInstance().timeInMillis,
                dateLastLogged = 0,
                numWeeks = numWeeks
            )
        }

        fun fromProto(proto: WorkoutDataProto.WorkoutData.Cycle): Cycle {
            return Cycle(
                proto.id,
                proto.name,
                proto.position,
                proto.dateCreated,
                proto.dateLastLogged,
                proto.numWeeks
            )
        }
    }

    fun areContentsEqual(other: Cycle): Boolean =
        id == other.id && name == other.name &&
                dateCreated == other.dateCreated && dateLastLogged == other.dateLastLogged

    fun toProto(): WorkoutDataProto.WorkoutData.Cycle {
        return WorkoutDataProto.WorkoutData.Cycle.newBuilder()
            .setId(id)
            .setName(name)
            .setPosition(position)
            .setDateCreated(dateCreated)
            .setDateLastLogged(dateLastLogged)
            .setNumWeeks(numWeeks)
            .build()
    }
}

typealias CycleMap = HashMap<Cycle, List<WorkoutMap>>

typealias WorkoutMap = HashMap<Workout, List<ExerciseSet>>

typealias TrainingPagerData = Pair<Cycle, Workout>

