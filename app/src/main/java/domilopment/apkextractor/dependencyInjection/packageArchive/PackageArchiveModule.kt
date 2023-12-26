package domilopment.apkextractor.dependencyInjection.packageArchive

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import domilopment.apkextractor.dependencyInjection.preferenceDataStore.PreferenceRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class PackageArchiveModule {
    @Binds
    @Singleton
    abstract fun bindPackageArchiveRepository(
        myPreferencesRepository: MyPackageArchiveRepository
    ): PackageArchiveRepository

    companion object {
        @Singleton
        @Provides
        fun provideApplicationDataSource(
            @ApplicationContext context: Context, preferenceRepository: PreferenceRepository
        ): ListOfAPKs {
            return ListOfAPKs.getPackageArchives(context, preferenceRepository)
        }
    }
}