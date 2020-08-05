package com.raywenderlich.podplay.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.util.DateUtils
import com.raywenderlich.podplay.util.HtmlUtils
import com.raywenderlich.podplay.viewmodel.PodcastViewModel.EpisodeViewData
import kotlinx.android.synthetic.main.episode_item.view.*

class EpisodeListAdapter(
    private var episodeViewList: List<EpisodeViewData>?,
    private val episodeListAdapterListener: EpisodeListAdapterListener
) : RecyclerView.Adapter<EpisodeListAdapter.ViewHolder>() {

    class ViewHolder(
        view: View,
        private val episodeListAdapterListener: EpisodeListAdapterListener
    ) : RecyclerView.ViewHolder(view) {
        var episodeViewData: EpisodeViewData? = null
        var titleTextView: TextView = view.titleView
        var descTextView: TextView = view.descView
        val durationTextView: TextView = view.durationView
        val releaseDateTextView: TextView = view.releaseDateView

        init {
            view.setOnClickListener {
                episodeViewData?.let {
                    episodeListAdapterListener.onSelectedEpisode(it)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.episode_item, parent, false),
            episodeListAdapterListener
        )
    }

    override fun getItemCount(): Int {
        return episodeViewList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val episodeViewList = episodeViewList ?: return
        val episodeView = episodeViewList[position]

        holder.episodeViewData = episodeView
        holder.titleTextView.text = episodeView.title
        holder.descTextView.text = HtmlUtils.htmlToSpannable(episodeView.description ?: "")
        holder.durationTextView.text = episodeView.duration
        holder.releaseDateTextView.text = episodeView.releaseDate?.let {
            DateUtils.dateToShortDate(it)
        }
    }

    interface EpisodeListAdapterListener {
        fun onSelectedEpisode(episodeViewData: EpisodeViewData)
    }
}