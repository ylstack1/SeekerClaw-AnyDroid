package com.seekerclaw.app.ui.skills

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object MarketplaceRepository {
    private const val BASE_URL = "https://clawhub.ai/api/v1"

    suspend fun searchSkills(query: String): Result<List<MarketplaceSkill>> = withContext(Dispatchers.IO) {
        runCatching {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "$BASE_URL/skills?q=$encodedQuery"
            val (status, body) = httpGet(url)
            if (status !in 200..299) {
                error("Marketplace search failed ($status)")
            }
            val responseObj = JSONObject(body)
            val arr = responseObj.optJSONArray("items") ?: JSONArray()
            val skills = mutableListOf<MarketplaceSkill>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                skills.add(parseSkill(obj))
            }
            skills
        }
    }

    suspend fun downloadSkill(downloadUrl: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val (status, body) = httpGet(downloadUrl)
            if (status !in 200..299) {
                error("Skill download failed ($status)")
            }
            body
        }
    }

    private fun parseSkill(obj: JSONObject): MarketplaceSkill {
        return MarketplaceSkill(
            id = obj.getString("slug"),
            name = obj.getString("displayName"),
            description = obj.optString("summary", ""),
            version = obj.optJSONObject("latestVersion")?.optString("version", "1.0.0") ?: "1.0.0",
            emoji = obj.optString("emoji", "🧩"),
            author = obj.optString("author", ""),
            imageUrl = obj.optString("imageUrl", ""),
            downloadUrl = obj.optString("downloadUrl", ""),
            triggers = obj.optJSONArray("triggers")?.let { arr ->
                List(arr.length()) { arr.getString(it) }
            } ?: emptyList(),
            requiresEnv = obj.optJSONArray("requiresEnv")?.let { arr ->
                List(arr.length()) { arr.getString(it) }
            } ?: emptyList(),
        )
    }

    private fun httpGet(url: String): Pair<Int, String> {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "SeekerClaw/Android")
        }

        return try {
            val status = conn.responseCode
            val stream = if (status in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            status to body
        } catch (e: Exception) {
            -1 to (e.message ?: "Unknown error")
        } finally {
            conn.disconnect()
        }
    }
}
