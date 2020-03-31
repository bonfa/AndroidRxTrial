package fbonfadelli.it.networktrial

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class WeatherActivity : AppCompatActivity() {

    private lateinit var details: ConstraintLayout
    private lateinit var stateName: TextView
    private lateinit var windSpeedContent: TextView
    private lateinit var windDirectionContent: TextView
    private lateinit var windDirectionCompassContent: TextView
    private lateinit var noContentMessage: TextView
    private lateinit var reloadButton: MaterialButton
    private lateinit var resetViewButton: MaterialButton
    private lateinit var progress: ProgressBar
    private lateinit var subscription: Disposable
    private lateinit var weatherRepository: WeatherRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        weatherRepository =
            WeatherRepository(RxWeatherServiceFactory.make(), WeatherAndroidCache(this))
        details = findViewById(R.id.detail)
        reloadButton = findViewById(R.id.reloadButton)
        resetViewButton = findViewById(R.id.resetViewButton)
        progress = findViewById(R.id.progress)
        stateName = findViewById(R.id.state_name)
        windSpeedContent = findViewById(R.id.wind_speed_content)
        windDirectionContent = findViewById(R.id.wind_direction_content)
        windDirectionCompassContent = findViewById(R.id.wind_direction_comp_content)
        noContentMessage = findViewById(R.id.no_content_message)
        reloadButton.setOnClickListener { loadWeatherData() }
        resetViewButton.setOnClickListener { resetViews() }
    }

    private fun resetViews() {
        stateName.text = "-"
        windSpeedContent.text = ""
        windDirectionContent.text = ""
        windDirectionCompassContent.text = ""
        details.visibility = View.GONE
        noContentMessage.visibility = View.GONE
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
        subscription = WeatherViewModel(weatherRepository)
            .getWeatherFor("44418")
            .subscribeOn(Schedulers.io())
            .observeOn(
                AndroidSchedulers.mainThread(),
                true
            ) // the delay error is mandatory otherwise when there is NO NETWORK, the retrofit exception blocks also the db
            .doOnSubscribe { showLoader() }
            .doOnComplete { hideLoader() }
            .doOnError { hideLoader(); /*showError(it)*/ }
            .doOnNext { showResult(it) }
            .subscribe { showResult(it) }
    }

    private fun showLoader() {
        progress.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        progress.visibility = View.GONE
    }

    private fun showResult(viewModel: ViewWeather) {
        if (viewModel.emptyMessage.visible) {
            noContentMessage.visibility = View.VISIBLE
            details.visibility = View.GONE
            noContentMessage.text = viewModel.emptyMessage.content.message
        }

        if (viewModel.errorMessage.visible) {
            Toast.makeText(this, viewModel.errorMessage.content.message, Toast.LENGTH_SHORT).show()
        }

        if (viewModel.weatherDetail.visible) {
            noContentMessage.visibility = View.GONE
            details.visibility = View.VISIBLE
            val viewWeatherDetailContent = viewModel.weatherDetail.content!!
            stateName.text = viewWeatherDetailContent.weatherStateName
            windSpeedContent.text = viewWeatherDetailContent.windSpeed
            windDirectionContent.text = viewWeatherDetailContent.windDirection
            windDirectionCompassContent.text = viewWeatherDetailContent.windDirectionCompass
        }
    }
}
