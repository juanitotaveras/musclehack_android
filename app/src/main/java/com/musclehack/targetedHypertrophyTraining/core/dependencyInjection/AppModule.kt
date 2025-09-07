package com.musclehack.targetedHypertrophyTraining.core.dependencyInjection

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.musclehack.targetedHypertrophyTraining.data.repository.BlogRepository
import com.musclehack.targetedHypertrophyTraining.data.repository.BookRepository
import com.musclehack.targetedHypertrophyTraining.data.repository.CycleCreationRepository
import com.musclehack.targetedHypertrophyTraining.data.repository.DefaultBlogRepository
import com.musclehack.targetedHypertrophyTraining.data.repository.DefaultBookRepository
import com.musclehack.targetedHypertrophyTraining.data.repository.DefaultCycleCreationRepository
import com.musclehack.targetedHypertrophyTraining.data.repository.DefaultExerciseBankRepository
import com.musclehack.targetedHypertrophyTraining.data.repository.DefaultPagerContentsRepository
import com.musclehack.targetedHypertrophyTraining.data.repository.DefaultProductsRepository
import com.musclehack.targetedHypertrophyTraining.data.repository.DefaultTrackerRepository
import com.musclehack.targetedHypertrophyTraining.data.repository.ExerciseBankRepository
import com.musclehack.targetedHypertrophyTraining.data.repository.PagerContentsRepository
import com.musclehack.targetedHypertrophyTraining.data.repository.ProductsRepository
import com.musclehack.targetedHypertrophyTraining.data.repository.TrackerRepository
import com.musclehack.targetedHypertrophyTraining.data.source.BlogDataSource
import com.musclehack.targetedHypertrophyTraining.data.source.BookDataSource
import com.musclehack.targetedHypertrophyTraining.data.source.TrackerDataSource
import com.musclehack.targetedHypertrophyTraining.data.source.local.AppDatabase
import com.musclehack.targetedHypertrophyTraining.data.source.local.AssetsLocalDataSource
import com.musclehack.targetedHypertrophyTraining.data.source.local.TrackerLocalDataSource
import com.musclehack.targetedHypertrophyTraining.data.source.remote.BlogRemoteDataSource
import com.musclehack.targetedHypertrophyTraining.utilities.LegacyDatabaseHelper.CYCLES
import com.musclehack.targetedHypertrophyTraining.utilities.LegacyDatabaseHelper.LOGS
import com.musclehack.targetedHypertrophyTraining.utilities.LegacyDatabaseHelper.SETS
import com.musclehack.targetedHypertrophyTraining.utilities.LegacyDatabaseHelper.WORKOUTS
import com.musclehack.targetedHypertrophyTraining.workoutTracker.training.TimerService
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(includes = [ApplicationModuleBinds::class])
object AppModule {
    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class TrackerLocalDataSource

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class AssetsLocalDataSource

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class BlogRemoteDataSource

    @Singleton
    @TrackerLocalDataSource
    @Provides
    fun provideTrackerLocalDataSource(
        database: AppDatabase,
        ioDispatcher: CoroutineDispatcher
    ): TrackerDataSource {
        return TrackerLocalDataSource(
            database.cycleDao(),
            database.workoutDao(),
            database.setDao(),
            database.exerciseLogDao(),
            database.exerciseNoteDao(),
            database.userExerciseDao(),
            ioDispatcher,
            database
        )
    }

    @Singleton
    @AssetsLocalDataSource
    @Provides
    fun provideAssetsLocalDataSource(
        context: Context,
        ioDispatcher: CoroutineDispatcher
    ): BookDataSource {
        return AssetsLocalDataSource(context, ioDispatcher)
    }

    @Singleton
    @BlogRemoteDataSource
    @Provides
    fun provideBlogRemoteDataSource(database: AppDatabase, ioDispatcher: CoroutineDispatcher):
            BlogDataSource {
        return BlogRemoteDataSource(ioDispatcher)
    }

