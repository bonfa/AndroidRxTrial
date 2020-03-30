package fbonfadelli.it.networktrial

import io.reactivex.Observable

class InMemoryWeatherCache : WeatherCache {
    private val map = mutableMapOf<String, WeatherResponse>()

    override fun load(locationId: String): Observable<WeatherResponse> {
        return Observable.just(loadOrEmpty(locationId))
    }

    private fun loadOrEmpty(locationId: String): WeatherResponse {
        var weatherResponse = map[locationId]
        if (weatherResponse == null) {
            weatherResponse = WeatherResponse(emptyList())
        }
        return weatherResponse
    }

    override fun store(weatherResponse: WeatherResponse, locationId: String) {
        map[locationId] = weatherResponse
    }
}
