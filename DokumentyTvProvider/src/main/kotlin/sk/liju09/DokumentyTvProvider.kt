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

    override val mainPage = mainPageOf(
        Pair("page/", "Najnovšie"),
        Pair("category/historie/page/", "Najnovšie - Historické"),
        Pair("category/katastroficke/page/", "Najnovšie - Katastrofické"),
        Pair("category/konspirace/page/", "Najnovšie - Konspirace"),
        Pair("category/krimi/page/", "Najnovšie - Krimi"),
        Pair("category/mysleni/page/", "Najnovšie - Myšlení"),
        Pair("category/prirodovedny-dokument/page/", "Najnovšie - Příroda"),
        Pair("category/technika/page/", "Najnovšie - Technika"),
        Pair("category/vesmir/page/", "Najnovšie - Vesmír"),
        Pair("category/zahady/page/", "Najnovšie - Záhady"),
        Pair("category/zivotni-styl/page/", "Najnovšie - Životní styl"),
    )

    override val supportedTypes = setOf(
        TvType.Documentary
    )

    override suspend fun getMainPage(page: Int, request : MainPageRequest): HomePageResponse {

        val document = app.get(
            mainUrl + request.data + page.toString(),
        ).document

        val home = document.select(".nag div.item").mapNotNull {
            val a = it.selectFirst("h2 a") ?: return@mapNotNull null
            val name = a.attr("title").replace("Permalink to", "").split("-dokument")[0].trim()
            val href = a.attr("href")
            val img = it.selectFirst("img")?.attr("src")
            newMovieSearchResponse(
                name,
                href,
                TvType.Documentary
            ) {
                this.posterUrl = img
            }
        }.toList()

        return newHomePageResponse(listOf(HomePageList(request.name, home, isHorizontalImages = true)))
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document
        return document.select(".nag div.item").mapNotNull{
            val a = it.selectFirst("h2 a") ?: return@mapNotNull null
            val name = a.attr("title").replace("Permalink to", "").split("-dokument")[0].trim()
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
        val recommendations = document.select(".nag div.item").mapNotNull{
            val a = it.selectFirst("h2 a") ?: return@mapNotNull null
            val resName = a.attr("title").replace("Permalink to", "").split("-dokument")[0].trim()
            val href = a.attr("href")
            val img = it.selectFirst("img")?.attr("src")?.replace("-160x90", "")

            newMovieSearchResponse(
                resName,
                href,
                TvType.Documentary
            ) {
                this.posterUrl = img
            }
        }

        val image = document.selectFirst("meta[property=og:image]")?.attr("content")

        // all tags
        //val tags = document.select("#extras a").map { it.text() }

        // only rel=category
        val tags = document.select("#extras a").filter { it ->
            it.attr("rel").equals("category tag")
        }.map { it.text().trim() }

        if (isSeries) {
            val descriptions = document.select(".entry-content p").filter { it ->
                it.text().isNotEmpty()
            }

            val episodesCount = document.select("iframe[allowfullscreen]").size + 1

            val episodes = document.select("iframe[allowfullscreen]").mapIndexed{ index, ep ->

                val epLink = ep.attr("src").let {
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

                newEpisode(epLink) {
                    this.name = epName
                    this.episode = index
                    this.posterUrl = thumb
                    this.description = if (episodesCount==descriptions.size) descriptions[index].text().trim() else null
                }
            }

            return TvSeriesLoadResponse(
                name,
                url,
                this.name,
                TvType.TvSeries,
                episodes,
                image, //image
                null,
                if (episodesCount != descriptions.size) plot else null,
                null,
                null,
                recommendations = recommendations,
                tags = tags
            )

            } else {
                val iframe = document.selectFirst("iframe[allowfullscreen]")
                val embedUrl = iframe!!.attr("src").let {
                    return@let if (it.startsWith("//")) "https:$it"
                    else it
                }

                return MovieLoadResponse(
                    name,
                    url,
                    this.name,
                    TvType.Movie,
                    embedUrl,
                    image,
                    null,
                    plot,
                    null,
                    recommendations = recommendations,
                    tags = tags
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
