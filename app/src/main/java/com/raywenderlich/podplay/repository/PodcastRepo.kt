package com.raywenderlich.podplay.repository

import androidx.lifecycle.LiveData
import com.raywenderlich.podplay.db.PodcastDao
import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import com.raywenderlich.podplay.service.FeedService
import com.raywenderlich.podplay.service.RssFeedResponse
import com.raywenderlich.podplay.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PodcastRepo(
    private var feedService: FeedService,
    private var podcastDao: PodcastDao
) {
    fun getPodcast(feedUrl: String, callback: (Podcast?) -> Unit) {
        var podcast: Podcast? = null
        feedService.getFeed(feedUrl) { feedResponse ->
            if (feedResponse != null) {
                podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
            }
            GlobalScope.launch(Dispatchers.Main) { // updates to UI must happen on main thread (to avoid unexpected results)
                callback(podcast)
            }
        }
    }

    private fun rssResponseToPodcast(
        feedUrl: String,
        imageUrl: String,
        rssResponse: RssFeedResponse
    ): Podcast? {
        val items = rssResponse.episodes ?: return null

        val description = if (rssResponse.description == "") {
            rssResponse.summary
        } else {
            rssResponse.description
        }

        return Podcast(
            null,
            feedUrl,
            rssResponse.title,
            description,
            imageUrl,
            rssResponse.lastUpdated,
            rssItemsToEpisode(items)
        )
    }

    private fun rssItemsToEpisode(
        episodeResponses: List<RssFeedResponse.EpisodeResponse>
    ): List<Episode> {
        return episodeResponses.map {
            Episode(
                it.guid ?: "",
                null,
                it.title ?: "",
                it.description ?: "",
                it.url ?: "",
                it.type ?: "",
                DateUtils.xmlDateToDate(it.pubDate),
                it.duration ?: ""
            )
        }
    }

    fun save(podcast: Podcast) {
        GlobalScope.launch {
            val podcastId = podcastDao.insertPodcast(podcast)

            for (episode in podcast.episodes) {
                episode.podcastId = podcastId
                podcastDao.insertEpisode(episode)
            }
        }
    }

    fun getAll(): LiveData<List<Podcast>> {
        return podcastDao.loadPodcasts()
    }
}