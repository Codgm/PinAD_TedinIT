package com.pinAD.pinAD_fe.Data.pin

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CustomDateTypeAdapter : JsonDeserializer<Date> {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Date {
        try {
            val dateStr = json?.asString
            return dateStr?.let {
                // 시간대 표시가 없는 경우 UTC로 가정하고 파싱
                dateFormat.parse(it)
            } ?: throw JsonParseException("Date string is null")
        } catch (e: ParseException) {
            throw JsonParseException("Failed to parse date", e)
        }
    }
}