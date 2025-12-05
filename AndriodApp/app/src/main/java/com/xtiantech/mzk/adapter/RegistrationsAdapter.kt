package com.xtiantech.mzk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xtiantech.mzk.data.Registration
import com.xtiantech.mzk.databinding.ItemRegistrationBinding

// 报名记录列表适配器（使用ListAdapter+DiffUtil优化刷新）
class RegistrationsAdapter : ListAdapter<Registration, RegistrationsAdapter.ViewHolder>(DiffCallback()) {

    // DiffUtil：对比新旧数据，只刷新变化的项
    class DiffCallback : DiffUtil.ItemCallback<Registration>() {
        override fun areItemsTheSame(oldItem: Registration, newItem: Registration): Boolean {
            return oldItem.id == newItem.id // 按ID判断是否是同一个项
        }

        override fun areContentsTheSame(oldItem: Registration, newItem: Registration): Boolean {
            return oldItem == newItem // 内容是否完全一致
        }
    }

    // ViewHolder：绑定列表项布局
    class ViewHolder(private val binding: ItemRegistrationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(registration: Registration) {
            // 绑定数据到布局控件
            binding.tvEventName.text = "活动名称：${registration.event_name}"
            binding.tvGameUsername.text = "游戏用户名：${registration.game_username}"
            binding.tvTeamName.text = "车队名称：${registration.team_name}"
            binding.tvGroupType.text = "参与组别：${registration.group_type}"
            binding.tvSupplement.text = "补充说明：${if (registration.supplement.isEmpty()) "无" else registration.supplement}"
            binding.tvTime.text = "报名时间：${registration.registered_at}"
        }
    }

    // 创建ViewHolder（加载列表项布局）
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRegistrationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // 绑定数据到ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}