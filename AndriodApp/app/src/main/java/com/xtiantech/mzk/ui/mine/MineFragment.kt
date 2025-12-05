package com.xtiantech.mzk.ui.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.xtiantech.mzk.R // 正确导入项目的R类
import com.xtiantech.mzk.databinding.FragmentMineBinding
import com.xtiantech.mzk.network.RetrofitClient

class MineFragment : Fragment() {
    // ViewBinding 委托+可空，避免内存泄漏
    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 初始化ViewBinding
        _binding = FragmentMineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化页面：更新用户登录状态
        updateUserInfo()

        // 退出登录按钮点击事件
        binding.btnLogout.setOnClickListener {
            RetrofitClient.clearUserInfo(requireContext()) // 清除本地用户信息
            Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show()
            updateUserInfo() // 刷新页面状态
        }

        // 未登录时，点击用户名跳登录页（使用修正后的action ID）
        binding.tvUsername.setOnClickListener {
            if (!RetrofitClient.isLogin(requireContext())) {
                findNavController().navigate(R.id.action_nav_mine_to_loginFragment)
            }
        }
    }

    /**
     * 更新用户信息显示（登录/未登录状态切换）
     */
    private fun updateUserInfo() {
        if (RetrofitClient.isLogin(requireContext())) {
            // 已登录：显示用户名，启用退出登录按钮
            val username = RetrofitClient.getUsername(requireContext())
            binding.tvUsername.text = "用户名：$username"
            binding.btnLogout.isEnabled = true
            binding.btnLogout.alpha = 1.0f // 按钮正常透明度
        } else {
            // 未登录：显示"未登录"，禁用退出登录按钮
            binding.tvUsername.text = "用户名：未登录"
            binding.btnLogout.isEnabled = false
            binding.btnLogout.alpha = 0.5f // 按钮半透明
        }
    }

    /**
     * 生命周期：销毁View时释放Binding，避免内存泄漏
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}