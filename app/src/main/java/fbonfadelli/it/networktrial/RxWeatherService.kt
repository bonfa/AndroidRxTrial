package fbonfadelli.it.networktrial

import fbonfadelli.it.networktrial.WeatherServiceFactory.Companion.make
import io.reactivex.Observable


class RxWeatherService {
    private val weatherApi: WeatherApi = make()

    fun getWeatherFor(locationId: String): Observable<WeatherResponse> {
        return weatherApi.get(locationId)
    }
}

