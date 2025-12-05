package com.xtiantech.mzk.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.xtiantech.mzk.R
import com.xtiantech.mzk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 导航控制器
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // 底部导航配置（仅保留底部导航绑定，删除 ActionBar 相关代码）
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_news,
                R.id.nav_register,
                R.id.nav_mine
            )
        )

        // 🔥 关键修改：删除这行代码（不需要绑定 ActionBar）
        // setupActionBarWithNavController(navController, appBarConfiguration)

        // 底部导航栏绑定导航控制器（正常保留）
        binding.navView.setupWithNavController(navController)
    }

    // 🔥 修正返回逻辑（无需依赖 ActionBar）
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}