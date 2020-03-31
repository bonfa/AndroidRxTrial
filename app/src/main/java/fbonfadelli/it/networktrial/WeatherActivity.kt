package fbonfadelli.it.networktrial

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class WeatherActivity : AppCompatActivity() {

    private lateinit var details: TextView
    private lateinit var reloadButton: MaterialButton
    private lateinit var resetViewButton: MaterialButton
    private lateinit var progress: ProgressBar
    private lateinit var subscription: Disposable
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var weatherViewModelProvider: WeatherViewModelProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        weatherRepository =
            WeatherRepository(RxWeatherServiceFactory.make(), WeatherAndroidCache(this))
        weatherViewModelProvider = WeatherViewModelProvider(weatherRepository)
        details = findViewById(R.id.detail)
        reloadButton = findViewById(R.id.reloadButton)
        resetViewButton = findViewById(R.id.resetViewButton)
        progress = findViewById(R.id.progress)
        reloadButton.setOnClickListener { loadWeatherData() }
        resetViewButton.setOnClickListener { details.text = "" }
    }

    override fun onStart() {
        super.onStart()
        loadWeatherData()
    }

    override fun onStop() {
        super.onStop()
        if (!subscription.isDisposed) {
            subscription.dispose()
        }
    }

    private fun loadWeatherData() {
        subscription = weatherViewModelProvider
            .getWeatherFor("44418")
            .subscribeOn(Schedulers.io())
            .observeOn(
                AndroidSchedulers.mainThread(),
                true
            ) // the delay error is mandatory otherwise when there is NO NETWORK, the retrofit exception blocks also the db
            .doOnNext { maybeUpdateView(it) }
            .subscribe { maybeUpdateView(it) }
    }

    private fun maybeUpdateView(viewModel: WeatherViewModel?) {
        viewModel?.let {
            updateView(it)
        }

        if (viewModel == null) {
            Log.w("WeatherActivity", "View model is null")
        }
    }

    private fun updateView(weatherViewModel: WeatherViewModel) {
        Log.w("Weather activity", "viewmodel=$weatherViewModel")
        progress.visibility = if (weatherViewModel.loader.visible) View.VISIBLE else View.GONE

        if (weatherViewModel.errorMessage.visible) {
            Toast.makeText(this, weatherViewModel.errorMessage.messageForUser, Toast.LENGTH_SHORT)
                .show()
            Log.e("WeatherActivity", weatherViewModel.errorMessage.messageForLogger)
        }

        if (weatherViewModel.weather.visible) {
            details.text = weatherViewModel.weather.content
        }
    }
}
