package com.raywenderlich.podplay.service

import com.raywenderlich.podplay.util.DateUtils
import okhttp3.*
import org.w3c.dom.Node
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory

class RssFeedService: FeedService {
    override fun getFeed(xmlFileURL: String, callback: (RssFeedResponse?) -> Unit) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(xmlFileURL)
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body()?.let {

                        val dbFactory = DocumentBuilderFactory.newInstance()
                        val dBuilder = dbFactory.newDocumentBuilder()
                        val doc = dBuilder.parse(it.byteStream())
                        val rssFeedResponse = RssFeedResponse(episodes = mutableListOf())

                        domToRssFeedResponse(doc, rssFeedResponse)
                        callback(rssFeedResponse)
//                        println(rssFeedResponse)
                        return
                    }
                }

                callback(null)
            }
        })
    }

    private fun domToRssFeedResponse(node: Node, rssFeedResponse: RssFeedResponse) {
        if (node.nodeType == Node.ELEMENT_NODE) {
            val nodeName = node.nodeName
            val parentName = node.parentNode.nodeName
            val grandParentName = node.parentNode.parentNode?.nodeName ?: ""

            if (parentName == "item" && grandParentName == "channel") {
                val currentItem = rssFeedResponse.episodes?.last()

                if (currentItem != null) {
                    when (nodeName) {
                        "title" -> currentItem.title = node.textContent
                        "description" -> currentItem.description = node.textContent
                        "guid" -> currentItem.guid = node.textContent
                        "pubDate" -> currentItem.pubDate = node.textContent
                        "itunes:duration" -> currentItem.duration = node.textContent
                        "link" -> currentItem.link = node.textContent
                        "enclosure" -> {
                            currentItem.url = node.attributes.getNamedItem("url").textContent
                            currentItem.type = node.attributes.getNamedItem("type").textContent
                        }
                    }
                }
            }

            if (parentName == "channel") {
                when (nodeName) {
                    "title" -> rssFeedResponse.title = node.textContent
                    "description" -> rssFeedResponse.description = node.textContent
                    "itunes:summary" -> rssFeedResponse.summary = node.textContent
                    "item" -> rssFeedResponse.episodes?.add(RssFeedResponse.EpisodeResponse())
                    "pubDate" -> rssFeedResponse.lastUpdated = DateUtils.xmlDateToDate(node.textContent)
                }
            }
        }

        val nodeList = node.childNodes
        for (i in 0 until nodeList.length) {
            val childNode = nodeList.item(i)

            domToRssFeedResponse(childNode, rssFeedResponse)
        }
    }

}

interface FeedService {
    fun getFeed(xmlFileURL: String, callback: (RssFeedResponse?) -> Unit)

    companion object {
        val instance: FeedService by lazy {
            RssFeedService()
        }
    }
}