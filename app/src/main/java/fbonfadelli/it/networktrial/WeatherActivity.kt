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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        weatherRepository = WeatherRepository(RxWeatherServiceFactory.make(), WeatherCache())
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

    private fun loadWeatherData() {
        subscription = weatherRepository
            .getWeatherFor("44418")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoader() }
            .doOnComplete { hideLoader() }
            .doOnError { hideLoader(); showError(it) }
            .onErrorReturn { WeatherResponse(emptyList()) }
            .subscribe { showResult(it.consolidated_weather) }
    }

    override fun onStop() {
        super.onStop()
        if (!subscription.isDisposed) {
            subscription.dispose()
        }
    }

    private fun showLoader() {
        progress.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        progress.visibility = View.GONE
    }

    private fun showError(error: Throwable) {
        error.message?.let { Log.e("WeatherActivity", it) }
        Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show()
    }

    private fun showResult(list: List<Weather>) {
        list.firstOrNull()?.let { details.text = it.toString() }
    }
}
