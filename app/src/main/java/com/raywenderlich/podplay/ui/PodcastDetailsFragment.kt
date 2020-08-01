package com.raywenderlich.podplay.ui

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.adapter.EpisodeListAdapter
import com.raywenderlich.podplay.viewmodel.PodcastViewModel
import kotlinx.android.synthetic.main.fragment_podcast_details.*
import java.lang.RuntimeException

class PodcastDetailsFragment : Fragment() {
    private var listener: OnPodcastDetailsListener? = null
    private val podcastViewModel: PodcastViewModel by activityViewModels()
    private lateinit var episodeListAdapter: EpisodeListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_podcast_details, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupControls()
        updateControls()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_details, menu)
    }

    private fun setupControls() {
        feedDescTextView.movementMethod = ScrollingMovementMethod()
        episodeRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(activity)
        episodeRecyclerView.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
            episodeRecyclerView.context, layoutManager.orientation
        )
        episodeRecyclerView.addItemDecoration(dividerItemDecoration)

        episodeListAdapter = EpisodeListAdapter(podcastViewModel.activePodcastViewData?.episodes)
        episodeRecyclerView.adapter = episodeListAdapter
    }

    private fun updateControls() {
        val viewData = podcastViewModel.activePodcastViewData ?: return

        feedTitleTextView.text = viewData.feedTitle
        feedDescTextView.text = viewData.feedDesc

        activity?.let {
            Glide.with(it).load(viewData.imageUrl).into(feedImageView)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnPodcastDetailsListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnPodcastDetailsListener")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_feed_action -> {
                podcastViewModel.activePodcastViewData?.feedUrl?.let {
                    listener?.onSubscribe()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun newInstance(): PodcastDetailsFragment {
            return PodcastDetailsFragment()
        }
    }

    interface OnPodcastDetailsListener {
        fun onSubscribe()
    }
}