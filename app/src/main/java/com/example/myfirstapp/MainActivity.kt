package com.example.myfirstapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.net.CronetProviderInstaller
import okhttp3.*
import org.chromium.net.CronetEngine
import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import com.android.volley.Request as VRequest
import com.android.volley.Response as VResponse


const val EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE"

class MainActivity : AppCompatActivity() {

    fun cronetRequest(start: Long) {
        class MyUrlRequestCallback : UrlRequest.Callback() {
            val TAG = "MyUrlRequestCallback"

            override fun onRedirectReceived(
                request: UrlRequest?,
                info: UrlResponseInfo?,
                newLocationUrl: String?
            ) {
                Log.i(TAG, "onRedirectReceived method called.")
                // You should call the request.followRedirect() method to continue
                // processing the request.
                request?.followRedirect()
            }

            override fun onResponseStarted(request: UrlRequest?, info: UrlResponseInfo?) {
                Log.i(TAG, "onResponseStarted method called.")
                // You should call the request.read() method before the request can be
                // further processed. The following instruction provides a ByteBuffer object
                // with a capacity of 102400 bytes to the read() method.
                request?.read(ByteBuffer.allocateDirect(102400))
            }

            override fun onReadCompleted(
                request: UrlRequest?,
                info: UrlResponseInfo?,
                byteBuffer: ByteBuffer?
            ) {
                Log.i(TAG, "onReadCompleted method called.")
                // You should keep reading the request until there's no more data.
                request?.read(ByteBuffer.allocateDirect(102400))
            }

            override fun onSucceeded(request: UrlRequest?, info: UrlResponseInfo?) {
                val cronet = System.currentTimeMillis()
                Log.i("!!!TIME", "cronet: " + (cronet - start))
                Log.i(TAG, "onSucceeded method called.")
            }

            override fun onFailed(
                request: UrlRequest?,
                info: UrlResponseInfo?,
                error: CronetException?
            ) {
                // The request has failed. If possible, handle the error.
                Log.e(TAG, "The request failed.", error)
            }
        }

        CronetProviderInstaller.installProvider(this.applicationContext)

        val myBuilder = CronetEngine.Builder(this.applicationContext)
        val cronetEngine: CronetEngine = myBuilder.build()

        val executor = Executors.newSingleThreadExecutor()

        val requestBuilder = cronetEngine.newUrlRequestBuilder(
            "http://google.com",
            MyUrlRequestCallback(),
            executor
        )

        val request: UrlRequest = requestBuilder.build()

        request.start()
    }

    fun volleyRequest(start: Long) {
        val textView = findViewById<TextView>(R.id.textView)

        val queue = Volley.newRequestQueue(this)
        val url =
            "http://google.com"

        val jsonObjectRequest = JsonObjectRequest(VRequest.Method.GET, url, null,
            VResponse.Listener { response ->
                textView.text = "Response: %s".format(response.toString())
                val volley = System.currentTimeMillis()
                Log.i("!!!TIME", "volley: " + (volley - start))
            },
            VResponse.ErrorListener { error ->
                textView.text = "err: ${error.localizedMessage}"
            }
        )
        queue.add(jsonObjectRequest)
    }

    fun okhttpRequest(start: Long) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("http://google.com")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val okhttp = System.currentTimeMillis()
                    Log.i("!!!TIME", "okhttp: " + (okhttp - start))

                    println(response.body!!.string())
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        volleyRequest(System.currentTimeMillis())
        cronetRequest(System.currentTimeMillis())
        okhttpRequest(System.currentTimeMillis())
    }

    /** Called when the user taps the Send button */
    fun sendMessage(view: View) {
        val editText = findViewById<EditText>(R.id.editText)
        val message = editText.text.toString()
        val intent = Intent(this, DisplayMessageActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
    }
}

