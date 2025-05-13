package com.pinAD.pinAD_fe.Data

import com.google.gson.*
import com.pinAD.pinAD_fe.Data.pin.Tag
import java.lang.reflect.Type

class TagsDeserializer : JsonDeserializer<List<Tag>> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): List<Tag> {
        val tags = mutableListOf<Tag>()
        val jsonArray = json.asJsonObject.getAsJsonArray("tags") // "tags" 배열 가져오기

        for (element in jsonArray) {
            val tagObject = element.asJsonObject
            val name = tagObject.get("name").asString
            val postCount = tagObject.get("post_count").asInt
            tags.add(Tag(name, postCount))
        }

        return tags
    }
}
