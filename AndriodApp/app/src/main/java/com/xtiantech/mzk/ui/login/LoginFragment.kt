package com.xtiantech.mzk.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.xtiantech.mzk.R // 新增：导入R类（用于导航Action ID）
import com.xtiantech.mzk.api.ApiService
import com.xtiantech.mzk.data.LoginRequest
import com.xtiantech.mzk.databinding.FragmentLoginBinding
import com.xtiantech.mzk.network.RetrofitClient
//import com.xtiantech.mzk.utils.NetworkUtils // 新增：可选，网络状态判断（如果没这个类可暂时注释）
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job // 新增：协程任务管理，避免内存泄漏
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var loginJob: Job? = null // 新增：管理登录请求，销毁时取消

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 登录按钮点击事件（原有逻辑不变）
        binding.btnLogin.setOnClickListener {
            // 获取输入内容（处理空值，兜底空字符串）
            val username = binding.etUsername.text?.toString()?.trim() ?: ""
            val password = binding.etPassword.text?.toString()?.trim() ?: ""

            // 输入校验
            when {
                username.isEmpty() -> Toast.makeText(context, "用户名不能为空", Toast.LENGTH_SHORT).show()
                password.isEmpty() -> Toast.makeText(context, "密码不能为空", Toast.LENGTH_SHORT).show()
                else -> login(username, password) // 执行登录
            }
        }

        // 新增：跳注册按钮点击事件（核心新增逻辑）
        binding.tvGoRegister.setOnClickListener {
            // 跳转到注册Fragment（对应nav_graph中配置的Action ID）
            findNavController().navigate(R.id.action_loginFragment_to_registerUserFragment)
        }
    }

    // 登录请求（协程+IO线程）- 仅新增网络判断和协程管理，原有逻辑不变
    private fun login(username: String, password: String) {
//        // 新增：网络状态判断（可选，提升体验，若无NetworkUtils类可注释这部分）
//        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
//            Toast.makeText(context, "网络不可用，请检查网络连接", Toast.LENGTH_SHORT).show()
//            return
//        }

        // 新增：取消之前未完成的登录请求，避免重复请求
        loginJob?.cancel()
        loginJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                // 获取ApiService实例（原有逻辑）
                val api = RetrofitClient.getInstance(requireContext()).create(ApiService::class.java)
                // 调用登录接口（原有逻辑）
                val response = api.login(LoginRequest(username, password))
                // 保存用户信息（原有逻辑）
                RetrofitClient.saveUserInfo(requireContext(), response.access_token, response.user.username)

                // 切回主线程更新UI（原有逻辑）
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack() // 返回上一页
                }
            } catch (e: Exception) {
                // 异常处理（切回主线程）（原有逻辑）
                withContext(Dispatchers.Main) {
                    val errorMsg = when (e.message) {
                        "401" -> "用户名或密码错误"
                        else -> "登录失败：${e.message ?: "未知错误"}"
                    }
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loginJob?.cancel() // 新增：销毁时取消登录请求，避免内存泄漏
        _binding = null // 原有逻辑
    }
}