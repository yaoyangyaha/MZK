package com.xtiantech.mzk.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.xtiantech.mzk.data.RegisterEventRequest
import com.xtiantech.mzk.databinding.FragmentRegisterBinding
import com.xtiantech.mzk.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    // 组别选项（Kotlin 数组）
    private val groupTypes = arrayOf("专业组", "业余组", "新手组")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 登录校验
        if (!RetrofitClient.isLogin(requireContext())) {
            findNavController().navigate(com.mzk.eventapp.R.id.action_to_login)
            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
            return
        }

        // 初始化组别下拉框
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, groupTypes)
        binding.etGroupType.setAdapter(adapter)

        // 提交报名
        binding.btnSubmit.setOnClickListener {
            val eventName = binding.etEventName.text?.toString()?.trim() ?: ""
            val gameUsername = binding.etGameUsername.text?.toString()?.trim() ?: ""
            val teamName = binding.etTeamName.text?.toString()?.trim() ?: ""
            val groupType = binding.etGroupType.text?.toString()?.trim() ?: ""
            val supplement = binding.etSupplement.text?.toString()?.trim() ?: ""

            // 校验必填字段
            when {
                eventName.isEmpty() -> Toast.makeText(context, "活动名称不能为空", Toast.LENGTH_SHORT).show()
                gameUsername.isEmpty() -> Toast.makeText(context, "游戏用户名不能为空", Toast.LENGTH_SHORT).show()
                teamName.isEmpty() -> Toast.makeText(context, "车队名称不能为空", Toast.LENGTH_SHORT).show()
                groupType.isEmpty() -> Toast.makeText(context, "参与组别不能为空", Toast.LENGTH_SHORT).show()
                else -> submitRegistration(eventName, gameUsername, teamName, groupType, supplement)
            }
        }

        // 查看我的报名
        binding.btnMyRegistrations.setOnClickListener {
            findNavController().navigate(com.mzk.eventapp.R.id.action_to_my_registrations)
        }
    }

    // 提交报名请求
    private fun submitRegistration(
        eventName: String,
        gameUsername: String,
        teamName: String,
        groupType: String,
        supplement: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = RetrofitClient.getInstance(requireContext()).create(ApiService::class.java)
                val request = RegisterEventRequest(eventName, gameUsername, teamName, groupType, supplement)
                val response = api.registerEvent(request)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                    // 清空表单（Kotlin 链式调用）
                    binding.apply {
                        etEventName.text?.clear()
                        etGameUsername.text?.clear()
                        etTeamName.text?.clear()
                        etGroupType.text?.clear()
                        etSupplement.text?.clear()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "报名失败：${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}