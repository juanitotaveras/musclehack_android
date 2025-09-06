package com.musclehack.targetedHypertrophyTraining.blog

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TableRow
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.musclehack.targetedHypertrophyTraining.databinding.PostCardBinding

class PostsAdapter2(private val viewModel: BlogViewModel) :
    ListAdapter<Post, PostsAdapter2.ViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(viewModel, item)
    }

    class ViewHolder private constructor(val binding: PostCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: BlogViewModel, item: Post) {
            binding.post = item
            binding.viewmodel = viewModel
            binding.position = adapterPosition

            val circularProgressDrawable = CircularProgressDrawable(binding.root.context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()
            Glide.with(binding.root.context)
                .load(item.imageURL)
                .placeholder(circularProgressDrawable)
                .into(binding.postImage)

            val layoutParams: Int
            val topPadding: Int
            val gravity: Int
            if (item.description.isEmpty()) {
                layoutParams = TableRow.LayoutParams.MATCH_PARENT
                topPadding = 80
                gravity = Gravity.CENTER_VERTICAL
            } else {
                layoutParams = TableRow.LayoutParams.WRAP_CONTENT
                topPadding = 0
                gravity = Gravity.NO_GRAVITY
            }

            binding.postTitle.layoutParams = TableRow.LayoutParams(layoutParams)
            binding.postTitle.gravity = gravity
            binding.postTitle.setPadding(0, topPadding, 0, 0)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PostCardBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.equals(newItem)
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.hasEqualContents(newItem)
    }
}