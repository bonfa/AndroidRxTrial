package fbonfadelli.it.networktrial

import io.reactivex.Observable


class RxWeatherService(private val weatherApi: WeatherApi) {
    fun getWeatherFor(locationId: String): Observable<WeatherResponse> {
        return weatherApi.get(locationId)
    }
}

