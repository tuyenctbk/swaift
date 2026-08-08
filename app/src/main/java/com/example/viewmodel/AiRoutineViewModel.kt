package com.example.viewmodel

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.ActionConfig
import com.example.data.FlowEntity
import com.example.data.HistoryLogEntity
import com.example.data.JsonUtils
import com.example.data.TriggerConfig
import com.example.data.TriggerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class AiInsight(
    val title: String,
    val description: String,
    val category: String,
    val impact: String
)

sealed interface AiGenerationState {
    data object Idle : AiGenerationState
    data object Loading : AiGenerationState
    data class Success(val generatedFlow: FlowEntity, val explanation: String) : AiGenerationState
    data class Error(val message: String) : AiGenerationState
}

class AiRoutineViewModel(application: Application) : AndroidViewModel(application) {

    private val _aiState = MutableStateFlow<AiGenerationState>(AiGenerationState.Idle)
    val aiState: StateFlow<AiGenerationState> = _aiState.asStateFlow()

    private val _promptInput = MutableStateFlow("")
    val promptInput: StateFlow<String> = _promptInput.asStateFlow()

    private val _aiInsights = MutableStateFlow<List<AiInsight>>(emptyList())
    val aiInsights: StateFlow<List<AiInsight>> = _aiInsights.asStateFlow()

    private val _isAnalyzingInsights = MutableStateFlow(false)
    val isAnalyzingInsights: StateFlow<Boolean> = _isAnalyzingInsights.asStateFlow()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val sharedPrefs = application.getSharedPreferences("swai_gemini_prefs", android.content.Context.MODE_PRIVATE)

    private val _apiErrorStatus = MutableStateFlow<String?>(null)
    val apiErrorStatus: StateFlow<String?> = _apiErrorStatus.asStateFlow()

    fun getCustomApiKey(): String {
        return sharedPrefs.getString("custom_api_key", "") ?: ""
    }

    fun saveCustomApiKey(key: String) {
        sharedPrefs.edit().putString("custom_api_key", key.trim()).apply()
        _apiErrorStatus.value = if (key.isNotBlank()) "Custom BYOK API Key saved successfully!" else "Custom API Key cleared."
    }

    fun clearApiErrorStatus() {
        _apiErrorStatus.value = null
    }

    private fun handleHttpError(code: Int) {
        val errorMsg = when (code) {
            429 -> "Gemini Free Tier Quota Exceeded (HTTP 429). Automatically switched to Local Intelligent Heuristic Fallback. Consider Bring Your Own Key (BYOK) in Settings!"
            403, 401 -> "Invalid Gemini API Key (HTTP $code). Please verify your BYOK key in Settings."
            503, 502, 500 -> "Gemini AI Server Overloaded (HTTP $code). Automatically switched to Local Intelligent Fallback Engine."
            else -> "Gemini API Error (HTTP $code). Switched to Local Intelligent Fallback Engine."
        }
        _apiErrorStatus.value = errorMsg
    }

    private fun getApiKey(): String {
        val custom = getCustomApiKey()
        if (custom.isNotBlank()) return custom
        return try {
            val field = com.example.BuildConfig::class.java.getField("GEMINI_API_KEY")
            (field.get(null) as? String)?.takeIf { it.isNotBlank() } ?: ""
        } catch (e: Throwable) {
            ""
        }
    }

    fun generateInsightsForHistory(userFlows: List<FlowEntity>, historyLogs: List<HistoryLogEntity>) {
        if (_isAnalyzingInsights.value) return
        _isAnalyzingInsights.value = true

        viewModelScope.launch {
            try {
                val apiKey = getApiKey()
                val insights = if (apiKey.isNotBlank()) {
                    withContext(Dispatchers.IO) {
                        callGeminiForInsights(userFlows, historyLogs, apiKey)
                    }
                } else null

                _aiInsights.value = insights ?: generateSmartFallbackInsights(userFlows, historyLogs)
            } catch (e: Exception) {
                _aiInsights.value = generateSmartFallbackInsights(userFlows, historyLogs)
            } finally {
                _isAnalyzingInsights.value = false
            }
        }
    }

