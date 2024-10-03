package com.example.mappin_fe

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import coil.load
import com.example.mappin_fe.Data.PinDataResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.*

class PinDetailBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(pinDataJson: String): PinDetailBottomSheet {
            return PinDetailBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("PIN_DATA_JSON", pinDataJson)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_pin_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val pinDataJson = arguments?.getString("PIN_DATA_JSON") ?: return
            Log.d("pinDataJson", "$pinDataJson")
            val jsonObject = JSONObject(pinDataJson)
            val pinDataObject = jsonObject.optJSONObject("nameValuePairs") ?: jsonObject
            Log.d("jsonObject", "$jsonObject")
            val gson = GsonBuilder()
                .registerTypeAdapter(List::class.java, TagsDeserializer())
                .create()
            val pinData = gson.fromJson(pinDataObject.toString(), PinDataResponse::class.java)


            setupUI(view, pinData, pinDataObject)
        } catch (e: Exception) {
            // Handle error (e.g., show error message)
            Log.e("PinDetailBottomSheet", "Error parsing data: ${e.message}", e)
        }
    }

    private fun setupUI(view: View, pinData: PinDataResponse,  jsonObject: JSONObject) {
        view.findViewById<TextView>(R.id.tvTitle).text = pinData.title
        view.findViewById<TextView>(R.id.tvDescription).text = "${pinData.description}"

        val infoJson = try {
            JSONObject(pinData.info.toString())
        } catch (e: Exception) {
            Log.e("PinDetailBottomSheet", "Error parsing info JSON: ${e.message}", e)
            JSONObject()
        }
        val range = infoJson.optInt("range", 0)
        val duration = infoJson.optInt("duration", 0)

        view.findViewById<TextView>(R.id.tvRange).visibility = View.GONE
        view.findViewById<TextView>(R.id.tvDuration).apply {
            text = "Duration: $duration hours"
            visibility = if (duration > 0) View.VISIBLE else View.GONE
        }
        setupPurchaseButton(view, pinData)
//        setupTags(view, pinData)
        setupChronometer(view, pinData, duration)
        setupProgressBar(view)
        setupMoreDetailsButton(view, pinData, jsonObject)
    }

    private fun setupPurchaseButton(view: View, pinData: PinDataResponse) {
        val btnPurchase = view.findViewById<Button>(R.id.btnPurchase)
        btnPurchase.visibility = if (pinData.is_ads == true) View.VISIBLE else View.GONE
    }

    private class TagsDeserializer : JsonDeserializer<List<Any>> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): List<Any> {
            if (json.isJsonObject) {
                val tagsObject = json.asJsonObject
                if (tagsObject.has("values") && tagsObject.get("values").isJsonArray) {
                    return context.deserialize(tagsObject.get("values"), typeOfT)
                }
            }
            return emptyList() // 또는 다른 기본값 반환
        }
    }

//    private fun setupTags(view: View, pinData: PinDataResponse) {
//        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupTags)
//        pinData.tags.forEach { tag ->
//            val chip = Chip(context)
//            chip.text = tag
//            chipGroup.addView(chip)
//        }
//    }

    private fun setupChronometer(view: View, pinData: PinDataResponse, duration: Int) {
        val chronometer = view.findViewById<Chronometer>(R.id.chronometer)
        val endTime = pinData.created_at.time + (duration * 60 * 60 * 1000)
        val remainingTime = endTime - System.currentTimeMillis()
        if (remainingTime > 0) {
            chronometer.base = SystemClock.elapsedRealtime() + remainingTime
            chronometer.start()
        } else {
            chronometer.text = "Expired"
        }
    }

    private fun setupProgressBar(view: View) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val maxParticipants = 100 // Example value
        val currentParticipants = 50 // Example value
        progressBar.max = maxParticipants
        progressBar.progress = currentParticipants
        view.findViewById<TextView>(R.id.tvParticipantInfo).text = "Current participants: $currentParticipants / $maxParticipants"
    }

    private fun setupMoreDetailsButton(view: View, pinData: PinDataResponse, jsonObject: JSONObject) {
        val tvMoreDetails = view.findViewById<TextView>(R.id.tvMoreDetails)
        val ivMedia = view.findViewById<ImageView>(R.id.ivMedia)
        val contentLayout = view.findViewById<ConstraintLayout>(R.id.contentLayout)
        val tvField1 = view.findViewById<TextView>(R.id.tvField1)
        val tvField2 = view.findViewById<TextView>(R.id.tvField2)
        val tvField3 = view.findViewById<TextView>(R.id.tvField3)

        val mediaUrl: String = when {
            jsonObject.has("media") -> jsonObject.optString("media")
            jsonObject.has("media_files") -> {
                val mediaFiles = jsonObject.optJSONArray("media_files")
                if (mediaFiles != null && mediaFiles.length() > 0) {
                    mediaFiles.optString(0)
                } else {
                    ""
                }
            }
            else -> ""
        }
        Log.d("PinDetail", "Media URL: $mediaUrl")

        var isExpanded = false

        tvMoreDetails.setOnClickListener {
            isExpanded = !isExpanded
            if (isExpanded) {
                if (mediaUrl.isNotEmpty()) {
                    ivMedia.load(mediaUrl)
                    ivMedia.visibility = View.VISIBLE
                } else {
                    ivMedia.visibility = View.GONE
                }

                val infoJson = try {
                    JSONObject(pinData.info.toString())
                } catch (e: Exception) {
                    Log.e("PinDetailBottomSheet", "Error parsing info JSON: ${e.message}", e)
                    JSONObject()
                }
                val additionalInfo = infoJson.optString("additionalInfo", "{}")
                val additionalInfoJson = try {
                    JSONObject(additionalInfo)
                } catch (e: Exception) {
                    Log.e("PinDetailBottomSheet", "Error parsing additionalInfo JSON: ${e.message}", e)
                    JSONObject()
                }
                Log.d("PinDetail", "Additional Info JSON: $additionalInfoJson")

                if (pinData.is_ads == true) {
                    tvField1.text = "상품명 : ${additionalInfoJson.optString("field1", "N/A")}"
                    tvField2.text = "판매 수량 : ${additionalInfoJson.optString("field2", "N/A")}"
                    tvField3.text = "할인율 또는 할인가 : ${additionalInfoJson.optString("field3", "N/A")}"

                    Log.d("PinDetail", "$tvField1, $tvField2, $tvField3")

                    tvField1.visibility = View.VISIBLE
                    tvField2.visibility = View.VISIBLE
                    tvField3.visibility = View.VISIBLE
                } else {
                    tvField1.text = "요청 사유 : ${additionalInfoJson.optString("field1", "N/A")}"
                    tvField1.visibility = View.VISIBLE
                    tvField2.visibility = View.GONE
                    tvField3.visibility = View.GONE
                }

                contentLayout.visibility = View.VISIBLE
                tvMoreDetails.text = "접기"
            } else {
                ivMedia.visibility = View.GONE
                contentLayout.visibility = View.GONE
                tvMoreDetails.text = "자세히 보기"
            }
        }
    }
}
