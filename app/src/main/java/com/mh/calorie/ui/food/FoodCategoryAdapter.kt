package com.mh.calorie.ui.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mh.calorie.R
import com.mh.calorie.databinding.ItemFoodCategoryBinding

/**
 * 左侧分类：选中项白底 + 深色字。
 * 选中态由 adapter 自己维护——点谁谁亮，外部不用再手动调 select()。
 */
class FoodCategoryAdapter(
    private val onPick: (FoodCategory) -> Unit
) : RecyclerView.Adapter<FoodCategoryAdapter.Holder>() {

    private val items = FoodCategory.entries
    private var selected: FoodCategory = FoodCategory.STAPLE

    /** 只在「代码里主动改选中」时用（例如进页面时恢复到上次的分类） */
    fun select(category: FoodCategory) {
        if (category == selected) return
        val old = items.indexOf(selected)
        selected = category
        if (old >= 0) notifyItemChanged(old)
        notifyItemChanged(items.indexOf(category))
    }

    /** 点击：先挪胶囊，再通知页面换列表 */
    private fun pick(category: FoodCategory) {
        select(category)
        onPick(category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(
            ItemFoodCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        ) { category -> pick(category) }

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
            // item 布局根就是 tvCategory，选中态直接落在它身上
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