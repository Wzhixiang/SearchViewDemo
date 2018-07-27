package com.wzx.searchviewdemo

import android.app.Application
import com.squareup.leakcanary.LeakCanary

/**
 * 描述：
 *
 * 创建人： Administrator
 * 创建时间： 2018/7/27
 * 更新时间：
 * 更新内容：
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        //检测内存
        LeakCanary.install(this);
    }
}