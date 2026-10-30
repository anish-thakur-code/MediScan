package com.anish.mediscan

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFdaApiService {

    @GET("drug/label.json")
    fun searchDrugLabel(
        @Query("search") searchQuery: String,
        @Query("limit") limit: Int = 1
    ): Call<OpenFdaResponse>
}
