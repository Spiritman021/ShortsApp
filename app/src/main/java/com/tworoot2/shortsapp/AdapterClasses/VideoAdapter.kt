package com.tworoot2.shortsapp.AdapterClasses

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.tworoot2.shortsapp.DataClasses.VideoData
import com.tworoot2.shortsapp.R

class VideoAdapter(var context: Context, var arrayList: ArrayList<VideoData>) :
    RecyclerView.Adapter<VideoAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_video, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = arrayList[position].title
        holder.videoView.setVideoURI(Uri.parse(arrayList[position].url))
        holder.videoView.setOnPreparedListener {
            holder.progressBar.visibility = View.GONE
            it.start()

            val videoRatio = it.videoWidth / it.videoHeight.toFloat()
            val screenRatio = holder.videoView.width / holder.videoView.height.toFloat()
            val scale = videoRatio / screenRatio

            if (scale >= 1f) {
                holder.videoView.scaleX = scale
            } else {
                holder.videoView.scaleY = (1f / scale)
            }

        }

        holder.videoView.setOnCompletionListener {
            it.start()
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoView: VideoView = itemView.findViewById(R.id.videoView)
        val title: TextView = itemView.findViewById(R.id.title)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }
}