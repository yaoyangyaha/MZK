package com.xtiantech.mzk.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.xtiantech.mzk.R // 正确导入项目的R类
import com.xtiantech.mzk.api.ApiService
import com.xtiantech.mzk.data.RegisterEventRequest
import com.xtiantech.mzk.databinding.FragmentRegisterBinding
import com.xtiantech.mzk.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterFragment : Fragment() {
    // ViewBinding 委托+可空，避免内存泄漏
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    // 参与组别选项（专业组/业余组/新手组）
    private val groupTypes = arrayOf("专业组", "业余组", "新手组")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 初始化ViewBinding
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 登录校验：未登录直接跳登录页（使用修正后的action ID）
        if (!RetrofitClient.isLogin(requireContext())) {
            findNavController().navigate(R.id.action_nav_register_to_loginFragment)
            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
            return
        }

        // 初始化参与组别下拉框（AutoCompleteTextView）
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            groupTypes
        )
        binding.etGroupType.setAdapter(adapter)

        // 提交报名按钮点击事件
        binding.btnSubmit.setOnClickListener {
            submitRegistration()
        }

        // 查看我的报名按钮（跳我的报名页，使用修正后的action ID）
        binding.btnMyRegistrations.setOnClickListener {
            findNavController().navigate(R.id.action_nav_register_to_myRegistrationsFragment)
        }
    }

    /**
     * 提交报名请求（协程+IO线程异步请求）
     */
    private fun submitRegistration() {
        // 获取输入框内容（trim去除空格，兜底空字符串）
        val eventName = binding.etEventName.text?.toString()?.trim() ?: ""
        val gameUsername = binding.etGameUsername.text?.toString()?.trim() ?: ""
        val teamName = binding.etTeamName.text?.toString()?.trim() ?: ""
        val groupType = binding.etGroupType.text?.toString()?.trim() ?: ""
        val supplement = binding.etSupplement.text?.toString()?.trim() ?: ""

        // 输入校验（避免空数据提交）
        when {
            eventName.isEmpty() -> Toast.makeText(context, "活动名称不能为空", Toast.LENGTH_SHORT).show()
            gameUsername.isEmpty() -> Toast.makeText(context, "游戏用户名不能为空", Toast.LENGTH_SHORT).show()
            teamName.isEmpty() -> Toast.makeText(context, "车队名称不能为空", Toast.LENGTH_SHORT).show()
            groupType.isEmpty() -> Toast.makeText(context, "参与组别不能为空", Toast.LENGTH_SHORT).show()
            else -> {
                // 校验通过，发起网络请求
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // 获取ApiService实例
                        val api = RetrofitClient.getInstance(requireContext()).create(ApiService::class.java)
                        // 构建请求体
                        val request = RegisterEventRequest(
                            event_name = eventName,
                            game_username = gameUsername,
                            team_name = teamName,
                            group_type = groupType,
                            supplement = supplement
                        )
                        // 调用报名接口
                        val response = api.registerEvent(request)

                        // 切回主线程更新UI
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                            // 清空表单
                            clearForm()
                        }
                    } catch (e: Exception) {
                        // 异常处理（网络错误、后端报错等）
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "报名失败：${e.message ?: "未知错误"}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * 清空报名表单
     */
    private fun clearForm() {
        binding.etEventName.text?.clear()
        binding.etGameUsername.text?.clear()
        binding.etTeamName.text?.clear()
        binding.etGroupType.text?.clear()
        binding.etSupplement.text?.clear()
    }

    /**
     * 生命周期：销毁View时释放Binding，避免内存泄漏
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}