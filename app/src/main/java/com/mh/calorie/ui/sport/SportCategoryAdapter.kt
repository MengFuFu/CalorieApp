package com.mh.calorie.ui.sport

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mh.calorie.R
import com.mh.calorie.databinding.ItemSportCategoryBinding

/** 左侧分类：单选，选中行白底 + 深色字加粗 */
class SportCategoryAdapter(
    private val onSelect: (SportCategory) -> Unit
) : RecyclerView.Adapter<SportCategoryAdapter.Holder>() {

    private val data = SportCategory.entries
    private var selected: SportCategory = SportCategory.FAVORITE

    fun select(category: SportCategory) {
        if (category == selected) return
        val old = data.indexOf(selected)
        selected = category
        notifyItemChanged(old)
        notifyItemChanged(data.indexOf(category))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemSportCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding, onSelect)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(data[position], data[position] == selected)
    }

    class Holder(
        private val binding: ItemSportCategoryBinding,
        private val onSelect: (SportCategory) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: SportCategory, isSelected: Boolean) {
            val ctx = binding.root.context
            binding.tvCategory.text = ctx.getString(category.labelRes)
            // selected 态交给 selector 画白底胶囊
            binding.tvCategory.isSelected = isSelected
            binding.tvCategory.setTextColor(
                ContextCompat.getColor(ctx, if (isSelected) R.color.text_primary else R.color.text_secondary)
            )
            binding.tvCategory.setTextAppearance(
                if (isSelected) com.mh.calorie.R.style.TextAppearance_Momo_CardTitle
                else com.mh.calorie.R.style.TextAppearance_Momo_Body
            )
            binding.tvCategory.post {
                val lp = binding.tvCategory.layoutParams
                lp.width = binding.tvCategory.parent.let { (it as ViewGroup).width }
                binding.tvCategory.layoutParams = lp
            }
            binding.tvCategory.setOnClickListener { onSelect(category) }
        }
    }
}