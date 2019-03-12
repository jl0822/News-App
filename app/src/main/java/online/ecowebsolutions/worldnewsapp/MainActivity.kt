package online.ecowebsolutions.worldnewsapp

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    var listData = ArrayList<Data>()
    var pageNumber = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    inner class MyAsyncTask : AsyncTask<String, Void, ArrayList<Data>>() {
        override fun onPostExecute(result: ArrayList<Data>?) {

            if (result != null){
                updateUi(result)
            }
        }

        override fun doInBackground(vararg params: String?): ArrayList<Data> {
            val url = createUrl(params[0])

            var jsonResponse: String? = ""
            try {

                jsonResponse = makeHttpRequest(url)

            } catch (e: IOException) {
                Log.e("MainActivity", "Problem making the HTTP request $e")
            }

            var data = extractFeaturesFromResponse(jsonResponse)

            return data
        }
    }

    fun updateUi(list : ArrayList<Data>){
        list_view?.adapter = NewsAdapter(this, list)
    }

    fun extractFeaturesFromResponse(guardianJson : String?):ArrayList<Data>{

        try {
            val baseJsonResponse = JSONObject(guardianJson)
            val response = baseJsonResponse.getJSONObject("response")

            val newsArray = response.getJSONArray("results")

            for (i in 0..9){

                val item = newsArray.getJSONObject(i)
                val sectionName = item.getString("sectionName")
                val webTitle = item.getString("webTitle")
                val webUrl = item.getString("webUrl")

                val data = Data(sectionName, webTitle, webUrl)

                listData.add(data)
            }

        }catch (e : JSONException){
            Log.e("MainActivity", "Problem parsing the news JSON results $e")
        }

        return listData
    }

    fun makeHttpRequest(url: URL?):String {
        var jsonResponse: String = ""
        var urlConnection: HttpURLConnection? = null
        var inputStream: InputStream? = null

        try {
            urlConnection = url?.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.setRequestProperty("Accept","application/json")
            urlConnection.setRequestProperty("api-key","b1313706-02b3-443a-8a3f-06d64abe1881")
            urlConnection.readTimeout = 10000
            urlConnection.connectTimeout = 15000
            urlConnection.connect()

            if (urlConnection.responseCode == 200){
                inputStream = urlConnection.inputStream
                jsonResponse = readFromStream(inputStream)
            }else{
                Log.i("MainActivity", "the code is : ${urlConnection.responseCode}")
            }

            urlConnection.disconnect()
            inputStream?.close()

        }catch (e: IOException) {
            Log.e("MainActivity", "Error response code: ${urlConnection?.responseCode}");
        }

        return jsonResponse
    }

    fun readFromStream(inputStream: InputStream?):String{
        val output = StringBuilder()
        val inputStreamReader = InputStreamReader(inputStream, Charset.forName("UTF-8"))
        val reader = BufferedReader(inputStreamReader)
        var line : String? = reader.readLine()

        while (line != null){
            output.append(line)
            line = reader.readLine()
        }

        return output.toString()
    }


    fun createUrl(stringUrl:String?): URL?{
        val url : URL?
        try {
            url = URL(stringUrl)
        }catch (e : MalformedURLException){
            Log.e("MainActivity", "Error with creating URL $e")
            return null
        }

        return url
    }

    fun searchWord(view: View){
        pageNumber = 1
        val stringUrl = "https://content.guardianapis.com/search?q=${edit_text?.text}&tag=politics/politics&page=$pageNumber"

        listData.clear()

        var myAsyncTask = MyAsyncTask()
        myAsyncTask.execute(stringUrl)
    }
    fun loadMore(view: View){
        pageNumber += 1

        val stringUrl = "https://content.guardianapis.com/search?q=${edit_text?.text}&tag=politics/politics&page=$pageNumber"

        val myAsynch = MyAsyncTask()
        myAsynch.execute(stringUrl)

    }
}