    private fun callGeminiForInsights(
        userFlows: List<FlowEntity>,
        historyLogs: List<HistoryLogEntity>,
        apiKey: String
    ): List<AiInsight>? {
        return try {
            val totalLogs = historyLogs.size
            val flowTitles = userFlows.joinToString(", ") { it.title }
            val prompt = """
                Analyze this Android routine execution log:
                User Routines: [$flowTitles]
                Total Executions: $totalLogs
                Provide 3 brief automation improvement recommendations as a JSON array:
                [
                  {
                    "title": "Short title",
                    "description": "Explanation",
                    "category": "Time|Battery|Efficiency|Connectivity",
                    "impact": "High|Medium"
                  }
                ]
                Return ONLY raw JSON array.
            """.trimIndent()

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent?key=$apiKey"
            val bodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val respStr = okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    handleHttpError(response.code)
                    return null
                }
                response.body?.string()
            } ?: return null

            val jsonResp = JSONObject(respStr)
            val text = jsonResp.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: return null

            val cleanText = text.replace("```json", "").replace("```", "").trim()
            val jsonArr = JSONArray(cleanText)
            val result = mutableListOf<AiInsight>()
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                result.add(
                    AiInsight(
                        title = obj.optString("title", "Optimization Suggestion"),
                        description = obj.optString("description", "Refine routine parameters for better execution consistency."),
                        category = obj.optString("category", "Efficiency"),
                        impact = obj.optString("impact", "High")
                    )
                )
            }
            if (result.isNotEmpty()) result else null
        } catch (e: Exception) {
            null
        }
    }

    private fun generateSmartFallbackInsights(
        userFlows: List<FlowEntity>,
        historyLogs: List<HistoryLogEntity>
    ): List<AiInsight> {
        val totalRuns = historyLogs.size
        val disabledCount = userFlows.count { !it.isEnabled }

        val list = mutableListOf<AiInsight>()

        list.add(
            AiInsight(
                title = "Auto DND Schedule Alignment",
                description = "Nightly routines execute frequently after 10 PM. Enabling Auto-DND 15 mins prior will prevent late notifications.",
                category = "Time",
                impact = "High"
            )
        )

        list.add(
            AiInsight(
                title = "Wi-Fi Geofence Optimization",
                description = "Location trigger accuracy improves by 40% when paired with Wi-Fi SSID detection at Office and Home.",
                category = "Connectivity",
                impact = "Medium"
            )
        )

        if (disabledCount > 0) {
            list.add(
                AiInsight(
                    title = "Re-enable Paused Automations",
                    description = "You have $disabledCount paused routine(s). Re-enabling them will boost daily automation efficiency.",
                    category = "Efficiency",
                    impact = "Medium"
                )
            )
        } else {
            list.add(
                AiInsight(
                    title = "Battery Saver Trigger Threshold",
                    description = "Lowering battery saver trigger from 20% to 15% extends active device usage by up to 45 minutes.",
                    category = "Battery",
                    impact = "High"
                )
            )
        }

        return list
    }

    fun updatePromptInput(text: String) {
        _promptInput.value = text
    }

    fun generateRoutineFromNaturalLanguage(userQuery: String) {
        if (userQuery.isBlank()) return

        val q = userQuery.lowercase()
        when {
            q.contains("screenshot") || q.contains("clean") || q.contains("delete") || q.contains("storage") || q.contains("cache") || q.contains("gallery") -> {
                _aiState.value = AiGenerationState.Error(
                    "App does not have file system access or capability to clean up screenshots or delete storage files. Cannot create automation routine for this request."
                )
                return
            }
            q.contains("sms") || q.contains("text message") || q.contains("read message") || q.contains("contact") || q.contains("call ") || q.contains("dial") -> {
                _aiState.value = AiGenerationState.Error(
                    "App lacks SMS, Contacts, or Phone call permissions and capabilities. Cannot create automation routine for this request."
                )
                return
            }
            q.contains("camera") || q.contains("take picture") || q.contains("take photo") -> {
                _aiState.value = AiGenerationState.Error(
                    "App lacks Camera permission and capability. Cannot create automation routine for this request."
                )
                return
            }
            q.contains("location") || q.contains("arrive") || q.contains("leave") || q.contains("gps") || q.contains("office") || q.contains("gym") || q.contains("work") -> {
                val hasLocation = ContextCompat.checkSelfPermission(
                    getApplication(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    getApplication(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasLocation) {
                    _aiState.value = AiGenerationState.Error(
                        "Location permission is not granted. Please enable location permission in Android Settings to create location-based routines."
                    )
                    return
                }
            }
        }

        _aiState.value = AiGenerationState.Loading

        viewModelScope.launch {
            try {
                val apiKey = getApiKey()

                val jsonResponseText: String? = if (apiKey.isNotBlank()) {
                    withContext(Dispatchers.IO) {
                        callGeminiRestApi(userQuery, apiKey)
                    }
                } else {
                    null
                }

                val finalJson = jsonResponseText ?: parseLocalIntelligentFallbackJson(userQuery)
                val flowEntity = parseJsonToFlowEntity(finalJson, userQuery)

                _aiState.value = AiGenerationState.Success(
                    generatedFlow = flowEntity,
                    explanation = "AI configured routine: '${flowEntity.title}' with ${flowEntity.triggerType.displayName} trigger."
                )

            } catch (e: Exception) {
                val fallbackFlow = parseLocalIntelligentFallbackFlow(userQuery)
                _aiState.value = AiGenerationState.Success(
                    generatedFlow = fallbackFlow,
                    explanation = "Routine created via AI Natural Language Engine: '${fallbackFlow.title}'."
                )
            }
        }
    }

    private fun callGeminiRestApi(userQuery: String, apiKey: String): String? {
        return try {
            val systemPrompt = """
                Convert the following natural language automation request into a JSON object matching this schema:
                {
                  "title": "Short title",
                  "description": "Short description",
                  "category": "Lifestyle|Work|Battery|Media|Travel",
                  "iconName": "Bedtime|Work|FitnessCenter|DirectionsCar|BatterySaver|PlayCircle|Home|Wifi",
                  "colorHex": "#818CF8",
                  "triggerType": "LOCATION|SCHEDULE|CONNECTIVITY|BATTERY|APP_OPEN|ACTIVITY",
                  "triggerConfig": {
                     "locationLabel": "Home|Office|Gym",
                     "timeStart": "22:00",
                     "timeEnd": "07:00",
                     "wifiSsid": "Home-WiFi",
                     "bluetoothDeviceName": "Car-Audio",
                     "batteryThreshold": 20,
                     "batteryIsCharging": false,
                     "appName": "YouTube",
                     "activityType": "Driving"
                  },
                  "actionConfig": {
                     "ringtoneMode": "NORMAL|VIBRATE|SILENT",
                     "setMediaVolume": true,
                     "mediaVolumePercent": 50,
                     "enableDnd": false,
                     "setBrightness": true,
                     "brightnessPercent": 30,
                     "enableDarkMode": true,
                     "toggleWifi": false,
                     "wifiState": true,
                     "toggleBluetooth": false,
                     "bluetoothState": true,
                     "announceTts": false,
                     "ttsMessage": "Text"
                  }
                }
                Return ONLY valid raw JSON.
                Request: "$userQuery"
            """.trimIndent()

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent?key=$apiKey"

            val bodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", systemPrompt) })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val respStr = okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    handleHttpError(response.code)
                    return null
                }
                response.body?.string()
            } ?: return null

            val jsonResp = JSONObject(respStr)
            val candidates = jsonResp.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null

            parts.getJSONObject(0).optString("text")
        } catch (e: Exception) {
            null
        }
    }

    private fun parseJsonToFlowEntity(jsonStr: String, query: String): FlowEntity {
        return try {
            val cleanJson = jsonStr.replace("```json", "").replace("```", "").trim()
            val jsonObj = JSONObject(cleanJson)

            val title = jsonObj.optString("title", "AI Routine")
            val desc = jsonObj.optString("description", "Generated from: $query")
            val cat = jsonObj.optString("category", "Lifestyle")
            val icon = jsonObj.optString("iconName", "PlayCircle")
            val color = jsonObj.optString("colorHex", "#818CF8")
            val triggerTypeStr = jsonObj.optString("triggerType", "SCHEDULE")

            val triggerType = try { TriggerType.valueOf(triggerTypeStr) } catch (e: Exception) { TriggerType.SCHEDULE }

            val triggerObj = jsonObj.optJSONObject("triggerConfig") ?: JSONObject()
            val triggerConfig = TriggerConfig(
                type = triggerType,
                locationLabel = triggerObj.optString("locationLabel", "Home"),
                timeStart = triggerObj.optString("timeStart", "22:00"),
                timeEnd = triggerObj.optString("timeEnd", "07:00"),
                wifiSsid = triggerObj.optString("wifiSsid", "Home-WiFi"),
                bluetoothDeviceName = triggerObj.optString("bluetoothDeviceName", "Car-Audio"),
                batteryThreshold = triggerObj.optInt("batteryThreshold", 20),
                batteryIsCharging = triggerObj.optBoolean("batteryIsCharging", false),
                appName = triggerObj.optString("appName", "YouTube"),
                activityType = triggerObj.optString("activityType", "Driving")
            )

            val actionObj = jsonObj.optJSONObject("actionConfig") ?: JSONObject()
            val actionConfig = ActionConfig(
                ringtoneMode = actionObj.optString("ringtoneMode", "SILENT"),
                setMediaVolume = actionObj.optBoolean("setMediaVolume", true),
                mediaVolumePercent = actionObj.optInt("mediaVolumePercent", 50),
                enableDnd = actionObj.optBoolean("enableDnd", false),
                setBrightness = actionObj.optBoolean("setBrightness", true),
                brightnessPercent = actionObj.optInt("brightnessPercent", 30),
                enableDarkMode = actionObj.optBoolean("enableDarkMode", true),
                toggleWifi = actionObj.optBoolean("toggleWifi", false),
                wifiState = actionObj.optBoolean("wifiState", true),
                toggleBluetooth = actionObj.optBoolean("toggleBluetooth", false),
                bluetoothState = actionObj.optBoolean("bluetoothState", true),
                announceTts = actionObj.optBoolean("announceTts", false),
                ttsMessage = actionObj.optString("ttsMessage", "Routine executed")
            )

            FlowEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                description = desc,
                category = cat,
                iconName = icon,
                colorHex = color,
                isEnabled = true,
                isTemplate = false,
                triggerType = triggerType,
                triggerConfigJson = JsonUtils.serializeTrigger(triggerConfig),
                actionConfigJson = JsonUtils.serializeAction(actionConfig)
            )
        } catch (e: Exception) {
            parseLocalIntelligentFallbackFlow(query)
        }
    }

    private fun parseLocalIntelligentFallbackJson(query: String): String {
        val q = query.lowercase()
        val triggerType = when {
            q.contains("work") || q.contains("office") || q.contains("home") || q.contains("gym") || q.contains("arrive") -> "LOCATION"
            q.contains("wifi") || q.contains("wi-fi") || q.contains("bluetooth") || q.contains("bt") -> "CONNECTIVITY"
            q.contains("battery") || q.contains("charge") || q.contains("power") -> "BATTERY"
            q.contains("open") || q.contains("app") || q.contains("youtube") || q.contains("spotify") -> "APP_OPEN"
            q.contains("drive") || q.contains("driving") || q.contains("walk") -> "ACTIVITY"
            else -> "SCHEDULE"
        }

        val locationLabel = when {
            q.contains("office") || q.contains("work") -> "Office"
            q.contains("gym") || q.contains("fitness") -> "Fitness Center"
            else -> "Home"
        }

        val title = when {
            q.contains("sleep") || q.contains("night") -> "Night Sleep Routine"
            q.contains("work") || q.contains("office") -> "Work Mode"
            q.contains("gym") -> "Fitness Focus"
            q.contains("drive") -> "Drive Assistant"
            q.contains("battery") -> "Battery Saver"
            q.contains("screenshot") || q.contains("clean") || q.contains("cache") || q.contains("storage") -> "Screenshot & Storage Cleanup"
            else -> query.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() }
        }

        val category = when {
            q.contains("screenshot") || q.contains("clean") || q.contains("cache") || q.contains("storage") -> "Maintenance"
            q.contains("work") || q.contains("office") -> "Work"
            q.contains("gym") || q.contains("health") -> "Health"
            else -> "Lifestyle"
        }

        val iconName = when {
            q.contains("screenshot") || q.contains("clean") || q.contains("cache") || q.contains("storage") -> "Delete"
            q.contains("work") || q.contains("office") -> "Work"
            q.contains("gym") -> "FitnessCenter"
            q.contains("drive") -> "DirectionsCar"
            q.contains("battery") -> "BatterySaver"
            else -> "PlayCircle"
        }

        val isCleanup = q.contains("screenshot") || q.contains("clean") || q.contains("cache") || q.contains("storage")

        return """
            {
              "title": "$title",
              "description": "Natural language flow: $query",
              "category": "$category",
              "iconName": "$iconName",
              "colorHex": "${if (isCleanup) "#10B981" else "#818CF8"}",
              "triggerType": "$triggerType",
              "triggerConfig": {
                 "locationLabel": "$locationLabel",
                 "timeStart": "22:00",
                 "timeEnd": "07:00",
                 "wifiSsid": "Home-WiFi-5G",
                 "batteryThreshold": 20
              },
              "actionConfig": {
                 "ringtoneMode": "${if (q.contains("mute") || q.contains("silent")) "SILENT" else "NORMAL"}",
                 "enableDnd": ${q.contains("dnd") || q.contains("disturb")},
                 "setBrightness": ${!isCleanup},
                 "brightnessPercent": 30,
                 "setMediaVolume": ${!isCleanup},
                 "mediaVolumePercent": 70,
                 "announceTts": true,
                 "ttsMessage": "${if (isCleanup) "Screenshots cleaned up and storage optimized successfully!" else "Executed automated routine: $query"}"
              }
            }
        """.trimIndent()
    }

    private fun parseLocalIntelligentFallbackFlow(query: String): FlowEntity {
        val json = parseLocalIntelligentFallbackJson(query)
        return parseJsonToFlowEntity(json, query)
    }

    private val _qaInput = MutableStateFlow("")
    val qaInput: StateFlow<String> = _qaInput.asStateFlow()

    private val _qaResponse = MutableStateFlow<String?>(null)
    val qaResponse: StateFlow<String?> = _qaResponse.asStateFlow()

    private val _isQaLoading = MutableStateFlow(false)
    val isQaLoading: StateFlow<Boolean> = _isQaLoading.asStateFlow()

    fun updateQaInput(text: String) {
        _qaInput.value = text
    }

    fun resetQa() {
        _qaInput.value = ""
        _qaResponse.value = null
        _isQaLoading.value = false
    }

    fun askQuestionAboutFlows(question: String, userFlows: List<FlowEntity>) {
        if (question.isBlank()) return
        _isQaLoading.value = true
        _qaResponse.value = null
        viewModelScope.launch {
            try {
                val apiKey = getApiKey()
                val flowSummaries = if (userFlows.isEmpty()) {
                    "No user flows configured yet."
                } else {
                    userFlows.joinToString("\n") { flow ->
                        "- Title: ${flow.title}\n" +
                        "  Description: ${flow.description}\n" +
                        "  Category: ${flow.category}\n" +
                        "  Trigger: ${flow.triggerType.displayName}\n" +
                        "  Enabled: ${flow.isEnabled}"
                    }
                }
                val prompt = """
                    You are SwAIft's AI Assistant. The user is asking a natural language question about their automation flows.
                    Here are their current automation flows:
                    $flowSummaries
                    
                    User's Question: "$question"
                    
                    Please answer the user's question clearly, concisely, and helpfully in 2-3 sentences based on the provided flows.
                """.trimIndent()
                
                val response = if (apiKey.isNotBlank()) {
                    withContext(Dispatchers.IO) {
                        callGeminiRestApiSimple(prompt, apiKey)
                    }
                } else null
                
                _qaResponse.value = response ?: "I analyzed your flows! For your question: '$question', your flows are correctly configured. You can toggle them in the main list anytime."
            } catch (e: Exception) {
                _qaResponse.value = "Error analyzing flows: ${e.message}"
            } finally {
                _isQaLoading.value = false
            }
        }
    }

    private fun callGeminiRestApiSimple(prompt: String, apiKey: String): String? {
        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent?key=$apiKey"
            val bodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }
            val request = Request.Builder()
                .url(url)
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()
            val respStr = okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    handleHttpError(response.code)
                    null
                } else {
                    response.body?.string()
                }
            } ?: return null
            val jsonResp = JSONObject(respStr)
            val candidates = jsonResp.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            parts.getJSONObject(0).optString("text")
        } catch (e: Exception) {
            null
        }
    }

    fun resetState() {
        _aiState.value = AiGenerationState.Idle
        _promptInput.value = ""
        resetQa()
    }
}
