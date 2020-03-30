package fbonfadelli.it.networktrial

import android.content.Context
import android.util.Log
import io.reactivex.Observable
import java.io.*

class WeatherAndroidCache(private val context: Context) : WeatherCache {

    override fun load(locationId: String): Observable<WeatherResponse> {
        return Observable.just(loadOrEmpty(locationId))
    }

    private fun loadOrEmpty(locationId: String): WeatherResponse {
        val file = File(context.cacheDir, "weather.$locationId")
        return if (file.exists()) {
            try {
                readObjectFrom(getBuffer(file))
                    .also { Log.d("WeatherAndroidCache", "Length = ${it.consolidated_weather.size}") }
            } catch (e: Exception) {
                WeatherResponse(emptyList())
                    .also {
                        if (e.message != null) {
                            Log.e("WeatherAndroidCache", "Error in reading from cache $e.message!!")
                        }
                    }
            }
        } else {
            WeatherResponse(emptyList())
        }

    }

    private fun getBuffer(file: File): ByteArray {
        val length = file.length()
        val buffer = ByteArray(length.toInt())
        val fileInputStream = FileInputStream(file)
        fileInputStream.read(buffer)
        fileInputStream.close()
        return buffer
    }

    private fun readObjectFrom(buffer: ByteArray): WeatherResponse {
        val byteArrayInputStream = ByteArrayInputStream(buffer)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        return objectInputStream.readObject() as WeatherResponse
    }

    override fun store(weatherResponse: WeatherResponse, locationId: String) {
        deleteOldCacheIfPresent(locationId)
        createNewCacheFileAndStore(locationId, weatherResponse)
    }

    private fun deleteOldCacheIfPresent(locationId: String) {
        val fileToDelete = File(context.cacheDir, "weather.$locationId")
        fileToDelete.delete()
    }

    private fun createNewCacheFileAndStore(
        locationId: String,
        weatherResponse: WeatherResponse
    ) {
        try {
            File.createTempFile("weather.$locationId", null, context.cacheDir)
            val cacheFile = File(context.cacheDir, "weather.$locationId")
            val os = FileOutputStream(cacheFile)
            os.write(getBytes(weatherResponse))
            os.close()
        } catch (e: Exception) {
            if (e.message != null) {
                Log.e("WeatherAndroidCache", "Error in storing in cache $e.message!!")
            }
        }
    }

    @Throws(java.io.IOException::class)
    private fun getBytes(obj: Any): ByteArray {

        val bos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(bos)
        oos.writeObject(obj)
        oos.flush()
        oos.close()
        bos.close()
        return bos.toByteArray()
    }
}
