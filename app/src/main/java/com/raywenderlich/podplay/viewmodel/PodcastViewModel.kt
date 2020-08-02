package com.raywenderlich.podplay.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import com.raywenderlich.podplay.repository.PodcastRepo
import com.raywenderlich.podplay.util.DateUtils
import com.raywenderlich.podplay.viewmodel.SearchViewModel.PodcastSummaryViewData
import java.util.*

class PodcastViewModel(application: Application): AndroidViewModel(application) {
    var podcastRepo: PodcastRepo? = null
    var activePodcastViewData: PodcastViewData? = null
    private var activePodcast: Podcast? = null
    var livePodcastData: LiveData<List<PodcastSummaryViewData>>? = null

    fun getPodcast(
        podcastSummaryViewData: PodcastSummaryViewData,
        callback: (PodcastViewData?) -> Unit
    ) {
        val repo = podcastRepo ?: return
        val feedUrl = podcastSummaryViewData.feedUrl ?: return

        repo.getPodcast(feedUrl) {
            it?.let {
                it.feedTitle = podcastSummaryViewData.name ?: ""
                it.imageUrl = podcastSummaryViewData.imageUrl ?: ""
                activePodcastViewData = podcastToPodcastView(it)
                activePodcast = it
                callback(activePodcastViewData)
            }
        }
    }

    fun getPodcasts(): LiveData<List<PodcastSummaryViewData>>? {
        val repo = podcastRepo ?: return null

        if (livePodcastData == null) {
            val liveData = repo.getAll()

            livePodcastData = Transformations.map(liveData) { podcastList ->
                podcastList.map { podcast ->
                    podcastToSumarryView(podcast)
                }
            }
        }

        return livePodcastData
    }


    fun setActivePodcast(feedUrl: String, callback: (PodcastSummaryViewData?) -> Unit) {
        val repo = podcastRepo ?: return

        repo.getPodcast(feedUrl) {
            if (it == null) {
                callback(null)
            } else {
                activePodcastViewData = podcastToPodcastView(it)
                activePodcast = it
                callback(podcastToSumarryView(it))
            }
        }
    }

    fun saveActivePodcast() {
        val repo = podcastRepo ?: return

        activePodcast?.let {
            repo.save(it)
        }
    }

    fun deleteActivePodcast() {
        val repo = podcastRepo ?: return

        activePodcast?.let {
            repo.delete(it)
        }
    }

    private fun podcastToSumarryView(podcast: Podcast): PodcastSummaryViewData {
        return PodcastSummaryViewData(
            podcast.feedTitle,
            DateUtils.dateToShortDate(podcast.lastUpdated),
            podcast.imageUrl,
            podcast.feedUrl
        )
    }

    private fun podcastToPodcastView(podcast: Podcast): PodcastViewData {
        return PodcastViewData(
            podcast.id != null, // If ID is set, it was loaded from DB, and it's subscribed
            podcast.feedTitle,
            podcast.feedUrl,
            podcast.feedDesc,
            podcast.imageUrl,
            episodeToEpisodeView(podcast.episodes)
        )
    }

    private fun episodeToEpisodeView(episodes: List<Episode>): List<EpisodeViewData> {
        return episodes.map { episode ->
            EpisodeViewData(
                episode.guid,
                episode.title,
                episode.description,
                episode.mediaUrl,
                episode.releaseDate,
                episode.duration
            )
        }
    }

    data class PodcastViewData(
        var subscribed: Boolean = false,
        var feedTitle: String? = "",
        var feedUrl: String? = "",
        var feedDesc: String? = "",
        var imageUrl: String? = "",
        var episodes: List<EpisodeViewData>
    )

    data class EpisodeViewData(
        var guid: String? = "",
        var title: String? = "",
        var description: String? = "",
        var mediaUrl: String? = "",
        var releaseDate: Date? = Date(),
        var duration: String? = ""
    )
}