    private fun legacyToNewDbMigrate(db: SupportSQLiteDatabase) {
        val cyclesTemp = "${CYCLES.TABLE}_TEMP"
        val workoutsTemp = "${WORKOUTS.TABLE}_TEMP"
        val setsTemp = "${SETS.TABLE}_TEMP"
        val logsTemp = "${LOGS.TABLE}_TEMP"
        db.execSQL("ALTER TABLE ${CYCLES.TABLE} RENAME TO $cyclesTemp")
        db.execSQL("ALTER TABLE ${WORKOUTS.TABLE} RENAME TO $workoutsTemp")
        db.execSQL("ALTER TABLE ${SETS.TABLE} RENAME TO $setsTemp")
        db.execSQL("ALTER TABLE ${LOGS.TABLE} RENAME TO $logsTemp")
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS cycles (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, position INTEGER NOT NULL, dateCreated INTEGER NOT NULL, dateLastLogged INTEGER NOT NULL, numWeeks INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS workouts (name TEXT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, position INTEGER NOT NULL, repeats INTEGER NOT NULL, lastDayViewed INTEGER NOT NULL, lastSetViewed INTEGER NOT NULL, cycleId INTEGER NOT NULL, FOREIGN KEY(cycleId) REFERENCES cycles(id) ON UPDATE NO ACTION ON DELETE CASCADE )")
            db.execSQL("CREATE TABLE IF NOT EXISTS sets (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, position INTEGER NOT NULL, cycleId INTEGER NOT NULL, workoutId INTEGER NOT NULL, exerciseName TEXT NOT NULL, lowerReps INTEGER NOT NULL, higherReps INTEGER NOT NULL, restTime INTEGER NOT NULL, FOREIGN KEY(workoutId) REFERENCES workouts(id) ON UPDATE NO ACTION ON DELETE CASCADE )")
            db.execSQL("CREATE TABLE IF NOT EXISTS exerciseLogs (setId INTEGER NOT NULL, day INTEGER NOT NULL, logDate INTEGER, weight REAL, reps INTEGER, hasNote INTEGER NOT NULL, subName TEXT, skip INTEGER NOT NULL, PRIMARY KEY(setId, day), FOREIGN KEY(setId) REFERENCES sets(id) ON UPDATE NO ACTION ON DELETE CASCADE )")
            db.execSQL("CREATE TABLE IF NOT EXISTS exerciseNotes (setId INTEGER NOT NULL, day INTEGER NOT NULL, note TEXT NOT NULL, date INTEGER NOT NULL, PRIMARY KEY(setId, day), FOREIGN KEY(setId, day) REFERENCES exerciseLogs(setId, day) ON UPDATE NO ACTION ON DELETE CASCADE )")

            db.execSQL("INSERT INTO cycles (id, name, position, dateCreated, dateLastLogged, numWeeks) SELECT ${CYCLES.ID}, ${CYCLES.NAME}, ${CYCLES.POSITION}, ${CYCLES.DATE_CREATED}, ${CYCLES.LOG_DATE}, ${CYCLES.DURATION} FROM $cyclesTemp")
            db.execSQL("INSERT INTO workouts (name, id, cycleId, position, repeats, lastDayViewed, lastSetViewed) SELECT ${WORKOUTS.NAME}, ${WORKOUTS.ID}, ${WORKOUTS.CYCLE_ID}, ${WORKOUTS.POSITION}, ${WORKOUTS.REPEATS}, ${WORKOUTS.LAST_DAY_VIEWED}, ${WORKOUTS.LAST_SET_VIEWED} FROM $workoutsTemp")
            db.execSQL("INSERT INTO sets (id, position, cycleId, workoutId, exerciseName, lowerReps, higherReps, restTime) SELECT ${SETS.ID},${SETS.POSITION},${SETS.CYCLE_ID},${SETS.WORKOUT_ID},${SETS.EXERCISE_NAME},${SETS.LOWER_REPS},${SETS.HIGHER_REPS},${SETS.REST_TIME}  FROM $setsTemp")
            db.execSQL("INSERT INTO exerciseLogs (setId, day, logDate, weight, reps, subName, skip, hasNote) SELECT ${LOGS.SET_ID},${LOGS.DAY},${LOGS.DATE_LOGGED},${LOGS.WEIGHT},${LOGS.REPS},${LOGS.SUB_NAME},0, 0 FROM $logsTemp")
            db.execSQL("UPDATE exerciseLogs SET weight = NULL WHERE weight < 0")
            db.execSQL("UPDATE exerciseLogs SET reps = NULL WHERE reps < 0")
            db.execSQL("INSERT INTO exerciseNotes (setId, day, note, date) SELECT ${LOGS.SET_ID},${LOGS.DAY},${LOGS.NOTE},${LOGS.DATE_LOGGED} FROM ${LOGS.TABLE}_TEMP WHERE ${LOGS.NOTE} IS NOT NULL")
            db.execSQL("UPDATE exerciseLogs SET hasNote = 1 WHERE setId = (SELECT setId FROM exerciseNotes WHERE day = exerciseLogs.day)")
        } catch (e: Exception) {
            // We may have a violation of Foreign Key constraints.
            db.execSQL("DROP TABLE $cyclesTemp")
            db.execSQL("DROP TABLE $workoutsTemp")
            db.execSQL("DROP TABLE $setsTemp")
            db.execSQL("DROP TABLE $logsTemp")
        }
    }

    @Singleton
    @Provides
    fun provideDataBase(context: Context): AppDatabase {
        val MIGRATION_12_14 = object : Migration(12, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                legacyToNewDbMigrate(db)
            }
        }
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                legacyToNewDbMigrate(db)
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("CREATE TABLE IF NOT EXISTS userExercises (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL)")
                } catch (e: Exception) {
                    // idk what to do...
                }
            }
        }
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "tht.db"
        ).addMigrations(MIGRATION_12_14, MIGRATION_13_14, MIGRATION_14_15)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    @Singleton
    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    @Provides
    fun provideTimerService(context: Context): TimerService {
        return TimerService()
    }

}

@Module
abstract class ApplicationModuleBinds {

    @Singleton
    @Binds
    abstract fun bindRepository(repo: DefaultTrackerRepository): TrackerRepository

    @Singleton
    @Binds
    abstract fun bindRepository2(repo: DefaultCycleCreationRepository): CycleCreationRepository

    @Singleton
    @Binds
    abstract fun bindRepository3(repo: DefaultBookRepository): BookRepository

    @Singleton
    @Binds
    abstract fun bindRepository4(repo: DefaultProductsRepository): ProductsRepository

    @Singleton
    @Binds
    abstract fun bindRepository5(repo: DefaultBlogRepository): BlogRepository

    @Singleton
    @Binds
    abstract fun bindRepository6(repo: DefaultPagerContentsRepository): PagerContentsRepository

    @Singleton
    @Binds
    abstract fun bindRepository7(repo: DefaultExerciseBankRepository): ExerciseBankRepository
}