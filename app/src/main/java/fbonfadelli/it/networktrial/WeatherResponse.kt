package fbonfadelli.it.networktrial

import java.io.Serializable

data class WeatherResponse(
    val consolidated_weather: List<Weather>
) : Serializable
