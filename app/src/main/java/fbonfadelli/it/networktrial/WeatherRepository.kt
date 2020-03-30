package fbonfadelli.it.networktrial

import io.reactivex.Observable


class WeatherRepository(private val weatherService: WeatherService) {
    fun getWeatherFor(locationId: String): Observable<WeatherResponse> {
        return weatherService.get(locationId)
    }
}

