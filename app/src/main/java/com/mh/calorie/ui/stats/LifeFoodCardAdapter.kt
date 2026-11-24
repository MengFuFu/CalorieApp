package com.mh.calorie.ui.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mh.calorie.core.domain.model.FoodLog
import com.mh.calorie.databinding.ItemLifeFoodCardBinding
import java.util.Locale

/** 照片详情页的一页 = 一条饮食记录 */
class LifeFoodCardAdapter : RecyclerView.Adapter<LifeFoodCardAdapter.Holder>() {

    private val items = mutableListOf<FoodLog>()

    fun submit(list: List<FoodLog>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemLifeFoodCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(items[position])

    class Holder(private val binding: ItemLifeFoodCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(log: FoodLog) {
            binding.tvName.text = log.name
            binding.tvAmountKcal.text =
                String.format(Locale.US, "%.1f%s/%.0fkcal", log.baseQty, log.unit, log.kcal)
            binding.tvCarb.text = format(log.carbGram)
            binding.tvProtein.text = format(log.proteinGram)
            binding.tvFat.text = format(log.fatGram)
        }

        /** 4.0 → "4g"，4.5 → "4.5g" */
        private fun format(value: Float): String =
            if (value % 1f == 0f) "${value.toInt()}g" else String.format(Locale.US, "%.1fg", value)
    }
}