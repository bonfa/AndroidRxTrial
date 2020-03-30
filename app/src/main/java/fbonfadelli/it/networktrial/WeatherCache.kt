package fbonfadelli.it.networktrial

import io.reactivex.Observable

class WeatherCache {
    private val map = mutableMapOf<String, WeatherResponse>()

    fun load(locationId: String): Observable<WeatherResponse> {
        return Observable.just(loadOrEmpty(locationId))
    }

    private fun loadOrEmpty(locationId: String): WeatherResponse {
        var weatherResponse = map[locationId]
        if (weatherResponse == null) {
            weatherResponse = WeatherResponse(emptyList())
        }
        return weatherResponse
    }

    fun store(weatherResponse: WeatherResponse, locationId: String) {
        map[locationId] = weatherResponse
    }
}
