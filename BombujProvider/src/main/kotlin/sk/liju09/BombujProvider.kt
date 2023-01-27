package sk.liju09

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.Jsoup

open class BombujProvider : MainAPI() { // all providers must be an instance of MainAPI
    final override var mainUrl = "https://bombuj.si/"
    var mainSeriesUrl = "https://serialy.bombuj.si/"
    override var name = "Bombuj"
    override var lang = "sk"

    // enable this when your provider has a main page
    override val hasMainPage = true

    override val mainPage = mainPageOf(
        // movies
        Pair("${mainUrl}zanre/all.php?page=PAGE&sort=id&title=1&zaner=all", "Najnovšie Filmy"),
        Pair("${mainUrl}zanre/index3.php?page=PAGE&sort=id&title=1&zaner=popularne", "Najnovšie Filmy - POPULÁRNE"),
        Pair("${mainUrl}zanre/index3.php?page=PAGE&sort=id&title=1&zaner=skutocny", "Najnovšie Filmy - SKUTOČNÁ UDALOSŤ"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Akční", "Najnovšie Filmy - AKČNÉ"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Dobrodružný", "Najnovšie Filmy - DOBRODRUŽNÉ"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Animovaný", "Najnovšie Filmy - ANIMOVANÉ"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Komedie", "Najnovšie Filmy - KOMÉDIE"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Krimi", "Najnovšie Filmy - KRIMI"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Drama", "Najnovšie Filmy - DRÁMA"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Rodinný", "Najnovšie Filmy - RODINNÉ"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Horor", "Najnovšie Filmy - HOROR"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Romantický", "Najnovšie Filmy - ROMANTICKÉ"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Sci-Fi", "Najnovšie Filmy - SCI-FI"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Thriller", "Najnovšie Filmy - THRILLER"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Fantasy", "Najnovšie Filmy - FANTASY"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Životopisný", "Najnovšie Filmy - ŽIVOTOPISNÉ"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Mysteriózní", "Najnovšie Filmy - MYSTERIÓZNE"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Dokumentární", "Najnovšie Filmy - DOKUMENTÁRNE"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Sportovní", "Najnovšie Filmy - ŠPORTOVÉ"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Válečný", "Najnovšie Filmy - VOJNOVÉ"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Historický", "Najnovšie Filmy - HISTORICKÉ"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Psychologický", "Najnovšie Filmy - PSYCHOLOGICKÉ"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Pohádka", "Najnovšie Filmy - ROZPRÁVKY"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Katastrofický", "Najnovšie Filmy - KATASTROFICKÉ"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Hudební", "Najnovšie Filmy - HUDOBNÉ"),
        Pair("${mainUrl}zanre/index.php?page=PAGE&sort=id&title=1&zaner=Western", "Najnovšie Filmy - WESTERN"),
        Pair("${mainUrl}zanre/index3.php?page=PAGE&sort=id&title=1&zaner=odp", "Najnovšie Filmy - ODPORÚČANÉ"),
        Pair("${mainUrl}zanre/index3.php?page=PAGE&sort=id&title=1&zaner=dystopia", "Najnovšie Filmy - DYSPOTICKÉ"),
        Pair("${mainUrl}zanre/index3.php?page=PAGE&sort=id&title=1&zaner=fb_comm", "Najnovšie Filmy - VIANOČNÉ"),

        //series
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=all&sort=id&nazov=1", "Najnovšie Seriály"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Akční&sort=id&nazov=1", "Najnovšie Seriály - AKČNÉ"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Dobrodružný&sort=id&nazov=1", "Najnovšie Seriály - DOBRODRUŽNÉ"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Animovaný&sort=id&nazov=1", "Najnovšie Seriály - ANIMOVANÉ"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Komedie&sort=id&nazov=1", "Najnovšie Seriály - KOMÉDIE"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Krimi&sort=id&nazov=1", "Najnovšie Seriály - KRIMI"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Drama&sort=id&nazov=1", "Najnovšie Seriály - DRÁMA"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Rodinný&sort=id&nazov=1", "Najnovšie Seriály - RODINNÉ"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Horor&sort=id&nazov=1", "Najnovšie Seriály - HOROR"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Romantický&sort=id&nazov=1", "Najnovšie Seriály - ROMANTICKÉ"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Telenovela&sort=id&nazov=1", "Najnovšie Seriály - TELENOVELA"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Reality-TV&sort=id&nazov=1", "Najnovšie Seriály - REALITY-TV"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Sci-Fi&sort=id&nazov=1", "Najnovšie Seriály - SCI-FI"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Thriller&sort=id&nazov=1", "Najnovšie Seriály - THRILLER"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Fantasy&sort=id&nazov=1", "Najnovšie Seriály - FANTASY"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Životopisný&sort=id&nazov=1", "Najnovšie Seriály - ŽIVOTOPISNÉ"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Mysteriózní&sort=id&nazov=1", "Najnovšie Seriály - MYSTERIÓZNE"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Dokumentární&sort=id&nazov=1", "Najnovšie Seriály - DOKUMENTÁRNE"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Sportovní&sort=id&nazov=1", "Najnovšie Seriály - ŠPORTOVÉ"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Válečný&sort=id&nazov=1", "Najnovšie Seriály - VOJNOVÉ"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Historický&sort=id&nazov=1", "Najnovšie Seriály - HISTORICKÉ"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Psychologický&sort=id&nazov=1", "Najnovšie Seriály - PSYCHOLOGICKÉ"),
        Pair("${mainSeriesUrl}zoznam-serialov/podla-zanru/index.php?page=PAGE&zaner=Western&sort=id&nazov=1", "Najnovšie Seriály - WESTERN"),
    )

    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries
    )

    override suspend fun getMainPage(page: Int, request : MainPageRequest): HomePageResponse {

        val isSeries = request.data.contains("zoznam-serialov")
        val document = app.get(
            request.data.replace("PAGE", page.toString()),
        ).document

        val home : List<SearchResponse>
        if (isSeries) {
            home = document.select("#podla-zanru > div:nth-child(2) > div").mapNotNull {
                val a = it.selectFirst("div:nth-child(2) > div:nth-child(1) > a") ?: return@mapNotNull null
                val name = it.select("div:nth-child(2) > div:nth-child(2) > div:nth-child(1) > a").text().trim()
                val href = a.attr("href").let { url ->
                    return@let if (url.startsWith("//")) "https:$url"
                    else url
                }
                val img = a.selectFirst("img")!!.attr("src").let { imgUrl ->
                    return@let if (imgUrl.startsWith("//")) "https:$imgUrl"
                    else imgUrl
                }
                newTvSeriesSearchResponse(
                    name,
                    href,
                    TvType.TvSeries
                ) {
                    this.posterUrl = img
                }
            }.toList()
        } else {
            home = document.select("div.hlavne_str").mapNotNull {
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
        }
        return newHomePageResponse(listOf(HomePageList(request.name, home)))
    }

    override suspend fun search(query: String): List<SearchResponse>? {
        val url = "${mainUrl}android_api/search_film.php?q=$query"
        val document = app.get(url).text
        return tryParseJson<ResponseSearchList>(document)?.results?.map {
            val name = it.name
            val id = it.id
            val movie = tryParseJson<ResponseMovieList>(app.get("${mainUrl}android_api/filmy/getfilmjson.php?id=$id").text)?.film?.get(0)
            val href = "${mainUrl}online-film-${movie?.url}"
            val img = "${mainUrl}images/covers/all/${movie?.url}.jpg"

            newMovieSearchResponse(
                name,
                href,
                TvType.Movie
            ) {
                this.posterUrl = img
            }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val isSeries = url.contains(mainSeriesUrl)

        if (isSeries) {
            // why?
            val name = document.select("#serial > div:nth-child(1) > div:nth-child(1) > div:nth-child(4) > div:nth-child(1) > div:nth-child(1) > h1:nth-child(1) > a").text().trim()
            val plot = document.selectFirst(".more_obsah")?.text()?.replace("... ", "")?.replace("(viac)", "")?.trim()

            val image = document.selectFirst("meta[property=og:image]")!!.attr("content").let {
                return@let if (it.startsWith("//")) "https:$it"
                else it
            }

            val tags = document.select("#serial > div:nth-child(1) > div:nth-child(1) > div:nth-child(4) > div:nth-child(1) > div:nth-child(1) > div:nth-child(4) > div:nth-child(2)").text().split("/").map { it.trim() }

            val seasons = document.select(".table_cele")
            val episodeList = ArrayList<Episode>()

            for (season in seasons) {
                val seasonDoc = app.get(season.select("a").attr("href").let { return@let if (it.startsWith("//")) "https:$it" else it }).document
                val seasonNum = seasonDoc.select(".seria").text().trim().substringBefore(".").replace("Séria ", "").toInt()

                val episodes = seasonDoc.select(".epizody a").map {
                    it.attr("href").let { itt -> return@let if (itt.startsWith("//")) "https:$itt" else itt }
                }
                episodes.map {
                    episodeList.add(
                    newEpisode(it) {
                        //this.name = name
                        this.season = seasonNum
                        this.episode = it.substringAfterLast("x").toInt()
                    }
                ) }
            }
            return TvSeriesLoadResponse(
                name,
                url,
                this.name,
                TvType.TvSeries,
                episodeList,
                image,
                null,
                plot,
                null,
                tags = tags
            )
        } else {
            val name = document.select(".cele_info a h1").text().trim()
            val plot = document.selectFirst(".more")?.text()?.replace("... ", "")?.replace("(viac)", "")?.trim()

            val recommendations = document.select("#odpfilms > div > div").mapNotNull{
                val recName = it.text().trim()
                val href = it.selectFirst("a")!!.attr("href").let { recUrl ->
                    return@let if (recUrl.startsWith("//")) "https:$recUrl"
                    else recUrl
                }
                val img = it.select("img").attr("src").let { imgUrl ->
                    return@let if (imgUrl.startsWith("//")) "https:$imgUrl"
                    else imgUrl
                }

                newMovieSearchResponse(
                    recName,
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

            val sources = document.select("li.selectedis").toString()

            return MovieLoadResponse(
                name,
                url,
                this.name,
                TvType.Movie,
                sources,
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
        Jsoup.parse(data).select("li.selectedis").apmap { source ->
            val link = source.attr("link").let {
                return@let if (it.startsWith("//")) "https:$it"
                else it
            }
            val src = app.get(link).document.select("iframe").attr("src")
            loadExtractor(src, "", subtitleCallback, callback)
        }
        return true
    }

    private data class ResponseSearchList(
        @JsonProperty("results") val results: List<ResponseSearch>,
    )

    private data class ResponseSearch(
        @JsonProperty("name") val name: String,
        @JsonProperty("id") val id: String,
    )

    private data class ResponseMovieList(
        @JsonProperty("film") val film: List<ResponseMovie>,
    )

    private data class ResponseMovie(
        @JsonProperty("id"               ) var id             : String? = null,
        @JsonProperty("name"             ) var name           : String? = null,
        @JsonProperty("entry_date"       ) var entryDate      : String? = null,
        @JsonProperty("active"           ) var active         : String? = null,
        @JsonProperty("description"      ) var description    : String? = null,
        @JsonProperty("year"             ) var year           : String? = null,
        @JsonProperty("url"              ) var url            : String? = null,
        @JsonProperty("views"            ) var views          : String? = null,
        @JsonProperty("viewtoday"        ) var viewtoday      : String? = null,
        @JsonProperty("viewtoday_date"   ) var viewtodayDate  : String? = null,
        @JsonProperty("todayviews"       ) var todayviews     : String? = null,
        @JsonProperty("herci"            ) var herci          : String? = null,
        @JsonProperty("hodnotenie"       ) var hodnotenie     : String? = null,
        @JsonProperty("reziser"          ) var reziser        : String? = null,
        @JsonProperty("zanre"            ) var zanre          : String? = null,
        @JsonProperty("skutocny"         ) var skutocny       : String? = null,
        @JsonProperty("name2"            ) var name2          : String? = null,
        @JsonProperty("type"             ) var type           : String? = null,
        @JsonProperty("odp"              ) var odp            : String? = null,
        @JsonProperty("name_eng"         ) var nameEng        : String? = null,
        @JsonProperty("dystopia"         ) var dystopia       : String? = null,
        @JsonProperty("dab_novinka"      ) var dabNovinka     : String? = null,
        @JsonProperty("dab_novinka_date" ) var dabNovinkaDate : String? = null,
        @JsonProperty("popularne"        ) var popularne      : String? = null,
        @JsonProperty("fb_comm"          ) var fbComm         : String? = null,
        @JsonProperty("csfd_id"          ) var csfdId         : String? = null
    )
}
