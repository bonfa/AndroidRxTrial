package fbonfadelli.it.networktrial

import io.reactivex.Observable

class WeatherViewModelProvider(private val weatherRepository: WeatherRepository) {
    private var viewModel = WeatherViewModel(
        ViewLoader.hidden(),
        ViewErrorMessage.hidden(),
        ViewWeather.hidden()
    )


    fun getWeatherFor(locationId: String): Observable<WeatherViewModel> {
        return Observable.create { emitter ->
            weatherRepository
                .getWeatherFor(locationId)
                .doOnSubscribe {
                    this.viewModel = this.viewModel.copy(loader = ViewLoader.visible())
                    emitter.onNext(this.viewModel)
                }
                .doOnComplete {
                    this.viewModel = this.viewModel.copy(loader = ViewLoader.hidden())
                    emitter.onNext(this.viewModel)
                    emitter.onComplete()
                }
                .doOnError {
                    this.viewModel = this.viewModel.copy(
                        errorMessage = ViewErrorMessage.visibleWith(
                            "An error occurred",
                            errorMessageFrom(it)
                        )
                    ).copy(loader = ViewLoader.hidden())
                    emitter.onNext(this.viewModel)
                    emitter.onComplete()
                }
                .onErrorReturn {
                    WeatherResponse(emptyList())
                }
                .doOnNext {
                    this.viewModel =
                        this.viewModel.copy(weather = ViewWeather.visibleWith(content(it)))
                    emitter.onNext(this.viewModel)
                }
                .subscribe {
                    this.viewModel =
                        this.viewModel.copy(weather = ViewWeather.visibleWith(content(it)))
                    emitter.onNext(this.viewModel)
                }
        }
    }

    private fun content(it: WeatherResponse) =
        it.consolidated_weather.firstOrNull()?.toString().orEmpty()

    private fun errorMessageFrom(it: Throwable): String =
        if (it.message != null) it.message!! else ""
}

data class WeatherViewModel(
    val loader: ViewLoader,
    val errorMessage: ViewErrorMessage,
    val weather: ViewWeather
)

data class ViewLoader private constructor(val visible: Boolean) {
    companion object {
        fun hidden(): ViewLoader {
            return ViewLoader(false)
        }

        fun visible(): ViewLoader {
            return ViewLoader(true)
        }

    }
}

data class ViewWeather private constructor(val visible: Boolean, val content: String) {
    companion object {
        fun hidden(): ViewWeather {
            return ViewWeather(false, "")
        }

        fun visibleWith(content: String): ViewWeather {
            return ViewWeather(true, content)
        }
    }
}

data class ViewErrorMessage private constructor(
    val visible: Boolean,
    val messageForUser: String,
    val messageForLogger: String
) {
    companion object {
        fun hidden(): ViewErrorMessage {
            return ViewErrorMessage(false, "", "")
        }

        fun visibleWith(messageForUser: String, messageForLogger: String): ViewErrorMessage {
            return ViewErrorMessage(true, messageForUser, messageForLogger)
        }
    }
}
