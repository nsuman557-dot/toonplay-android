package com.app.toonplay

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.app.toonplay.data.remote.ToonPlayApiService
import com.app.toonplay.data.repository.AnimeHomeRepository
import com.app.toonplay.data.repository.UserPreferencesRepository
import com.squareup.moshi.Moshi
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ToonPlayApplication : Application(), ImageLoaderFactory {

  lateinit var homeRepository: AnimeHomeRepository
    private set

  lateinit var preferencesRepository: UserPreferencesRepository
    private set

  private lateinit var okHttpClient: OkHttpClient

  override fun onCreate() {
    super.onCreate()

    val httpCacheDir = File(cacheDir, "http_cache")
    okHttpClient =
      OkHttpClient.Builder()
        .cache(Cache(httpCacheDir, 20L * 1024L * 1024L))
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
          val req =
            chain
              .request()
              .newBuilder()
              .header("Accept", "application/json")
              .header("User-Agent", "ToonPlay-Android/1.0")
              .build()
          chain.proceed(req)
        }
        .build()

    val moshi = Moshi.Builder().build()

    val retrofit =
      Retrofit.Builder()
        .baseUrl("https://animesalt.streamindia.co.in/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService = retrofit.create(ToonPlayApiService::class.java)
    homeRepository = AnimeHomeRepository(apiService)
    preferencesRepository = UserPreferencesRepository(applicationContext)
  }

  override fun newImageLoader(): ImageLoader {
    return ImageLoader.Builder(this)
      .okHttpClient(okHttpClient)
      .memoryCachePolicy(CachePolicy.ENABLED)
      .diskCachePolicy(CachePolicy.ENABLED)
      .memoryCache {
        MemoryCache.Builder(this)
          .maxSizePercent(0.25)
          .build()
      }
      .diskCache {
        DiskCache.Builder()
          .directory(File(cacheDir, "coil_image_cache"))
          .maxSizeBytes(120L * 1024L * 1024L)
          .build()
      }
      .respectCacheHeaders(false)
      .crossfade(false)
      .allowHardware(true)
      .build()
  }
}
