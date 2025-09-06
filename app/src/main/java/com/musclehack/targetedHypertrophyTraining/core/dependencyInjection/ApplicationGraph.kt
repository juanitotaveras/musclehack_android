package com.musclehack.targetedHypertrophyTraining.core.dependencyInjection

import android.content.Context
import com.musclehack.targetedHypertrophyTraining.App
import com.musclehack.targetedHypertrophyTraining.about.AboutModule
import com.musclehack.targetedHypertrophyTraining.blog.BlogModule
import com.musclehack.targetedHypertrophyTraining.book.BookModule
import com.musclehack.targetedHypertrophyTraining.databackup.BackupModule
import com.musclehack.targetedHypertrophyTraining.exerciseBank.ExerciseBankModule
import com.musclehack.targetedHypertrophyTraining.more.ContributeModule
import com.musclehack.targetedHypertrophyTraining.premium.ProductsModule
import com.musclehack.targetedHypertrophyTraining.testimonials.TestimonialsModule
import com.musclehack.targetedHypertrophyTraining.trainingCycleCreation.CycleCreatorModule
import com.musclehack.targetedHypertrophyTraining.workoutTracker.di.TrackerModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AboutModule::class,
        AppModule::class,
        AndroidSupportInjectionModule::class,
        BackupModule::class,
        ContributeModule::class,
        TrackerModule::class,
        BlogModule::class,
        BookModule::class,
        ExerciseBankModule::class,
        ProductsModule::class,
        CycleCreatorModule::class,
        TestimonialsModule::class,
        ViewModelBuilder::class
    ]
)
interface ApplicationGraph : AndroidInjector<App> {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): ApplicationGraph
    }

}