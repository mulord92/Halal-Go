package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class PlaceDetailsResponse(
    @Json(name = "result") val result: PlaceResult?,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class PlaceResult(
    @Json(name = "name") val name: String,
    @Json(name = "rating") val rating: Double?,
    @Json(name = "formatted_phone_number") val formatted_phone_number: String?
)

interface PlaceDetailsApiService {
    @GET("details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String,
        @Header("x-rapidapi-host") host: String,
        @Header("x-rapidapi-key") apiKey: String
    ): PlaceDetailsResponse
}

object PlaceDetailsApi {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://google-map-place-api.p.rapidapi.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val service: PlaceDetailsApiService = retrofit.create(PlaceDetailsApiService::class.java)
}
