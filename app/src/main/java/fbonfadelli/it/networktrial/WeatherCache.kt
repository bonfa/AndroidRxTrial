package fbonfadelli.it.networktrial

import io.reactivex.Observable

interface WeatherCache {
    fun load(locationId: String): Observable<WeatherResponse>
    fun store(weatherResponse: WeatherResponse, locationId: String)
}