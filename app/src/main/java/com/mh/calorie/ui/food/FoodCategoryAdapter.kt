package com.mh.calorie.ui.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mh.calorie.R
import com.mh.calorie.databinding.ItemFoodCategoryBinding

/** 左侧分类：选中项白底 + 深色字 */
class FoodCategoryAdapter(
    private val onPick: (FoodCategory) -> Unit
) : RecyclerView.Adapter<FoodCategoryAdapter.Holder>() {

    private val items = FoodCategory.entries
    private var selected: FoodCategory = FoodCategory.STAPLE

    fun select(category: FoodCategory) {
        if(category == selected) return
        val old = items.indexOf(selected)
        selected = category
        notifyItemChanged(old)
        notifyItemChanged(items.indexOf(category))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemFoodCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding, onPick)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position], items[position] == selected)
    }

    override fun getItemCount(): Int = items.size

    class Holder(
        private val binding: ItemFoodCategoryBinding,
        private val onPick: (FoodCategory) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: FoodCategory, isSelected: Boolean) {
            val context = binding.root.context
            binding.root.isSelected = isSelected
            binding.tvCategory.text = category.title
            binding.tvCategory.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isSelected) R.color.text_primary else R.color.text_secondary
                )
            )
            binding.tvCategory.setTextSize(if (isSelected) 15f else 14f)
            binding.root.setOnClickListener { onPick(category) }
        }
    }

}