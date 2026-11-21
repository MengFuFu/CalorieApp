package com.mh.calorie.ui.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mh.calorie.databinding.ItemFoodRowBinding

/** 右侧食物行：热量数字橙色加粗、单位灰色 */
class FoodItemAdapter(
    private val unit: () -> String,
    private val onAdd: (FoodItem) -> Unit
) : ListAdapter<FoodItem, FoodItemAdapter.Holder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemFoodRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding, unit, onAdd)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    class Holder(
        private val binding: ItemFoodRowBinding,
        private val unit: () -> String,
        private val onAdd: (FoodItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FoodItem) {
            binding.tvName.text = item.name
            // 热量数字橙色、单位灰色：交给 TextView 解析一段 HTML
            binding.tvKcal.text = HtmlCompat.fromHtml(
                "<font color=\"#F5A83C\"><b>${item.kcal}</b></font>" +
                        "<font color=\"#8C8C8C\"> kcal${unit()}</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            binding.btnAdd.setOnClickListener { onAdd(item) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<FoodItem>() {
            override fun areItemsTheSame(a: FoodItem, b: FoodItem) = a.name == b.name
            override fun areContentsTheSame(a: FoodItem, b: FoodItem) = a == b
        }
    }
}