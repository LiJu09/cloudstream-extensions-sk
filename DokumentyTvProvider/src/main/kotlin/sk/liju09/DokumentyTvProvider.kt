package sk.liju09

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor

open class DokumentyTvProvider : MainAPI() { // all providers must be an instance of MainAPI
    override var mainUrl = "https://dokumenty.tv/" 
    override var name = "Dokumenty.tv"
    override var lang = "cz"

    // enable this when your provider has a main page
    override val hasMainPage = true

    override val supportedTypes = setOf(
        TvType.Documentary
    )

    override suspend fun getMainPage(page: Int, request : MainPageRequest): HomePageResponse {
        val document = app.get(mainUrl).document
        val items = document.select(".nag div.item").mapNotNull{ it -> 
            val a = it.selectFirst("h2 a") ?: return@mapNotNull null
            val name = a.attr("title").replace("Permalink to", "").replace("-dokument", "").trim()
            val href = a.attr("href")
            val img = it.selectFirst("img")?.attr("src")
            newMovieSearchResponse(
                name,
                href,
                TvType.Documentary
            ) {
                this.posterUrl = img
            }
        }
        return HomePageResponse(listOf(HomePageList("Najnovšie", items, isHorizontalImages = true)), false)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document
        return document.select(".nag div.item").mapNotNull{ it -> 
            val a = it.selectFirst("h2 a") ?: return@mapNotNull null
            val name = a.attr("title").replace("Permalink to", "").replace("-dokument", "").trim()
            val href = a.attr("href")
            val img = it.selectFirst("img")?.attr("src")
            newMovieSearchResponse(
                name,
                href,
                TvType.Documentary
            ) {
                this.posterUrl = img
            }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val name = document.select("h1.entry-title").text().replace("-dokument", "").trim()
        val isSeries = document.select("iframe[allowfullscreen]").size != 1
        val plot = document.selectFirst(".entry-content p")?.text()?.trim()
        //val thumb = ep.selectFirst("img")?.attr("src")
        val recommendations = document.select(".nag div.item").mapNotNull{ it ->
            val a = it.selectFirst("h2 a") ?: return@mapNotNull null
            val resName = a.attr("title").replace("Permalink to", "").replace("-dokument", "").trim()
            val href = a.attr("href")
            val img = it.selectFirst("img")?.attr("src")

            newMovieSearchResponse(
                resName,
                href,
                TvType.Documentary
            ) {
                this.posterUrl = img
            }
        }

        if (isSeries) {
            val descriptions = document.select(".entry-content p").filter { it ->
                it.text().isNotEmpty()
            }

            val episodesCount = document.select("iframe[allowfullscreen]").size + 1

            val episodes = document.select("iframe[allowfullscreen]").mapIndexed{ index, ep ->

                val epLink = ep.attr("src").let { it ->
                    return@let if (it.startsWith("//")) "https:$it"
                    else it
                }

                val epIframe = app.get(epLink).document

                val epName = epIframe.selectFirst(".vid-card_n")?.text()
                    ?.replace("-dokument", "")
                    ?.replace("www.Dokumenty.TV", "")
                    ?.replace("()", "")
                    ?.trim()
                val thumb = epIframe.selectFirst(".vid-card_img")?.attr("src")

                //categories.addAll(
                //    ep.select(".episodeMeta > a[href*=\"/category/\"]").map { it.text().trim() })

                newEpisode(epLink) {
                    this.name = epName
                    this.episode = index
                    this.posterUrl = thumb
                    this.description = if (episodesCount==descriptions.size) descriptions[index].text().trim() else null
                }
            }

            Log.d("DEBUGTV", "episodecount: ${episodes.size}")
            Log.d("DEBUGTV", "descriptions: ${descriptions.size}")

            return TvSeriesLoadResponse(
                name,
                url,
                this.name,
                TvType.TvSeries,
                episodes,
                null, //image
                null,
                if (episodesCount != descriptions.size) plot else null,
                null,
                null,
                recommendations = recommendations,
                //backgroundPosterUrl = img
                //categories.toList()
            )

            } else {
                val iframe = document.selectFirst("iframe[allowfullscreen]")
                val embedUrl = iframe!!.attr("src").let {
                    return@let if (it.startsWith("//")) "https:$it"
                    else it
                }
                val embedIframe = app.get(embedUrl).document
                val img = embedIframe.selectFirst(".vid-card_img")?.attr("src")

                return MovieLoadResponse(
                    name,
                    url,
                    this.name,
                    TvType.Documentary,
                    embedUrl,
                    img,
                    null,
                    plot,
                    null,
                    recommendations = recommendations,
                    //backgroundPosterUrl = img
                    //soup.selectFirst(".videoDetails")!!.select("a[href*=\"/category/\"]")
                    //    .map { it.text().trim() }
                )
            }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        loadExtractor(data, subtitleCallback, callback)
        return true
    }
}
