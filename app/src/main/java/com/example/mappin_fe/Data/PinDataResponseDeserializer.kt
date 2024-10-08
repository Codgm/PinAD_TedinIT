package com.example.mappin_fe.Data

import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PinDataResponseDeserializer : JsonDeserializer<PinDataResponse> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): PinDataResponse {
        val jsonObject = json.asJsonObject
        val location = jsonObject.get("location").asString
        val (longitude, latitude) = extractLatLngFromLocation(location)

        val tagsJsonArray = jsonObject.getAsJsonArray("tags")
        val tagsList = mutableListOf<String>()
        for (tagElement in tagsJsonArray) {
            val tagObject = tagElement.asJsonObject
            tagsList.add(tagObject.get("name").asString)
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        return PinDataResponse(
            id = jsonObject.get("id").asString,
            latitude = latitude,
            longitude = longitude,
            location = location,
            user = jsonObject.get("user").asInt,
            title = jsonObject.get("title").asString,
            description = jsonObject.get("description").asString,
            media_files = listOf(jsonObject.get("media").asString),
            info = jsonObject.get("info").asString,
            tags = tagsList,
            visibility = "public", // 서버 응답에 없으므로 기본값 설정
            is_ads = jsonObject.get("is_ads").asBoolean,
            created_at = dateFormat.parse(jsonObject.get("created_at").asString),
            updated_at = dateFormat.parse(jsonObject.get("updated_at").asString)
        )
    }

    private fun extractLatLngFromLocation(location: String): Pair<Double, Double> {
        val regex = """POINT \(([-\d.]+) ([-\d.]+)\)""".toRegex()
        val matchResult = regex.find(location)
        return if (matchResult != null) {
            val (lon, lat) = matchResult.destructured
            Pair(lon.toDouble(), lat.toDouble())
        } else {
            Pair(0.0, 0.0) // 기본값 설정
        }
    }
}