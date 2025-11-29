package com.xtiantech.mzk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xtiantech.mzk.data.Registration
import com.xtiantech.mzk.databinding.ItemRegistrationBinding

class RegistrationsAdapter : ListAdapter<Registration, RegistrationsAdapter.ViewHolder>(DiffCallback()) {

    // DiffUtil 刷新优化
    class DiffCallback : DiffUtil.ItemCallback<Registration>() {
        override fun areItemsTheSame(oldItem: Registration, newItem: Registration): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Registration, newItem: Registration): Boolean {
            return oldItem == newItem
        }
    }

    // ViewHolder（Kotlin 委托绑定）
    class ViewHolder(private val binding: ItemRegistrationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Registration) {
            binding.tvEventName.text = "活动名称：${item.event_name}"
            binding.tvGameUsername.text = "游戏用户名：${item.game_username}"
            binding.tvTeamName.text = "车队名称：${item.team_name}"
            binding.tvGroupType.text = "参与组别：${item.group_type}"
            binding.tvSupplement.text = "补充说明：${if (item.supplement.isEmpty()) "无" else item.supplement}"
            binding.tvTime.text = "报名时间：${item.registered_at}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRegistrationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}