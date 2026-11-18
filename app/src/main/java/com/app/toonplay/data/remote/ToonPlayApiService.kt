package com.app.toonplay.data.remote

import retrofit2.http.GET

interface ToonPlayApiService {
  @GET("api/home")
  suspend fun getHomeFeed(): HomeApiResponse
}
