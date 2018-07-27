package com.wzx.searchviewdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import com.wzx.searchview.SearchView

class MainActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var searchContentView: EditText
    private lateinit var searchResultView: TextView

    var searchThread = object : Thread() {
        override fun run() {
            super.run()
            Thread.sleep(3000)
            //休眠5秒后结束动画
            searchView.endSearch()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchView = findViewById(R.id.searchView)
        searchContentView = findViewById(R.id.ed_search_content)
        searchResultView = findViewById(R.id.tv_result)

        searchView.addOnSearchListener(object : SearchView.OnSearchListener {
            override fun onSearchEnd() {
                Log.i("MainActivity", "onSearchEnd")
                searchResultView.text = "与${searchContentView.text}相关信息如下：\n1.xxx\n2.xxx"
            }

            override fun onSearchCancel() {
                Log.i("MainActivity", "onSearchCancel")
                searchResultView.text = "停止搜索：${searchContentView.text}"
            }

            override fun onSearchStart() {
                Log.i("MainActivity", "onSearchStart")
                searchThread.start()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        //释放动画
        searchView.release()
    }

    override fun onBackPressed() {
        if (searchView.isSearching()) {

            searchView.cancelSearch()
        } else {
            finish()
        }
    }
}
