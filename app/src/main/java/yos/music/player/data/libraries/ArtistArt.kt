package yos.music.player.data.libraries

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// Artist photo and logo from TheAudioDB, looked up once per artist and kept in the app's files
object ArtistArt {
    data class Art(val photo: String?, val logo: String?)

    // shortcut: TheAudioDB's public test key; a personal key belongs here if lookups start failing
    private const val API = "https://www.theaudiodb.com/api/v1/json/123/search.php?s="
    private val memory = HashMap<String, Art>()

    suspend fun get(context: Context, artist: String): Art? = withContext(Dispatchers.IO) {
        memory[artist]?.let { return@withContext it }
        val dir = File(context.filesDir, "artist_art").apply { mkdirs() }
        val file = File(dir, URLEncoder.encode(artist, "UTF-8").take(150) + ".json")
        val cached = runCatching { if (file.exists()) parse(JSONObject(file.readText())) else null }.getOrNull()
        val art = cached ?: runCatching {
            val conn = URL(API + URLEncoder.encode(artist, "UTF-8")).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val found = JSONObject(body).optJSONArray("artists")?.optJSONObject(0)
            val saved = JSONObject()
                .put("photo", found?.let { pick(it, "strArtistFanart", "strArtistWideThumb", "strArtistThumb") } ?: "")
                .put("logo", found?.let { pick(it, "strArtistLogo") } ?: "")
            // "not found" is saved too, so the service is asked only once per artist
            file.writeText(saved.toString())
            parse(saved)
        }.getOrNull() ?: return@withContext null // offline: try again next time
        memory[artist] = art
        art
    }

    private fun pick(o: JSONObject, vararg keys: String) =
        keys.firstNotNullOfOrNull { k -> o.optString(k).takeIf { it.startsWith("http") } }

    private fun parse(o: JSONObject) =
        Art(o.optString("photo").takeIf { it.isNotBlank() }, o.optString("logo").takeIf { it.isNotBlank() })
}
