package com.xtiantech.mzk.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.xtiantech.mzk.R
import com.xtiantech.mzk.api.ApiService
import com.xtiantech.mzk.api.RegisterRequest
import com.xtiantech.mzk.databinding.FragmentRegisterUserBinding
import com.xtiantech.mzk.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.regex.Pattern

class RegisterUserFragment : Fragment() {
    private var _binding: FragmentRegisterUserBinding? = null
    private val binding get() = _binding!!
    private var registerJob: Job? = null
    private val phonePattern = Pattern.compile("^1[3-9]\\d{9}$")
    private val emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    // 修复：用自定义Toast管理器替代反射，兼容API34+
    private var currentToast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            if (checkFormValid()) {
                submitRegister()
            }
        }

        binding.tvGoLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerUserFragment_to_loginFragment)
        }
    }

    private fun checkFormValid(): Boolean {
        val username = binding.etUsername.text?.toString()?.trim() ?: ""
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val phone = binding.etPhone.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString() ?: ""
        val confirmPwd = binding.etConfirmPassword.text?.toString() ?: ""



        when {
            username.isEmpty() -> { showToast("用户名不能为空"); return false }
            email.isEmpty() -> { showToast("邮箱不能为空"); return false }
            phone.isEmpty() -> { showToast("手机号不能为空"); return false }
            password.isEmpty() -> { showToast("密码不能为空"); return false }
            confirmPwd.isEmpty() -> { showToast("请确认密码"); return false }
            username.length < 3 || username.length > 20 -> { showToast("用户名长度需3-20位"); return false }
            !emailPattern.matcher(email).matches() -> { showToast("邮箱格式不正确"); return false }
            !phonePattern.matcher(phone).matches() -> { showToast("手机号格式不正确"); return false }
            password.length < 6 -> { showToast("密码至少6位"); return false }
            password != confirmPwd -> { showToast("两次密码不一致"); return false }
        }

        return true
    }

    // 🔥 核心修复：协程+超时逻辑（废弃+运算符，修正withTimeout参数）
    private fun submitRegister() {
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "注册中..."

        val request = RegisterRequest(
            username = binding.etUsername.text!!.trim().toString(),
            email = binding.etEmail.text!!.trim().toString(),
            phone = binding.etPhone.text!!.trim().toString(),
            password = binding.etPassword.text!!.toString()
        )

        registerJob?.cancel()
        var retryCount = 0
        val maxRetry = 2

        registerJob = CoroutineScope(Dispatchers.Main).launch { // 改用Main协程域，内部切换IO
            while (retryCount <= maxRetry) {
                try {
                    // 修复：正确的超时+Dispatcher写法（先切换IO线程，再设置超时）
                    val response = withContext(Dispatchers.IO) {
                        withTimeout(5000L) { // 5000L 明确为Long类型，避免参数类型错误
                            val api = RetrofitClient.getInstance(requireContext()).create(ApiService::class.java)
                            api.register(request)
                        }
                    }

                    // 成功处理
                    showToast(response.message)
                    findNavController().navigate(R.id.action_registerUserFragment_to_loginFragment)
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "注册账号"
                    break

                } catch (e: Exception) {
                    retryCount++
                    if (retryCount > maxRetry) {
                        val errorMsg = when {
                            // 识别HTTP 409冲突
                            e is retrofit2.HttpException && e.code() == 409 -> {
                                val errorBody = e.response()?.errorBody()?.string() ?: ""
                                when {
                                    errorBody.contains("Username already exists") -> "用户名已存在"
                                    errorBody.contains("Email already exists") -> "邮箱已存在"
                                    errorBody.contains("Phone already exists") -> "手机号已存在"
                                    else -> "注册失败：该账号信息已被使用"
                                }
                            }
                            e is kotlinx.coroutines.TimeoutCancellationException -> "请求超时，请重试"
                            e.message?.contains("unexpected end of stream") == true -> "网络不稳定，请重试"
                            else -> "注册失败：${e.message ?: "未知错误"}"
                        }

                        showToast(errorMsg)
                        android.util.Log.e("RegisterError", "错误详情：$errorMsg", e)
                        binding.btnRegister.isEnabled = true
                        binding.btnRegister.text = "注册账号"
                    } else {
                        // 重试间隔500ms
                        delay(500)
                    }
                }
            }
        }
    }

    // 🔥 修复：替代反射的Toast管理，兼容API34+
    private fun showToast(message: String) {
        // 取消上一个未显示的Toast，避免队列超限
        currentToast?.cancel()
        currentToast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        registerJob?.cancel()
        currentToast?.cancel() // 销毁时取消Toast
        _binding = null
    }
}