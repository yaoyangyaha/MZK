package com.xtiantech.mzk.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.xtiantech.mzk.data.LoginRequest
import com.xtiantech.mzk.databinding.FragmentLoginBinding
import com.xtiantech.mzk.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {
    // ViewBinding（Kotlin 可空 + 委托）
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

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

        // 登录按钮点击
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text?.toString()?.trim() ?: ""
            val password = binding.etPassword.text?.toString()?.trim() ?: ""

            // 输入校验
            when {
                username.isEmpty() -> Toast.makeText(context, "用户名不能为空", Toast.LENGTH_SHORT).show()
                password.isEmpty() -> Toast.makeText(context, "密码不能为空", Toast.LENGTH_SHORT).show()
                else -> login(username, password)
            }
        }
    }

    // 登录请求（协程 + IO 线程）
    private fun login(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = RetrofitClient.getInstance(requireContext()).create(ApiService::class.java)
                val response = api.login(LoginRequest(username, password))

                // 保存 Token 和用户名
                RetrofitClient.saveUserInfo(requireContext(), response.access_token, response.user.username)

                // 切回主线程更新 UI
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack() // 返回上一页
                }
            } catch (e: Exception) {
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

    // 生命周期：释放 Binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}