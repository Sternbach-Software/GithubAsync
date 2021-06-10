package shmuly.networking

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.*
import java.util.prefs.Preferences

fun prefNode(): Preferences = Preferences.userRoot().node("Notification")

fun loadStoredParams(): Int = prefNode().get("interval", "10").toInt()

fun removeStoredParams() = prefNode().removeNode()

fun saveParams(interval: Int) {
    prefNode().apply {
        put("interval", interval.toString())
        sync()
    }
}

suspend fun getLatestCommit(
    service: GitHubService
) = coroutineScope {
    service.getLatestCommit().body() ?: throw IllegalStateException("getLatestCommit().body() returned null")
}

suspend fun getListOfFileContents(
    service: GitHubService
)  = coroutineScope {
    val files = service.getFiles().body() ?: listOf()

    val channel = Channel<Pair<String,String,>>()
    for (file in files) {
        launch {
            if(file.name.contains(".txt")){
                val fileContent = service
                    .getFileContent(file.name)
                val content = fileContent
                    .body()
                    .let{
                        it ?: println("error=${fileContent.errorBody()?.string()}, it=$it")
                        String(
                            Base64
                                .getMimeDecoder()
                                .decode(
                                    it
                                        ?.content
                                        ?.toByteArray(Charsets.UTF_8),
                                )
                        )
                    }
                channel.send(Pair(file.name,content))
            }
        }
    }
//    var allUsers = emptyList<User>()
    val fileContents = mutableListOf<Pair<String,String>>()
    repeat(files.size) {
        val fileContent = channel.receive()
        println("fileContent recieved: $fileContent")
        fileContents.add(fileContent)
    }
    fileContents
}