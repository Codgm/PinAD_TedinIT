package com.example.mappin_fe.Data

import com.google.gson.*
import java.lang.reflect.Type

class TagsDeserializer : JsonDeserializer<List<String>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<String> {
        val tags = mutableListOf<String>()
        if (json.isJsonArray) {
            json.asJsonArray.forEach { element ->
                when {
                    element.isJsonPrimitive -> tags.add(element.asString)
                    element.isJsonObject -> tags.add(element.toString())
                }
            }
        } else if (json.isJsonObject) {
            tags.add(json.toString())
        }
        return tags
    }
}