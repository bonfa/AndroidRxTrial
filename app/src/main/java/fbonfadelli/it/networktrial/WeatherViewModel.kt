package fbonfadelli.it.networktrial

import io.reactivex.Observable

class WeatherViewModel(private val weatherRepository: WeatherRepository) {
    fun getWeatherFor(locationId: String): Observable<ViewWeather> {
        return this.weatherRepository.getWeatherFor(locationId)
            .map {
                if (it.consolidated_weather.isNotEmpty()) {
                    this.toViewModel(it.consolidated_weather.first())
                } else {
                    ViewWeather(
                        ViewMessage(false, ViewMessageContent("")),
                        ViewMessage(true, ViewMessageContent("could not find any location")),
                        ViewWeatherDetail(false, null)
                    )
                }
            }
            .onErrorReturn {
                ViewWeather(
                    ViewMessage(true, ViewMessageContent("an error occurred")),
                    ViewMessage(false, ViewMessageContent("")),
                    ViewWeatherDetail(false, null)
                )
            }
    }

    private fun toViewModel(weather: Weather) = ViewWeather(
        ViewMessage(false, ViewMessageContent("")),
        ViewMessage(false, ViewMessageContent("")),
        ViewWeatherDetail(
            true,
            ViewWeatherDetailContent(
                weatherStateName = weather.weather_state_name,
                windDirectionCompass = weather.wind_direction_compass,
                applicableDate = weather.applicable_date,
                minTemperature = "%.2f".format(weather.min_temp),
                maxTemperature = "%.2f".format(weather.max_temp),
                currentTemperature = "%.2f".format(weather.the_temp),
                windSpeed = "%.2f".format(weather.wind_speed),
                windDirection = "%.2f".format(weather.wind_direction),
                airPressure = "%.2f".format(weather.air_pressure),
                humidity = "%.2f".format(weather.humidity)
            )
        )
    )
}

data class ViewWeather(
    val errorMessage: ViewMessage,
    val emptyMessage: ViewMessage,
    val weatherDetail: ViewWeatherDetail
)

data class ViewMessage(val visible: Boolean, val content: ViewMessageContent)

data class ViewMessageContent(val message: String?)

data class ViewWeatherDetail(
    val visible: Boolean,
    val content: ViewWeatherDetailContent?
)

data class ViewWeatherDetailContent(
    val weatherStateName: String,
    val windDirectionCompass: String,
    val applicableDate: String,
    val minTemperature: String,
    val maxTemperature: String,
    val currentTemperature: String,
    val windSpeed: String,
    val windDirection: String,
    val airPressure: String,
    val humidity: String
)
