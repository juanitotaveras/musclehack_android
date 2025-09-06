package com.musclehack.targetedHypertrophyTraining.databackup

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BackupModule {

    @ContributesAndroidInjector
    abstract fun backupFragment(): BackupFragment
}
