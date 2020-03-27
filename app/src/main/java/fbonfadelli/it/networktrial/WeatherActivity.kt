package fbonfadelli.it.networktrial

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class WeatherActivity : AppCompatActivity() {

    private lateinit var details: TextView
    private lateinit var subscription: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        details = findViewById(R.id.detail)
        subscription = RxWeatherService()
            .getWeatherFor("44418")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoader() }
            .doOnComplete { hideLoader() }
            .doOnError { hideLoader(); showError(it) }
            .onErrorReturn { WeatherResponse(emptyList()) }
            .subscribe { showResult(it.consolidated_weather) }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!subscription.isDisposed) {
            subscription.dispose()
        }
    }

    private fun showLoader() {
        Log.d("WeatherActivity", "showing loader")
    }

    private fun hideLoader() {
        Log.d("WeatherActivity", "hiding loader")
    }

    private fun showError(error: Throwable) {
        Log.e("WeatherActivity", error.message)
    }

    private fun showResult(list: List<Weather>) {
        if (list.isEmpty()) {
            details.text = "error"
        } else {
            details.text = list.first().toString()
        }
    }
}
