package com.pinAD.pinAD_fe.AddPin.Camera

import android.app.AlertDialog
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.pinAD.pinAD_fe.R

class ThumbnailAdapter(
    private val mediaFiles: MutableList<MediaFile>,
    private val onItemDeleted: (Int) -> Unit,
    private val onItemClicked: (MediaFile) -> Unit
) : RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder>() {

    inner class ThumbnailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnailImageView: ImageView = view.findViewById(R.id.thumbnailImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_thumbnail, parent, false)
        return ThumbnailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        val mediaFile = mediaFiles[position]

        // 썸네일 이미지 설정
        holder.thumbnailImageView.setImageURI(Uri.parse(mediaFile.uri))

        // 클릭 시 전체화면 프리뷰로 전환
        holder.thumbnailImageView.setOnClickListener {
            onItemClicked(mediaFile)
        }

        // 길게 누르면 삭제 다이얼로그 표시
        holder.thumbnailImageView.setOnLongClickListener {
            showDeleteDialog(holder.itemView.context, position)
            true
        }
    }

    private fun showDeleteDialog(context: android.content.Context, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("삭제 확인")
            .setMessage("이 미디어 파일을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                mediaFiles.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, mediaFiles.size)
                onItemDeleted(position)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun getItemCount() = mediaFiles.size
}