package yos.music.player.data.libraries

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// Artist pictures, looked up once per artist and kept in the app's files:
// TheAudioDB for the wide picture, profile picture and logo, Deezer for a profile picture TheAudioDB lacks
object ArtistArt {
    // photo: a wide picture for banners; thumb: the square profile picture
    data class Art(val photo: String?, val thumb: String?, val logo: String?)

    // shortcut: TheAudioDB's public test key; a personal key belongs here if lookups start failing
    private const val API = "https://www.theaudiodb.com/api/v1/json/123/search.php?s="
    private const val DEEZER = "https://api.deezer.com/search/artist?limit=10&q="
    private val memory = HashMap<String, Art>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var prefetchJob: Job? = null

    private fun cacheFile(context: Context, artist: String) =
        File(File(context.filesDir, "artist_art4").apply { mkdirs() }, URLEncoder.encode(artist, "UTF-8").take(150) + ".json")

    suspend fun get(context: Context, artist: String): Art? = withContext(Dispatchers.IO) {
        memory[artist]?.let { return@withContext it }
        // caches of earlier versions
        listOf("artist_art", "artist_art2", "artist_art3").forEach { File(context.filesDir, it).deleteRecursively() }
        val file = cacheFile(context, artist)
        val cached = runCatching { if (file.exists()) parse(JSONObject(file.readText())) else null }.getOrNull()
        val art = cached ?: runCatching {
            // the search is loose ("LiSA" finds an Italian "Lisa"): keep only an exact name or alias
            val found = JSONObject(fetch(API + URLEncoder.encode(artist, "UTF-8"))).optJSONArray("artists")
                ?.optJSONObject(0)?.takeIf { o ->
                    o.optString("strArtist") == artist ||
                        o.optString("strArtistAlternate").split(",").any { it.trim() == artist }
                }
            val thumb = found?.let { pick(it, "strArtistThumb") } ?: deezerPicture(artist)
            val saved = JSONObject()
                .put("photo", found?.let { pick(it, "strArtistFanart", "strArtistWideThumb") } ?: "")
                .put("thumb", thumb ?: "")
                .put("logo", found?.let { pick(it, "strArtistLogo") } ?: "")
            // "not found" is saved too, so the services are asked only once per artist
            file.writeText(saved.toString())
            parse(saved)
        }.onFailure { android.util.Log.w("ArtistArt", "lookup failed: $artist", it) }
            .getOrNull() ?: return@withContext null // offline or rate-limited: try again next time
        memory[artist] = art
        art
    }

    // Deezer's search is loose too: same name ignoring case, the exact spelling first, else the most followed
    private fun deezerPicture(artist: String): String? {
        val results = JSONObject(fetch(DEEZER + URLEncoder.encode(artist, "UTF-8"))).optJSONArray("data") ?: return null
        val same = (0 until results.length()).map { results.getJSONObject(it) }
            .filter { it.optString("name").equals(artist, ignoreCase = true) }
        val best = same.firstOrNull { it.optString("name") == artist } ?: same.maxByOrNull { it.optInt("nb_fan") }
        // Deezer's placeholder for artists without a picture has an empty hash in its address
        return best?.optString("picture_xl")?.takeIf { it.startsWith("http") && "/artist//" !in it }
    }

    // keeps the picture in the app's files so it shows offline for good; the web address is the fallback
    suspend fun local(context: Context, url: String): String = withContext(Dispatchers.IO) {
        val name = Integer.toHexString(url.hashCode()) + "_" + url.substringAfterLast('/')
        val file = File(File(context.filesDir, "artist_images").apply { mkdirs() }, name)
        if (!file.exists()) runCatching {
            val tmp = File(file.path + ".part")
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 15000
            conn.inputStream.use { input -> tmp.outputStream().use { input.copyTo(it) } }
            conn.disconnect()
            tmp.renameTo(file)
        }
        if (file.exists()) android.net.Uri.fromFile(file).toString() else url
    }

    // fetches every artist's pictures in the background, so all of them show offline, not only those already seen
    fun prefetch(context: Context, artists: List<String>) {
        if (prefetchJob?.isActive == true) return
        val app = context.applicationContext
        prefetchJob = scope.launch {
            for (artist in artists) {
                val known = artist in memory || cacheFile(app, artist).exists()
                // TheAudioDB's test key allows only a few lookups a minute: space new ones, retry once a minute later,
                // and stop if that fails too (likely offline): the next app start picks up where this left off
                val art = get(app, artist) ?: run { delay(60_000); get(app, artist) } ?: break
                if (!known) delay(2_500)
                listOfNotNull(art.thumb, art.photo, art.logo).forEach { local(app, it) }
            }
        }
    }

    private fun fetch(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        return try { conn.inputStream.bufferedReader().use { it.readText() } } finally { conn.disconnect() }
    }

    private fun pick(o: JSONObject, vararg keys: String) =
        keys.firstNotNullOfOrNull { k -> o.optString(k).takeIf { it.startsWith("http") } }

    private fun parse(o: JSONObject) =
        Art(
            o.optString("photo").takeIf { it.isNotBlank() },
            o.optString("thumb").takeIf { it.isNotBlank() },
            o.optString("logo").takeIf { it.isNotBlank() }
        )
}
