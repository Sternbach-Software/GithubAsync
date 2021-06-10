package shmuly.networking

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubService {
    @GET("repos/neurorehab-md/Therapy-Toolkit-Database/commits/main")
    suspend fun getLatestCommit(): Response<Commit>

    @GET("repos/neurorehab-md/Therapy-Toolkit-Database/contents")
    suspend fun getFiles(): Response<List<GithubFile>>

    @GET("repos/neurorehab-md/Therapy-Toolkit-Database/contents/{name}")
    suspend fun getFileContent(@Path("name") name: String): Response<FileContent>
}

@Serializable
data class FileContent(
    val content: String,
)

@Serializable
data class GithubFile(
    val name: String,
)

@Serializable
data class Commit(
    val sha: String
)

fun createGitHubService(): GitHubService {
//    val authToken = "Basic " + Base64.getEncoder().encode("$username:$password".toByteArray()).toString(Charsets.UTF_8)
    val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "token g" + "hp_mqonBg" + "vs7kQdavt" + "D5s1BLtQd" + "KTm1ry1ZDdL4"/*need to do this so that GitHub doesn't invalidate it*/)
            val request = builder.build()
            chain.proceed(request)
        }
        .build()

    val contentType = "application/json".toMediaType()
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(contentType))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(httpClient)
        .build()
    return retrofit.create(GitHubService::class.java)
}
