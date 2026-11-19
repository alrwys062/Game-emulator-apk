package com.example.service

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class DeviceSpecSummary(
    val cpuCores: Int,
    val totalRamMb: Long,
    val modelName: String,
    val isLowEndDevice: Boolean
) {
    companion object {
        fun inspect(context: Context): DeviceSpecSummary {
            val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am?.getMemoryInfo(mi)
            val ramMb = (mi.totalMem / (1024L * 1024L)).coerceAtLeast(1024L)
            val model = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
            val lowEnd = cores <= 4 || ramMb < 3500L
            return DeviceSpecSummary(cores, ramMb, model, lowEnd)
        }
    }
}

data class AiPerfRecommendation(
    val summaryText: String,
    val frameSkip: Int,
    val resolutionScale: Int,
    val renderScalePercent: Int,
    val fpsCap: Int,
    val threadsMode: String
)

object GeminiAssistantService {

    val supportedModels = listOf(
        "gemini-2.5-flash",
        "gemini-2.0-flash",
        "gemini-2.5-pro"
    )

    suspend fun callGemini(
        apiKey: String,
        model: String,
        prompt: String,
        isArabic: Boolean
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException(
                    if (isArabic) "يرجى إدخال مفتاح Gemini API في الإعدادات أولاً (يُحفظ محلياً فقط)."
                    else "Please enter your Gemini API key in Settings first (saved locally only)."
                )
            )
        }

        runCatching {
            val cleanModel = model.trim().ifEmpty { "gemini-2.5-flash" }
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$cleanModel:generateContent?key=${apiKey.trim()}"
            val bodyJson = JSONObject().apply {
                put(
                    "contents",
                    JSONArray().put(
                        JSONObject().put(
                            "parts",
                            JSONArray().put(JSONObject().put("text", prompt))
                        )
                    )
                )
            }

            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 30_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(bodyJson.toString())
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            conn.disconnect()

            if (code !in 200..299) {
                val errMsg = runCatching {
                    JSONObject(responseText).optJSONObject("error")?.optString("message")
                }.getOrNull() ?: "HTTP $code"
                throw IllegalStateException(errMsg)
            }

            val root = JSONObject(responseText)
            val text = root
                .optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")
                ?.trim()

            if (text.isNullOrBlank()) {
                throw IllegalStateException(if (isArabic) "لم يرد نص من Gemini." else "Empty response from Gemini.")
            }
            text
        }
    }

    suspend fun diagnoseErrorLog(
        apiKey: String,
        model: String,
        errorDetail: String,
        logs: String,
        gameTitle: String,
        systemName: String,
        device: DeviceSpecSummary,
        isArabic: Boolean
    ): Result<String> {
        val langInstr = if (isArabic) "أجب بالعربية بشكل مختصر وعملي ومرقّم:" else "Answer concisely in English with numbered steps:"
        val prompt = """
            You are an expert EmulatorJS & RetroArch Android WebView troubleshooter.
            Game: $gameTitle ($systemName)
            Device: ${device.modelName}, ${device.cpuCores} CPU cores, ${device.totalRamMb} MB RAM
            Error code: $errorDetail
            Console logs:
            ${logs.takeLast(1800)}

            $langInstr
            Explain the exact likely cause and concrete steps the user can click inside RetroVerse (e.g. Switch CDN, Disable Threads, Upload BIOS, Check internet, Use .zip/.chd).
        """.trimIndent()
        return callGemini(apiKey, model, prompt, isArabic)
    }

    suspend fun recommendPerformance(
        apiKey: String,
        model: String,
        systemName: String,
        gameTitle: String,
        device: DeviceSpecSummary,
        isArabic: Boolean
    ): Result<AiPerfRecommendation> {
        val langNote = if (isArabic) "اكتب summary بالعربية." else "Write summary in English."
        val prompt = """
            Recommend optimal EmulatorJS settings for:
            Game: $gameTitle ($systemName)
            Device: ${device.modelName}, ${device.cpuCores} cores, ${device.totalRamMb} MB RAM.
            Return ONLY valid JSON with keys:
            {"summary":"...", "frameSkip":0..3, "resolutionScale":1..3, "renderScalePercent":50|75|100, "fpsCap":30|60, "threadsMode":"auto"|"on"|"off"}
            $langNote
        """.trimIndent()

        return callGemini(apiKey, model, prompt, isArabic).map { raw ->
            val jsonBlock = raw
                .substringAfter("{", "")
                .let { if (it.isNotEmpty()) "{${it.substringBeforeLast("}")}}" else "{}" }
            val obj = runCatching { JSONObject(jsonBlock) }.getOrElse { JSONObject() }
            AiPerfRecommendation(
                summaryText = obj.optString("summary", raw),
                frameSkip = obj.optInt("frameSkip", if (device.isLowEndDevice) 1 else 0).coerceIn(0, 5),
                resolutionScale = obj.optInt("resolutionScale", 1).coerceIn(1, 4),
                renderScalePercent = obj.optInt("renderScalePercent", if (device.isLowEndDevice) 75 else 100).coerceIn(50, 100),
                fpsCap = obj.optInt("fpsCap", 60).let { if (it <= 35) 30 else 60 },
                threadsMode = obj.optString("threadsMode", "auto").let { if (it in listOf("auto", "on", "off")) it else "auto" }
            )
        }
    }

    suspend fun fetchGameInfo(
        apiKey: String,
        model: String,
        gameTitle: String,
        systemName: String,
        isArabic: Boolean
    ): Result<String> {
        val prompt = if (isArabic) {
            "اكتب نبذة مختصرة وممتعة باللغة العربية عن لعبة \"$gameTitle\" على جهاز $systemName: سنة الإصدار، التصنيف، أسلوب اللعب، ونصيحة سريعة للاعب."
        } else {
            "Write a concise summary about the classic game \"$gameTitle\" for $systemName: release year, genre, gameplay overview, and one quick tip."
        }
        return callGemini(apiKey, model, prompt, isArabic)
    }

    suspend fun suggestOfficialCoverTitle(
        apiKey: String,
        model: String,
        fileName: String,
        systemName: String,
        isArabic: Boolean
    ): Result<String> {
        val prompt = """
            Given a ROM filename "$fileName" for $systemName, reply with ONLY the exact official Libretro / No-Intro English boxart title (without file extension, e.g. "Super Mario Advance 2 - Super Mario World (USA)"). No explanation, just one line.
        """.trimIndent()
        return callGemini(apiKey, model, prompt, isArabic).map { it.lines().firstOrNull()?.trim().orEmpty() }
    }
}
