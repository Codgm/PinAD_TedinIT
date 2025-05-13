package com.pinAD.pinAD_fe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pinAD.pinAD_fe.Data.pin.like_comment.Comment

class CommentsAdapter(
    private val comments: MutableList<Comment>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvContent: TextView = view.findViewById(R.id.tvContent)
        val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.tvUsername.text = comment.author__username
        holder.tvContent.text = comment.content
        holder.tvTimestamp.text = comment.created_at
        holder.btnDelete.setOnClickListener { onDeleteClick(comment.id) }
    }

    override fun getItemCount() = comments.size

    fun updateComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }

    fun addComment(comment: Comment) {
        comments.add(0, comment)
        notifyItemInserted(0)
    }

    fun removeComment(commentId: Int) {
        val position = comments.indexOfFirst { it.id == commentId }
        if (position != -1) {
            comments.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}