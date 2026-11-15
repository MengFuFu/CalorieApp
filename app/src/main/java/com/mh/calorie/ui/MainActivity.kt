package com.mh.calorie.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mh.calorie.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * M0 临时占位页。
 *
 * 存在目的只有一个：证明「构建 → 安装 → 启动 → 不崩」这条链路是通的。
 * M2 会把它改造成承载 5 个 Tab 的单 Activity 容器。
 */
@AndroidEntryPoint // 让 Hilt 能向本 Activity 注入依赖，现在没依赖可注入，但有它才能验证 DI 通了
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}