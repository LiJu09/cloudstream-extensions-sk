package sk.liju09

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor

open class BombujProvider : MainAPI() { // all providers must be an instance of MainAPI
    override var mainUrl = "https://bombuj.si/"
    var mainSeriesUrl = "https://serialy.bombuj.si/"
    override var name = "Bombuj"
    override var lang = "sk"

    // enable this when your provider has a main page
    override val hasMainPage = true

    override val mainPage = mainPageOf(
        Pair("/zanre/all.php?page=1&sort=id&title=1&zaner=all", "Najnovšie"),
    )

    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries
    )

    override suspend fun getMainPage(page: Int, request : MainPageRequest): HomePageResponse {

        val params = mapOf(
            "page" to page.toString(),
            "sort" to "id",
            "title" to "1",
            "zaner" to "all"
        )
        val document = app.get(
            mainUrl + request.data,
            params = params
        ).document

        val home = document.select("div.hlavne_str").mapNotNull {
            val a = it.selectFirst(".lava_strana_str a") ?: return@mapNotNull null
            val name = a.selectFirst("img")?.attr("alt")!!.trim()
            val href = a.attr("href")
            val img = a.selectFirst("img")!!.attr("src").let { imgUrl ->
                return@let if (imgUrl.startsWith("//")) "https:$imgUrl"
                else imgUrl
            }
            newMovieSearchResponse(
                name,
                href,
                TvType.Movie
            ) {
                this.posterUrl = img
            }
        }.toList()

        return newHomePageResponse(listOf(HomePageList(request.name, home)))
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document
        return document.select(".nag div.item").mapNotNull{
            val a = it.selectFirst("h2 a") ?: return@mapNotNull null
            val name = a.attr("title").replace("Permalink to", "").split("-dokument")[0].trim()
            val href = a.attr("href")
            val img = "https:" + a.selectFirst("img")?.attr("src")

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

        val name = document.select(".cele_info a h1").text().trim()
        val plot = document.selectFirst(".more")?.text()?.replace("... ", "")?.replace("(viac)", "")?.trim()

        // TODO
        val recommendations = document.select(".nag div.item").mapNotNull{
            val a = it.selectFirst("h2 a") ?: return@mapNotNull null
            val resName = a.attr("title").replace("Permalink to", "").split("-dokument")[0].trim()
            val href = a.attr("href")
            val img = it.selectFirst("img")?.attr("src")?.replace("-160x90", "")

            newMovieSearchResponse(
                resName,
                href,
                TvType.Movie
            ) {
                this.posterUrl = img
            }
        }

        val image = document.selectFirst("meta[property=og:image]")!!.attr("content").let {
            return@let if (it.startsWith("//")) "https:$it"
            else it
        }

        val tags = document.select("h3.zaner_filmu").text().split(" - ")[1].trim().split("/").map { it.trim() }

        val sources = document.select("li.selectedis")
        val source = sources[0]

        val embedUrl = source.attr("link").let {
            return@let if (it.startsWith("//")) "https:$it"
            else it
        }

        val src = app.get(embedUrl).document.select("iframe").attr("src")

        return MovieLoadResponse(
            name,
            url,
            this.name,
            TvType.Movie,
            src,
            image,
            null,
            plot,
            null,
            recommendations = recommendations,
            tags = tags
        )

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
