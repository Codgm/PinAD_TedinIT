package com.pinAD.pinAD_fe

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.pinAD.pinAD_fe.network.RetrofitInstance

class MediaAdapter(private val mediaList: List<String>) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaContent = mediaList[position]

        if (isBase64Image(mediaContent)) {
            try {
                val cleanBase64 = mediaContent.substringAfter("base64,")
                val bitmap = decodeBase64ToBitmap(cleanBase64)
                val rotatedBitmap = bitmap?.let { rotateBitmap(it, 90f) } // 필요에 따라 각도 수정
                holder.imageView.setImageBitmap(rotatedBitmap)
            } catch (e: Exception) {
                Log.e("ImageLoad", "Failed to decode base64 image")
            }
        } else {
            val fullUrl = "${RetrofitInstance.BASE_URL}$mediaContent"
            holder.imageView.load(fullUrl) {
                crossfade(true)
                listener(
                    onError = { _, throwable ->
                        Log.e("ImageLoad", "Error loading image: ${throwable}")
                    }
                )
            }
        }
    }

    private fun isBase64Image(str: String): Boolean {
        return str.startsWith("data:image") || try {
            Base64.decode(str, Base64.DEFAULT)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e("Base64Decode", "Error decoding Base64: ${e.message}")
            null
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun getItemCount(): Int = mediaList.size
}
