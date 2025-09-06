package com.musclehack.targetedHypertrophyTraining.blog

import androidx.lifecycle.ViewModel
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelBuilder
import com.musclehack.targetedHypertrophyTraining.core.dependencyInjection.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class BlogModule {

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    abstract fun blogFragment(): BlogFragment

    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    abstract fun confirmOpenNotificationLinkDialogFragment(): ConfirmOpenNotificationLinkDialogFragment

    @Binds
    @IntoMap
    @ViewModelKey(BlogViewModel::class)
    abstract fun bindViewModel(viewModel: BlogViewModel): ViewModel
}