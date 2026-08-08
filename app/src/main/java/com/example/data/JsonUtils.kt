package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object JsonUtils {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val triggerAdapter = moshi.adapter(TriggerConfig::class.java)
    private val actionAdapter = moshi.adapter(ActionConfig::class.java)

    fun serializeTrigger(config: TriggerConfig): String {
        return try {
            triggerAdapter.toJson(config)
        } catch (e: Exception) {
            "{}"
        }
    }

    fun deserializeTrigger(json: String): TriggerConfig {
        return try {
            if (json.isBlank()) TriggerConfig()
            else triggerAdapter.fromJson(json) ?: TriggerConfig()
        } catch (e: Exception) {
            TriggerConfig()
        }
    }

    fun serializeAction(config: ActionConfig): String {
        return try {
            actionAdapter.toJson(config)
        } catch (e: Exception) {
            "{}"
        }
    }

    fun deserializeAction(json: String): ActionConfig {
        return try {
            if (json.isBlank()) ActionConfig()
            else actionAdapter.fromJson(json) ?: ActionConfig()
        } catch (e: Exception) {
            ActionConfig()
        }
    }

    fun serializeBackup(flows: List<FlowEntity>, logs: List<HistoryLogEntity>): String {
        val root = org.json.JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        val flowsArr = org.json.JSONArray()
        flows.forEach { f ->
            val obj = org.json.JSONObject()
            obj.put("id", f.id)
            obj.put("title", f.title)
            obj.put("description", f.description)
            obj.put("category", f.category)
            obj.put("iconName", f.iconName)
            obj.put("colorHex", f.colorHex)
            obj.put("isEnabled", f.isEnabled)
            obj.put("isTemplate", f.isTemplate)
            obj.put("triggerType", f.triggerType.name)
            obj.put("triggerConfigJson", f.triggerConfigJson)
            obj.put("actionConfigJson", f.actionConfigJson)
            obj.put("scheduledTime", f.scheduledTime ?: "")
            obj.put("runCount", f.runCount)
            flowsArr.put(obj)
        }
        root.put("flows", flowsArr)

        val logsArr = org.json.JSONArray()
        logs.forEach { l ->
            val obj = org.json.JSONObject()
            obj.put("id", l.id)
            obj.put("flowId", l.flowId)
            obj.put("flowTitle", l.flowTitle)
            obj.put("iconName", l.iconName)
            obj.put("colorHex", l.colorHex)
            obj.put("timestampMillis", l.timestampMillis)
            obj.put("status", l.status)
            obj.put("triggerReason", l.triggerReason)
            obj.put("actionsExecuted", l.actionsExecuted)
            logsArr.put(obj)
        }
        root.put("historyLogs", logsArr)

        return root.toString(2)
    }

    fun parseBackup(jsonStr: String): Pair<List<FlowEntity>, List<HistoryLogEntity>> {
        val root = org.json.JSONObject(jsonStr)

        val flows = mutableListOf<FlowEntity>()
        val flowsArr = root.optJSONArray("flows") ?: org.json.JSONArray()
        for (i in 0 until flowsArr.length()) {
            val obj = flowsArr.getJSONObject(i)
            flows.add(
                FlowEntity(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    description = obj.optString("description", ""),
                    category = obj.optString("category", "Lifestyle"),
                    iconName = obj.optString("iconName", "PlayCircle"),
                    colorHex = obj.optString("colorHex", "#818CF8"),
                    isEnabled = obj.optBoolean("isEnabled", true),
                    isTemplate = obj.optBoolean("isTemplate", false),
                    triggerType = try { TriggerType.valueOf(obj.getString("triggerType")) } catch (e: Exception) { TriggerType.SCHEDULE },
                    triggerConfigJson = obj.optString("triggerConfigJson", "{}"),
                    actionConfigJson = obj.optString("actionConfigJson", "{}"),
                    scheduledTime = obj.optString("scheduledTime", "08:00"),
                    runCount = obj.optInt("runCount", 0)
                )
            )
        }

        val logs = mutableListOf<HistoryLogEntity>()
        val logsArr = root.optJSONArray("historyLogs") ?: org.json.JSONArray()
        for (i in 0 until logsArr.length()) {
            val obj = logsArr.getJSONObject(i)
            logs.add(
                HistoryLogEntity(
                    id = obj.optLong("id", 0L),
                    flowId = obj.optString("flowId", ""),
                    flowTitle = obj.optString("flowTitle", ""),
                    iconName = obj.optString("iconName", "PlayCircle"),
                    colorHex = obj.optString("colorHex", "#818CF8"),
                    timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis()),
                    status = obj.optString("status", "SUCCESS"),
                    triggerReason = obj.optString("triggerReason", ""),
                    actionsExecuted = obj.optString("actionsExecuted", "")
                )
            )
        }

        return Pair(flows, logs)
    }
}
