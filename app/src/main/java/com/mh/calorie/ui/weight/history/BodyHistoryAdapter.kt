package com.mh.calorie.ui.weight.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mh.calorie.R
import com.mh.calorie.databinding.ItemBodyHistoryRowBinding
import com.mh.calorie.databinding.ItemWeightDayBinding
import com.mh.calorie.ui.weight.body.iconRes
import com.mh.calorie.ui.weight.body.labelRes
import java.util.Locale

/** 一天一张卡；卡内行数不定，直接往容器里 inflate（与体重历史页同一套做法） */
class BodyHistoryAdapter : ListAdapter<BodyDayGroup, BodyHistoryAdapter.DayViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemWeightDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DayViewHolder(private val binding: ItemWeightDayBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: BodyDayGroup) {
            val ctx = binding.root.context
            binding.tvDate.text = group.dateText

            binding.containerRows.removeAllViews()
            group.rows.forEach { row ->
                val rowBinding = ItemBodyHistoryRowBinding.inflate(
                    LayoutInflater.from(ctx), binding.containerRows, false
                )
                rowBinding.ivPartIcon.setImageResource(row.part.iconRes())
                rowBinding.tvValue.text = ctx.getString(R.string.body_cm_value, formatCm(row.valueCm))
                rowBinding.tvPart.text = ctx.getString(row.part.labelRes())
                rowBinding.tvTime.text = row.time
                binding.containerRows.addView(rowBinding.root)
            }
        }

        /** 40 → "40"，40.5 → "40.5" */
        private fun formatCm(value: Float): String =
            if (value % 1f == 0f) value.toInt().toString()
            else String.format(Locale.US, "%.1f", value)
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<BodyDayGroup>() {
            override fun areItemsTheSame(a: BodyDayGroup, b: BodyDayGroup): Boolean =
                a.dateText == b.dateText

            override fun areContentsTheSame(a: BodyDayGroup, b: BodyDayGroup): Boolean = a == b
        }
    }
}