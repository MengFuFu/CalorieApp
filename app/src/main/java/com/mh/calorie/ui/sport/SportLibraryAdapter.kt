package com.mh.calorie.ui.sport

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mh.calorie.R
import com.mh.calorie.databinding.ItemSportLibraryRowBinding
import java.util.Locale

/** 右侧运动列表 */
class SportLibraryAdapter(
    private val onAdd: (SportItem) -> Unit
) : ListAdapter<SportItem, SportLibraryAdapter.Holder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemSportLibraryRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding, onAdd)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    class Holder(
        private val binding: ItemSportLibraryRowBinding,
        private val onAdd: (SportItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SportItem) {
            binding.tvName.text = item.name
            // 普通运动 sampleMinutes=10，自定义运动是用户填的分钟数
            val basis = item.sampleMinutes
            binding.tvKcal.text = formatKcal(item.kcalFor(basis.toFloat(), SportUnit.MIN))
            binding.tvBasis.text = binding.root.context
                .getString(R.string.sport_kcal_per_minutes, basis)
            binding.btnAdd.setOnClickListener { onAdd(item) }
        }

        /** 90 → "90"，83.3 → "83.3" */
        private fun formatKcal(value: Float): String =
            if (value % 1f == 0f) value.toInt().toString()
            else String.format(Locale.US, "%.1f", value)
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<SportItem>() {
            override fun areItemsTheSame(a: SportItem, b: SportItem): Boolean = a.name == b.name
            override fun areContentsTheSame(a: SportItem, b: SportItem): Boolean = a == b
        }
    }
}