package fbonfadelli.it.networktrial

import io.reactivex.Observable
import io.reactivex.ObservableEmitter


data class WeatherViewModelProviderState(
    internal val viewModel: WeatherViewModel
)

class WeatherViewModelProvider(private val weatherRepository: WeatherRepository) {
    private var state: WeatherViewModelProviderState = WeatherViewModelProviderState(
        WeatherViewModel(
            ViewLoader.hidden(),
            ViewErrorMessage.hidden(),
            ViewWeather.hidden()
        )
    )

    fun getWeatherFor(locationId: String): Observable<WeatherViewModel> {
        return Observable.create { emitter ->
            weatherRepository
                .getWeatherFor(locationId)
                .doOnSubscribe {
                    setState(state.viewModel.copy(loader = ViewLoader.visible()))
                    emitTheViewModelInStateTo(emitter)
                }
                .doOnComplete {
                    setState(state.viewModel.copy(loader = ViewLoader.hidden()))
                    emitTheViewModelInStateTo(emitter)
                    emitter.onComplete()
                }
                .doOnError {
                    setState(
                        state.viewModel.copy(
                            errorMessage = ViewErrorMessage.visibleWith(
                                "An error occurred",
                                errorMessageFrom(it)
                            )
                        ).copy(loader = ViewLoader.hidden())
                    )
                    emitTheViewModelInStateTo(emitter)
                    emitter.onComplete()
                }
                .onErrorReturn {
                    WeatherResponse(emptyList())
                }
                .doOnNext {
                    setState(state.viewModel.copy(weather = ViewWeather.visibleWith(content(it))))
                    emitTheViewModelInStateTo(emitter)
                }
                .subscribe {
                    setState(state.viewModel.copy(weather = ViewWeather.visibleWith(content(it))))
                    emitTheViewModelInStateTo(emitter)
                }
        }
    }

    private fun emitTheViewModelInStateTo(emitter: ObservableEmitter<WeatherViewModel>) {
        emitter.onNext(this.state.viewModel)
    }

    private fun setState(viewModel: WeatherViewModel) {
        this.state = WeatherViewModelProviderState(viewModel)
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
