@file:Suppress("MemberVisibilityCanBePrivate")

package com.max.weatherviewer.api.weather

import retrofit2.http.GET
import retrofit2.http.Query


class WeatherProvider(private val apiKey: String, private val maxCallRate: Int) {

    private interface WeatherApi {
        // see doc ref https://openweathermap.org/current
        @GET("/data/2.5/weather")
        suspend fun fetchWeather(@Query("lat") lat: Double,
                                 @Query("lon") lon: Double,
                                 @Query("APPID") apiKey: String): Weather

    }

   // private val api: WeatherApi

    init {
        /*val client = OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            .connectTimeout(3, TimeUnit.SECONDS)
            .build()

        api = Retrofit.Builder()
            .baseUrl("http://api.openweathermap.org/data/2.5/".toHttpUrlOrNull()!!)
            //.addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(client)
            .build()
            .create(WeatherApi::class.java)*/
    }

   /* suspend fun fetchWeather(location: Location): Weather {
        return api.fetchWeather(location.lat, location.lon, apiKey)
    }*/

    /*fun fetchWeather(locations: Collection<Location>): Single<List<Weather>> {
        val delay = 60_000L / maxCallRate

        return Single.concat(locations.map { location ->
            fetchWeather(location)
                    .doOnSuccess { println("Processed %.2f%%".format(100 * (locations.indexOf(location) + 1) / locations.size.toDouble())) }
                    .delay(delay, TimeUnit.MILLISECONDS)
                    .retryWhen { errors ->
                        errors.zipWith(Flowable.range(1, 5), BiFunction<Throwable, Int, Int> { _, retry -> retry })
                                .doOnComplete { println("Giving up job...") }
                                .flatMap { retryCount ->
                                    Flowable.timer(delay * retryCount, TimeUnit.MILLISECONDS)
                                            .doOnNext { println("Retry $retryCount, for location $location. Await time: ${(delay * retryCount) / 1000L} seconds") }
                                }
                    }
        }).toList(locations.size)
    }*/

}