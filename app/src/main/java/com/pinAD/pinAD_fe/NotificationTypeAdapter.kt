package com.pinAD.pinAD_fe

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.pinAD.pinAD_fe.Data.notification.BaseNotification
import com.pinAD.pinAD_fe.Data.notification.BusinessNotification
import com.pinAD.pinAD_fe.Data.notification.Content
import com.pinAD.pinAD_fe.Data.notification.Notification

class NotificationTypeAdapter : TypeAdapter<BaseNotification>() {
    private val gson = Gson()

    override fun write(out: JsonWriter, value: BaseNotification?) {
        // 작성 로직은 현재 불필요하므로 생략
        out.nullValue()
    }

    override fun read(reader: JsonReader): BaseNotification {
        var notification_id = 0
        var title = ""
        var content: Any? = null
        var status = ""
        var created_at = ""

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "notification_id" -> notification_id = reader.nextInt()
                "title" -> title = reader.nextString()
                "content" -> {
                    // content의 타입을 확인
                    val token = reader.peek()
                    content = when (token) {
                        JsonToken.STRING -> reader.nextString()
                        JsonToken.BEGIN_OBJECT -> {
                            val contentJson = gson.fromJson<Content>(
                                reader,
                                Content::class.java
                            )
                            contentJson
                        }
                        else -> {
                            reader.skipValue()
                            null
                        }
                    }
                }
                "status" -> status = reader.nextString()
                "created_at" -> created_at = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return when (content) {
            is String -> BusinessNotification(
                notification_id = notification_id,
                title = title,
                content = content,
                status = status,
                created_at = created_at
            )
            is Content -> Notification(
                notification_id = notification_id,
                title = title,
                content = content,
                status = status,
                created_at = created_at
            )
            else -> throw JsonParseException("Invalid content format")
        }
    }
}
