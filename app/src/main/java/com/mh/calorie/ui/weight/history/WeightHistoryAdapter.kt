package com.mh.calorie.ui.weight.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mh.calorie.R
import com.mh.calorie.databinding.ItemWeightDayBinding
import com.mh.calorie.databinding.ItemWeightHistoryRowBinding
import java.util.Locale

/** 一天一张卡；卡里行数不定，直接往容器里 inflate，不搞多 ViewType */
class WeightHistoryAdapter : ListAdapter<WeightDayGroup, WeightHistoryAdapter.DayViewHolder>(DIFF) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DayViewHolder {
        val binding = ItemWeightDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: DayViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class DayViewHolder(private val binding: ItemWeightDayBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: WeightDayGroup) {
            binding.tvDate.text = group.dateText

            binding.containerRows.removeAllViews()
            group.rows.forEach { row ->
                val rowBinding = ItemWeightHistoryRowBinding.inflate(
                    LayoutInflater.from(binding.root.context), binding.containerRows, false
                )
                val ctx = binding.root.context
                rowBinding.tvWeight.text =
                    ctx.getString(R.string.weight_kg_value, formatKg(row.weightKg))
                rowBinding.tvBodyFat.text = ctx.getString(
                    R.string.weight_body_fat_value,
                    row.bodyFatPercent?.let { formatKg(it) } ?: "--"
                )
                rowBinding.tvTime.text = row.time
                binding.containerRows.addView(rowBinding.root)
            }
        }

        private fun formatKg(value: Float): String =
            if (value % 1f == 0f) value.toInt().toString()
            else String.format(Locale.US, "%.1f", value)
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<WeightDayGroup>() {
            override fun areItemsTheSame(
                a: WeightDayGroup,
                b: WeightDayGroup
            ): Boolean = a.dateText == b.dateText

            override fun areContentsTheSame(
                a: WeightDayGroup,
                b: WeightDayGroup
            ): Boolean = a == b

        }
    }
}