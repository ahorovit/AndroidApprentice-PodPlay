package com.raywenderlich.podplay.adapter

import android.app.Activity
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.viewmodel.SearchViewModel

import kotlinx.android.synthetic.main.search_item.view.*

class PodcastListAdapter(
    private var podcastSummaryViewList: List<SearchViewModel.PodcastSummaryViewData>?,
    private val podcastListAdapterListener: PodcastListAdapterListener,
    private val parentActivity: Activity
) : RecyclerView.Adapter<PodcastListAdapter.ViewHolder>() {

    fun setSearchData(podcastSummaryViewData: List<SearchViewModel.PodcastSummaryViewData>) {
        podcastSummaryViewList = podcastSummaryViewData
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.search_item, parent, false),
            podcastListAdapterListener
        )
    }

    override fun getItemCount(): Int {
        return podcastSummaryViewList?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchViewList = podcastSummaryViewList ?: return
        val searchView = searchViewList[position]
        holder.podcastSummaryViewData = searchView
        holder.nameTextView.text = searchView.name
        holder.lastUpdatedTextView.text = searchView.lastUpdated

        Glide.with(parentActivity)
            .load(searchView.imageUrl)
            .into(holder.podcastImageView)
    }

    interface PodcastListAdapterListener {
        fun onShowDetails(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData)
    }

    inner class ViewHolder(
        view: View,
        private val podcastListAdapterListener: PodcastListAdapterListener
    ) : RecyclerView.ViewHolder(view) {
        var podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData? = null
        val nameTextView: TextView = view.podcastNameTextView
        val lastUpdatedTextView: TextView = view.podcastLastUpdatedTextView
        val podcastImageView: ImageView = view.podcastImage

        init {
            view.setOnClickListener {
                podcastSummaryViewData?.let {
                    podcastListAdapterListener.onShowDetails(it)
                }
            }
        }
    }
}