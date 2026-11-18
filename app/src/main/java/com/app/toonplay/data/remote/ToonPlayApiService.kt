
package com.app.toonplay.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ToonPlayApiService {

  companion object {
    const val ANIMESALT_BASE_URL = "https://animesalt.streamindia.co.in/"
    const val MOVIEBOX_BASE_URL = "https://moviebox.streamindia.co.in/"
  }
  @GET("api/home")
  suspend fun getHomeFeed(): HomeApiResponse

  @GET("api/search")
  suspend fun searchAnimeSalt(
    @Query("q") query: String,
    @Query("page") page: Int = 1,
  ): AnimeSaltSearchResponseDto

  @GET("${MOVIEBOX_BASE_URL}api/search")
  suspend fun searchMoviebox(
    @Query("q") query: String,
    @Query("page") page: Int = 1,
  ): MovieboxSearchResponseDto

  @GET("api/info")
  suspend fun getAnimeSaltInfo(
    @Query("id") id: String,
    @Query("anilistid") anilistId: Int? = null,
  ): AnimeInfoApiResponseDto

  @GET("${MOVIEBOX_BASE_URL}api/info")
  suspend fun getMovieboxInfo(
    @Query("detailPath") detailPath: String,
    @Query("subjectId") subjectId: String? = null,
  ): MovieboxInfoApiResponseDto
}