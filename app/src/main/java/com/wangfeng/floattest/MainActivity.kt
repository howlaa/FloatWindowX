package com.wangfeng.floattest

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.wangfeng.floatwindow.FloatWindow

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        floatTest()
    }



    override fun onStop() {
        super.onStop()
        Log.d("wangfeng","Main onstop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("wangfeng","Main onDestroy")
    }

    private fun floatTest(){
        floatBasic()
    }

    private fun floatBasic(){
        val iv = ImageView(this)
        iv.setImageResource(R.mipmap.ic_launcher)
        //基本使用
        FloatWindow
            .with(applicationContext)
            .setView(iv)
            .setDesktopShow(true)
            .build()
        FloatWindow.get().show()
    }
}