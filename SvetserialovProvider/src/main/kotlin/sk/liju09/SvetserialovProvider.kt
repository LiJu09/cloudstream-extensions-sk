package sk.liju09

import android.util.Base64
import android.util.Log
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.getAndUnpack
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.Jsoup
import com.lagradost.cloudstream3.APIHolder.getCaptchaToken

open class SvetserialovProvider : MainAPI() { // all providers must be an instance of MainAPI
    final override var mainUrl = "https://svetserialov.to/"
    override var name = "Svetserialov"
    override var lang = "sk"

    // enable this when your provider has a main page
    override val hasMainPage = true

    override val mainPage = mainPageOf(
        Pair("${mainUrl}zoznam-serialov?ajaxLoad=true&page=PAGE", "Najnovšie Seriály"),
    )

    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries
    )

    override suspend fun getMainPage(page: Int, request : MainPageRequest): HomePageResponse {

        val document = app.get(
            request.data.replace("PAGE", page.toString()),
            headers = mapOf(
                // newest
                "Cookie" to "filterOrderBy=3",
            )
        ).document

        val home : List<SearchResponse>

        home = document.select(".div-single-result").map {
            val name = it.select("span.original").text().trim()
            val href = it.select("a").attr("href").substringAfter("/")
            val img = it.select("img").attr("src").substringAfter("/")
            newTvSeriesSearchResponse(
                name,
                mainUrl + href,
                TvType.TvSeries
            ) {
                this.posterUrl = "$mainUrl$img"
            }
        }

        return newHomePageResponse(listOf(HomePageList(request.name, home)))
    }

    override suspend fun search(query: String): List<SearchResponse>? {
        val movieUrl = "${mainUrl}android_api/search_film.php?q=$query"
        val movieDocument = app.get(movieUrl).text
        val movies = tryParseJson<ResponseMovieSearchList>(movieDocument)?.results?.map {
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
        val seriesUrl = "${mainUrl}android_api/search_serial.php?q=$query"
        val seriesDocument = app.get(seriesUrl).text
        val series = tryParseJson<ResponseSeriesSearchList>(seriesDocument)?.results?.map {
            val name = it.one
            val id = it.id
            val serie = tryParseJson<ResponseSeriesList>(app.get("${mainUrl}android_api/serialy/getserialjson.php?id=$id").text)?.serial?.get(0)
            val href = "${mainUrl}serial-${serie?.url}"
            val img = "${mainUrl}images/covers/${serie?.url}.jpg"

            newTvSeriesSearchResponse(
                name,
                href,
                TvType.TvSeries
            ) {
                this.posterUrl = img
            }
        }
        val results = ArrayList<SearchResponse>()
        if (movies != null) {
            results.addAll(movies)
        }
        if (series != null) {
            results.addAll(series)
        }
        return results
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val name = document.select("h1.nunito").text().trim()
        val plot = document.select(".show-text").text().trim()

        val image = document.select(".show-image img").attr("src").let { mainUrl + it.substringAfter("/") }

        val tags = document.select("span.single-genre").map { it.text().trim() }

        val year = document.select(".year").text().let {
            return@let if (it.contains("-")) it.substringBefore("-").trim().toInt()
            else it.toInt()
        }

        val seasons = document.select("div.series div.span6")
        val episodeList = ArrayList<Episode>()
        for (season in seasons) {
            val seasonNum = season.select("h2").text().trim().substringBefore(".").toInt()
            val accId = season.select("div.accordion").attr("class").substringAfter("accordionId").substringBefore(" ")
            val episodes = app.get("$url?loadAccordionId=$accId").document.select("a")
            episodes.map {
                val epName = it.select("ep_name").text().trim()
                //val epNum = it.select("number_eps ").text().trim().toInt()
//                val sources = app.get(it.attr("href").let { epit -> mainUrl + epit.substringAfter("/") }).document.select("ul.nunito a").map { epSrc ->
//                    app.get(mainUrl + Base64.decode(epSrc.attr("data-iframe"), Base64.URL_SAFE).toString().substringAfter("/")).document.select("iframe").attr("src")
//                }.toList()
                val epUrl = it.attr("href").let { epit -> mainUrl + epit.substringAfter("/") }
                Log.d("SVSEPURL", epUrl)
                val sources = app.get(epUrl).document.select("ul.nunito a").map { epSrc ->
                    val urll = mainUrl + Base64.decode(epSrc.attr("data-iframe"), Base64.DEFAULT).toString(Charsets.UTF_8).substringAfter("/")
                    val toToken = app.get(
                        urll,
                        referer = epUrl
                    ).document.toString()

                    val token = toToken.substringAfter("'sitekey': '").substringBefore("',").let { captchaKey ->
                        getCaptchaToken(
                            urll,
                            captchaKey,
                            referer = urll
                        )
                    } ?: throw ErrorLoadingException("can't bypass captcha")

                    val data = app.post(
                        urll,
                        data = mapOf(
                            "g-recaptcha-response" to token
                        ),
                        referer = urll,
                        headers = mapOf(
                            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
                            "Origin" to "https://svetserialov.to",
                            "Alt-Used" to "svetserialov.to",
                            "Sec-Fetch-Dest" to "iframe",
                            "Sec-Fetch-Mode" to "navigate",
                            "Sec-Fetch-Site" to "same-origin",
                            "Referer" to urll,
                            "User-Agent" to "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:109.0) Gecko/20100101 Firefox/109.0"
                        )
                    ).document

                    Log.d("SVSdata", token.toString())

                    data.select("iframe").attr("src")
                }
                Log.d("SVSSRCS", sources.toString())
                episodeList.add(
                    newEpisode(sources) {
                        this.name = epName
                        this.season = seasonNum
                        //this.episode = epNum
                    }
                )
            }
        }
        return TvSeriesLoadResponse(
            name,
            url,
            this.name,
            TvType.TvSeries,
            episodeList,
            image,
            year,
            plot,
            null,
            tags = tags,
//            actors = actors,
        )

    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        loadExtractor(data, data, subtitleCallback, callback)
        return true
    }

    private data class ResponseMovieSearchList(
        @JsonProperty("results") val results: List<ResponseMovieSearch>,
    )

    private data class ResponseMovieSearch(
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

    private data class ResponseSeriesSearchList(
        @JsonProperty("results_serialy") val results: List<ResponseSeriesSearch>,
    )

    private data class ResponseSeriesSearch(
        @JsonProperty("0") val zero: String,
        @JsonProperty("id") val id: String,
        @JsonProperty("1") val one: String,
        @JsonProperty("nazov_1") val nazov_1: String,
    )

    private data class ResponseSeriesList(
        @JsonProperty("serial") val serial: List<ResponseSeries>,
    )

    private data class ResponseSeries(
        @JsonProperty("id"             ) var id            : String? = null,
        @JsonProperty("active"         ) var active        : String? = null,
        @JsonProperty("nazov_1"        ) var nazov1        : String? = null,
        @JsonProperty("nazov_2"        ) var nazov2        : String? = null,
        @JsonProperty("nazov_3"        ) var nazov3        : String? = null,
        @JsonProperty("url"            ) var url           : String? = null,
        @JsonProperty("rok"            ) var rok           : String? = null,
        @JsonProperty("zaner"          ) var zaner         : String? = null,
        @JsonProperty("krajina"        ) var krajina       : String? = null,
        @JsonProperty("reziser"        ) var reziser       : String? = null,
        @JsonProperty("herci"          ) var herci         : String? = null,
        @JsonProperty("obsah"          ) var obsah         : String? = null,
        @JsonProperty("hodnotenie"     ) var hodnotenie    : String? = null,
        @JsonProperty("views"          ) var views         : String? = null,
        @JsonProperty("todayviews"     ) var todayviews    : String? = null,
        @JsonProperty("viewtoday"      ) var viewtoday     : String? = null,
        @JsonProperty("viewtoday_date" ) var viewtodayDate : String? = null,
        @JsonProperty("dab"            ) var dab           : String? = null
    )
}
