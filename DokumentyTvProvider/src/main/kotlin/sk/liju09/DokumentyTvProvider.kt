package sk.liju09

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

        val title = document.select("h1.single-title").text().trim()
        val isSeries = document.select("iframe[allowfullscreen]").size != 1
        val plot = document.selectFirst(".entry-content p")?.text()?.trim()
        //val thumb = ep.selectFirst("img")?.attr("src")

        val recommendations = document.select(".releated-posts .nag div.item").mapNotNull{ it ->
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

        val episodes = if (isSeries) {
            document.select("iframe[allowfullscreen]").mapIndexed{ index, ep ->
                val thumb = ep.selectFirst("img")?.attr("src")

                val epLink = ep?.attr("src")?.let { it ->
                    return@let if (it.startsWith("//")) "https:$it"
                    else it
                }

                val season = 0
                val epNum = index

                //categories.addAll(
                //    ep.select(".episodeMeta > a[href*=\"/category/\"]").map { it.text().trim() })

                newEpisode(epLink) {
                    this.name = index.toString()
                    this.season = season
                    this.episode = epNum
                    this.posterUrl = thumb
                }
            }

            } else {
                val iframe = document.selectFirst("iframe[allowfullscreen]")
                val embedUrl = iframe!!.attr("src").let { it ->
                    return@let if (it.startsWith("//")) "https:$it"
                    else it
                }
                val img = iframe.selectFirst("img")?.attr("src")

                listOf(MovieLoadResponse(
                    title,
                    url,
                    this.name,
                    TvType.Documentary,
                    embedUrl,
                    img,
                    null,
                    plot,
                    null,
                    recommendations = recommendations,
                    backgroundPosterUrl = img
                    //soup.selectFirst(".videoDetails")!!.select("a[href*=\"/category/\"]")
                    //    .map { it.text().trim() }
                ))
            }

        return if (isSeries) TvSeriesLoadResponse(
            title,
            url,
            this.name,
            TvType.TvSeries,
            episodes.map { it as Episode },
            null, //image
            null,
            plot,
            null,
            null,
            recommendations = recommendations,
            //backgroundPosterUrl = img
            //categories.toList()
        ) else (episodes.first() as MovieLoadResponse)
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
