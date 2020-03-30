package fbonfadelli.it.networktrial

import io.reactivex.Observable


class WeatherRepository(
    private val weatherService: WeatherService,
    private val weatherCache: WeatherCache
) {

    fun getWeatherFor(locationId: String): Observable<WeatherResponse> {
        return weatherCache.load(locationId)
            .concatWith(weatherService.get(locationId)
                .doOnNext {
                    if (it.consolidated_weather.isNotEmpty()) {
                        this.weatherCache.store(it, locationId)
                    }
                }
            )

    }
}

