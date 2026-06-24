package com.myanmarrussian.api

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Request/Response data classes - matching iOS API contract
 */
data class TutorRequest(
    @SerializedName("message") val message: String,
    @SerializedName("mode") val mode: String,
    @SerializedName("langMode") val langMode: String,
    @SerializedName("history") val history: List<HistoryItem>
)

data class HistoryItem(
    @SerializedName("role") val role: String,
    @SerializedName("text") val text: String
)

data class TutorResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("response") val response: String?,
    @SerializedName("timestamp") val timestamp: String?,
    @SerializedName("error") val error: String?
)

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("timestamp") val timestamp: String
)

// AI ကတ်ပြား (Vocabulary) အတွက် လိုအပ်သော Data Models များ
data class VocabularyResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("level") val level: String,
    @SerializedName("vocabulary") val vocabulary: List<VocabularyItem>,
    @SerializedName("count") val count: Int
)

data class VocabularyItem(
    @SerializedName("id") val id: String,
    @SerializedName("category") val category: String,
    @SerializedName("myanmar") val myanmar: String,
    @SerializedName("russian") val russian: String,
    @SerializedName("pronunciation") val pronunciation: String
)

/**
 * Retrofit API interface
 */
interface TutorApi {
    @POST("api/tutor")
    suspend fun sendMessage(
        @Body request: TutorRequest,
        @Header("x-gemini-api-key") apiKey: String? // 💡 Named argument ပိုမိုအဆင်ပြေစေရန် နောက်သို့ ရွှေ့ထားပါသည်
    ): Response<TutorResponse>

    @GET("api/health")
    suspend fun checkHealth(): Response<HealthResponse>

    // Level အလိုက် AI ထံမှ ကတ်ပြားများ တောင်းရန် API Function
    @GET("api/vocabulary")
    suspend fun getVocabulary(
        @Query("level") level: String,
        @Header("x-gemini-api-key") apiKey: String? // 💡 Flashcards တောင်းရာတွင်လည်း အဆင်ပြေအောင် နောက်ဆုံးတွင် ထားရှိပါသည်
    ): Response<VocabularyResponse>
}

/**
 * API Service factory - creates Retrofit instance with given base URL
 */
object TutorApiService {

    fun create(baseUrl: String): TutorApi {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Ensure base URL ends with /
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        return Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TutorApi::class.java)
    }
}
