package com.raywenderlich.podplay.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.adapter.PodcastListAdapter
import com.raywenderlich.podplay.db.PodPlayDatabase
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.repository.PodcastRepo
import com.raywenderlich.podplay.service.FeedService
import com.raywenderlich.podplay.service.ItunesService
import com.raywenderlich.podplay.viewmodel.PodcastViewModel
import com.raywenderlich.podplay.viewmodel.SearchViewModel
import com.raywenderlich.podplay.adapter.PodcastListAdapter.PodcastListAdapterListener
import com.raywenderlich.podplay.ui.PodcastDetailsFragment.OnPodcastDetailsListener
import kotlinx.android.synthetic.main.activity_podcast.*

class PodcastActivity : AppCompatActivity(), PodcastListAdapterListener, OnPodcastDetailsListener {

    private val searchViewModel by viewModels<SearchViewModel>()
    private val podcastViewModel by viewModels<PodcastViewModel>()

    private lateinit var podcastListAdapter: PodcastListAdapter
    private lateinit var searchMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast)

        setupToolbar()
        setupViewModels()
        updateControls()
        handleIntent(intent) // reloads last intent saved in onNewIntent() -- relaunches search
        addBackStackListener()
        setupPodcastListView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)

        searchMenuItem = menu.findItem(R.id.search_item)
        val searchView = searchMenuItem.actionView as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        // in case of configuration change, prevent mixing search/detail elements
        if (supportFragmentManager.backStackEntryCount > 0) {
            podcastRecyclerView.visibility = View.INVISIBLE
        }

        // Menu is recreated when we add the Fragment menu item, so we must hide the searchMenu here
        if (podcastRecyclerView.visibility == View.INVISIBLE) {
            searchMenuItem.isVisible = false
        }

        return true
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun setupViewModels() {
        searchViewModel.iTunesRepo = ItunesRepo(ItunesService.instance)

        val db = PodPlayDatabase.getInstance(this)
        podcastViewModel.podcastRepo = PodcastRepo(FeedService.instance, db.podcastDao())
    }

    private fun updateControls() {
        podcastRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this)
        podcastRecyclerView.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
            podcastRecyclerView.context,
            layoutManager.orientation
        )

        podcastRecyclerView.addItemDecoration(dividerItemDecoration)

        podcastListAdapter = PodcastListAdapter(null, this, this)
        podcastRecyclerView.adapter = podcastListAdapter
    }


    override fun onShowDetails(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData) {
        val feedUrl = podcastSummaryViewData.feedUrl ?: return

        showProgressBar()

        podcastViewModel.getPodcast(podcastSummaryViewData) {
            hideProgressBar()
            if (it != null) {
                showDetailsFragment()
            } else {
                showError("Error loading feed $feedUrl")
            }
        }

    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok_button), null)
            .create()
            .show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEARCH) {
            val query = intent.getStringExtra(SearchManager.QUERY) ?: return
            performSearch(query)
        }
    }

    private fun performSearch(term: String) {
        showProgressBar()
        searchViewModel.searchPodcasts(term) { results ->
            hideProgressBar()
            toolbar.title = term
            podcastListAdapter.setSearchData(results)
        }
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.INVISIBLE
    }

    private fun createPodcastDetailsFragment(): PodcastDetailsFragment {
        val podcastDetailsFragment = supportFragmentManager
            .findFragmentByTag(TAG_DETAILS_FRAGMENT) as PodcastDetailsFragment?

        return podcastDetailsFragment ?: PodcastDetailsFragment.newInstance()
    }

    private fun showDetailsFragment() {
        val podcastDetailsFragment = createPodcastDetailsFragment()

        supportFragmentManager.beginTransaction()
            .add(
                R.id.podcastDetailsContainer,
                podcastDetailsFragment,
                TAG_DETAILS_FRAGMENT
            )
            .addToBackStack(TAG_DETAILS_FRAGMENT).commit()

        podcastRecyclerView.visibility = View.INVISIBLE

        searchMenuItem.isVisible = false
    }

    private fun addBackStackListener() {
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                podcastRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun setupPodcastListView() {
        podcastViewModel.getPodcasts()?.observe(this, Observer {
            if (it != null) {
                showSubscribedPodcasts()
            }
        })
    }

    private fun showSubscribedPodcasts() {
        val podcasts = podcastViewModel.getPodcasts()?.value

        if (podcasts != null) {
            toolbar.title = getString(R.string.subscribed_podcasts)
            podcastListAdapter.setSearchData(podcasts)
        }
    }

    companion object {
        private const val TAG = "PodcastActivity"
        private const val TAG_DETAILS_FRAGMENT = "DetailsFragment"
    }

    override fun onSubscribe() {
        podcastViewModel.saveActivePodcast()
        supportFragmentManager.popBackStack()
    }

    override fun onUnsubscribe() {
        podcastViewModel.deleteActivePodcast()
        supportFragmentManager.popBackStack()
    }
}
