package com.mh.calorie.ui.sport

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mh.calorie.databinding.ItemSportDayBinding
import com.mh.calorie.databinding.ItemSportRecordRowBinding

/** 一天一张卡，卡内逐条运动 */
class SportRecordAdapter : ListAdapter<SportDayGroup, SportRecordAdapter.DayHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayHolder {
        val binding = ItemSportDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayHolder(binding)
    }

    override fun onBindViewHolder(holder: DayHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DayHolder(private val binding: ItemSportDayBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: SportDayGroup) {
            binding.tvDate.text = group.dateText
            binding.containerRows.removeAllViews()
            val inflater = LayoutInflater.from(binding.root.context)
            group.rows.forEach { row ->
                val rowBinding = ItemSportRecordRowBinding.inflate(
                    inflater, binding.containerRows, false
                )
                rowBinding.tvName.text = row.name
                rowBinding.tvQuantity.text = row.quantityText
                rowBinding.tvKcal.text = row.kcalText
                binding.containerRows.addView(rowBinding.root)
            }
        }

    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<SportDayGroup>() {
            override fun areItemsTheSame(a: SportDayGroup, b: SportDayGroup) = a.dateText == b.dateText
            override fun areContentsTheSame(a: SportDayGroup, b: SportDayGroup) = a == b
        }
    }
}