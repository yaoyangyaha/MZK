package com.xtiantech.mzk.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.xtiantech.mzk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // ViewBinding（Kotlin lateinit）
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化 Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 导航控制器
        val navController = findNavController(com.mzk.eventapp.R.id.nav_host_fragment_activity_main)
        // 底部导航配置
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                com.mzk.eventapp.R.id.nav_news,
                com.mzk.eventapp.R.id.nav_register,
                com.mzk.eventapp.R.id.nav_mine
            )
        )
        // 绑定 ActionBar 和底部导航
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    // 导航返回按钮
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(com.mzk.eventapp.R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}