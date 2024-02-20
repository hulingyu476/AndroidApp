package com.example.foldertest

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class MainActivity : AppCompatActivity() {
    var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            // Access to all files
            val uri: Uri = Uri.parse("package:com.example.foldertest")
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
            startActivity(intent)
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ),
            123
        )

        val textView = findViewById<TextView>(R.id.textView)

        findViewById<Button>(R.id.button).setOnClickListener {

            //9666
            //sandisk
            val directory = File("/storage/7EE3-955D/2000/")
            //aiguozhe
            //val directory = File("/storage/DEE4-1A57/2000/")

            job = MainScope().launch(Dispatchers.IO) {
                val start = Date()
                var str = ""

                str += "directory.exists() is ${directory.exists()}, $directory"
                Log.d("walkTopDown", "${str.split("\n").last()}")
                launch(Dispatchers.Main) { textView.text = str }

                str += "\nstart walkTopDown ${Date().time - start.time}ms"
                Log.d("walkTopDown", "${str.split("\n").last()}")
                launch(Dispatchers.Main) { textView.text = str }
                val fileWalk = directory.walkTopDown()

                str += "\nstart filter isFile ${Date().time - start.time}ms"
                Log.d("walkTopDown", "${str.split("\n").last()}")
                launch(Dispatchers.Main) { textView.text = str }
                val files = fileWalk.filter { it.isFile }

                str += "\nstart forEach ${Date().time - start.time}ms"
                Log.d("walkTopDown", "${str.split("\n").last()}")
                launch(Dispatchers.Main) { textView.text = str }
                var sum = 0L
                var isFirst = true
                files.forEach {
                    if (isFirst) {
                        str += "\nstart counting ${Date().time - start.time}ms"
                        Log.d("walkTopDown", "${str.split("\n").last()}")
                        launch(Dispatchers.Main) { textView.text = str }
                        isFirst = false
                    }
                    sum += it.length()
                }

                str += "\nread END, list size is ${files.toList().size}, total size is $sum, ${Date().time - start.time}ms"
                Log.d("walkTopDown", "${str.split("\n").last()}")
                launch(Dispatchers.Main) { textView.text = str }
            }
        }
    }
}