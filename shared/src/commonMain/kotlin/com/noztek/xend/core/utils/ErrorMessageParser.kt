package com.noztek.xend.core.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun errorMessageParser(errorText: String): String {
    return try {
        val json = Json.parseToJsonElement(errorText).jsonObject
        when {
            "message" in json -> json["message"]?.jsonPrimitive?.contentOrNull ?: errorText
            "error" in json -> json["error"]?.jsonPrimitive?.contentOrNull ?: errorText
            "errors" in json -> {
                val errors = json["errors"]?.jsonArray?.mapNotNull {
                    it.jsonPrimitive.contentOrNull
                }.orEmpty()
                if (errors.isEmpty()) errorText else errors.joinToString("\n")
            }
            else -> errorText
        }
    } catch (_: Exception) {
        errorText
    }
}
