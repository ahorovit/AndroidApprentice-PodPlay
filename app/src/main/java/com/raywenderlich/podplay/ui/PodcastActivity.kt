package com.raywenderlich.podplay.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.widget.SearchView
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.service.ItunesService

class PodcastActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)

        val searchMenuItem = menu.findItem(R.id.search_item)
        val searchView = searchMenuItem?.actionView as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        return true
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
        val itunesService = ItunesService.instance
        val itunesRepo = ItunesRepo(itunesService)

        itunesRepo.searchByTerm(term) {
            Log.i(TAG, "Results = $it")
        }
    }



    companion object {
        private const val TAG = "PodcastActivity"
    }
}
