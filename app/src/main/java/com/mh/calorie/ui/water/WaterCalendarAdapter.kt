package com.mh.calorie.ui.water

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mh.calorie.R
import com.mh.calorie.databinding.ItemWaterDayBinding

/** 日历格子：day 为 null 表示这个位置是上/下个月的空白 */
data class WaterDayCell(
    val day: Int? = null,
    val ratio: Float = 0f,
    val isToday: Boolean = false,
    val dateIso: String? = null,
    val enabled: Boolean = true        // 未来日期为 false
)

class WaterCalendarAdapter(
    private val onClick: (WaterDayCell) -> Unit
) : ListAdapter<WaterDayCell, WaterCalendarAdapter.Holder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemWaterDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding, onClick)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    class Holder(
        private val binding: ItemWaterDayBinding,
        private val onClick: (WaterDayCell) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cell: WaterDayCell) {
            val context = binding.root.context

            if (cell.day == null) {
                binding.root.visibility = android.view.View.INVISIBLE
                return
            }
            binding.root.visibility = android.view.View.VISIBLE

            when {
                cell.isToday -> {
                    binding.tvDay.text = ContextCompat.getString(context, R.string.water_today_mark)
                    binding.tvDay.setBackgroundResource(R.drawable.bg_today_circle)
                    binding.tvDay.setTextColor(ContextCompat.getColor(context, R.color.water_blue))
                    binding.tvDay.setTextSize(13f)
                }
                !cell.enabled -> {
                    // 未来日期：数字置灰、水杯不画、点击无效
                    binding.tvDay.text = cell.day.toString()
                    binding.tvDay.background = null
                    binding.tvDay.setTextColor(ContextCompat.getColor(context, R.color.text_tertiary))
                    binding.tvDay.setTextSize(15f)
                }
                else -> {
                    binding.tvDay.text = cell.day.toString()
                    binding.tvDay.background = null
                    binding.tvDay.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    binding.tvDay.setTextSize(15f)
                }
            }

            binding.ivCup.setRatio(if (cell.enabled) cell.ratio else 0f)
            binding.root.setOnClickListener { if (cell.enabled) onClick(cell) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<WaterDayCell>() {
            override fun areItemsTheSame(a: WaterDayCell, b: WaterDayCell) =
                a.day == b.day && a.dateIso == b.dateIso

            override fun areContentsTheSame(a: WaterDayCell, b: WaterDayCell) = a == b
        }
    }
}