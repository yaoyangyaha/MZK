package com.xtiantech.mzk.ui.mine


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.xtiantech.mzk.databinding.FragmentMineBinding
import com.xtiantech.mzk.network.RetrofitClient

class MineFragment : Fragment() {
    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 更新用户信息
        updateUserInfo()

        // 退出登录
        binding.btnLogout.setOnClickListener {
            RetrofitClient.clearUserInfo(requireContext())
            Toast.makeText(context, "已退出登录", Toast.LENGTH_SHORT).show()
            updateUserInfo() // 刷新页面
        }

        // 未登录时点击用户名跳登录
        binding.tvUsername.setOnClickListener {
            if (!RetrofitClient.isLogin(requireContext())) {
                findNavController().navigate(com.mzk.eventapp.R.id.action_to_login)
            }
        }
    }

    // 更新用户信息（Kotlin 函数提取）
    private fun updateUserInfo() {
        if (RetrofitClient.isLogin(requireContext())) {
            val username = RetrofitClient.getUsername(requireContext())
            binding.tvUsername.text = "用户名：$username"
            binding.btnLogout.isEnabled = true
        } else {
            binding.tvUsername.text = "用户名：未登录"
            binding.btnLogout.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}