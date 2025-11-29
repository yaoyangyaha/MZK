package com.xtiantech.mzk.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.xtiantech.mzk.adapter.RegistrationsAdapter
import com.xtiantech.mzk.databinding.FragmentMyRegistrationsBinding
import com.xtiantech.mzk.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyRegistrationsFragment : Fragment() {
    private var _binding: FragmentMyRegistrationsBinding? = null
    private val binding get() = _binding!!
    // 适配器（Kotlin lateinit）
    private lateinit var adapter: RegistrationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyRegistrationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 登录校验
        if (!RetrofitClient.isLogin(requireContext())) {
            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        // 初始化 RecyclerView
        adapter = RegistrationsAdapter()
        binding.rvRegistrations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@MyRegistrationsFragment.adapter
        }

        // 获取我的报名
        getMyRegistrations()
    }

    // 获取报名记录
    private fun getMyRegistrations() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = RetrofitClient.getInstance(requireContext()).create(ApiService::class.java)
                val response = api.getMyRegistrations()

                withContext(Dispatchers.Main) {
                    adapter.submitList(response.registrations)
                    binding.tvTotal.text = "共 ${response.total} 条报名记录"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "获取报名失败：${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}