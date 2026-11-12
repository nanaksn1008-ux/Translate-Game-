package com.example.penerjemahgame

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast

class MainActivity : Activity() {
    private var chooser: ValueCallback<Array<Uri>>? = null
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        web.addJavascriptInterface(Bridge(), "Android")
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                chooser?.onReceiveValue(null)
                chooser = callback
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "*/*"
                return try {
                    startActivityForResult(Intent.createChooser(i, "Pilih file dialog"), 1)
                    true
                } catch (e: Exception) {
                    chooser = null
                    false
                }
            }
        }
        web.loadUrl("file:///android_asset/index.html")
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            val uri = data?.data
            val result: Array<Uri>? = if (resultCode == RESULT_OK && uri != null) arrayOf(uri) else null
            chooser?.onReceiveValue(result)
            chooser = null
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    inner class Bridge {
        @JavascriptInterface
        fun saveFile(name: String, data: String): Boolean {
            return try {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, name)
                    put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)!!
                contentResolver.openOutputStream(uri)!!.use { it.write(data.toByteArray(Charsets.UTF_8)) }
                runOnUiThread { Toast.makeText(this@MainActivity, "Tersimpan di Unduhan", Toast.LENGTH_SHORT).show() }
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
