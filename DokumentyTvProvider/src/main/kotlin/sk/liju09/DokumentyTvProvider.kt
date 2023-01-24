package sk.liju09

import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse

class DokumentyTvProvider : MainAPI() { // all providers must be an instance of MainAPI
    override var mainUrl = "https://dokumenty.tv/" 
    override var name = "Dokumenty.tv"
    override val supportedTypes = setOf(TvType.Movie)

    override var lang = "en"

    // enable this when your provider has a main page
    override val hasMainPage = true

    override val supportedTypes = setOf(
        TvType.Documentary
    )

    override suspend fun getMainPage(page: Int, request : MainPageRequest): HomePageResponse {
        val document = app.get(mainUrl).document
        val items = document.select(".nag div.item").mapNotNull{ it -> 
            val a = it.selectFirst("h2 a") ?: return@mapNotNull null
            val name = a.attr("title").trim()
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
        return document.select("article.cactus-post-item").mapNotNull{ it -> 
            val a = it.selectFirst("h3 a") ?: return@mapNotNull null
            val name = a.attr("title").trim()
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

        val embedUrl = document.selectFirst("iframe[allowfullscreen]")?.attr("src")?.let { it ->
            return@let if (it.startsWith("//")) "https:$it"
            else it
        }
        val title = document.select("h1.single-title").text().trim()

        val plot = document.select(".single-post-content p").text().trim()
        
        return newMovieLoadResponse(title, url, TvType.Documentary, embedUrl) {
            this.plot = plot
            this.recommendations = document.select(".post-list-in-single article.cactus-post-item").mapNotNull{ it -> 
                val a = it.selectFirst("h3 a") ?: return@mapNotNull null
                val name = a.attr("title").trim()
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

data class LinkElement(
    @JsonProperty("src") val src: String
)