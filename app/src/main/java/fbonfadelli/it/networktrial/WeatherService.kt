package fbonfadelli.it.networktrial

import retrofit2.http.GET
import retrofit2.http.Path
import io.reactivex.Observable

interface WeatherService {

    @GET("/api/location/{woeid}/")
    fun get(@Path("woeid") id: String): Observable<WeatherResponse>
}
