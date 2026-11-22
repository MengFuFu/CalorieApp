package com.mh.calorie.ui.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mh.calorie.R
import com.mh.calorie.databinding.ItemFoodRowBinding
import java.util.Locale

/** 食物列表。份数由外部查仓库给出，数据一变外部要 notifyDataSetChanged */
class FoodItemAdapter(
    private val unitOf: (FoodItem) -> String,
    private val countOf: (FoodItem) -> Int,
    private val onAdd: (FoodItem) -> Unit,
    private val onPlus: (FoodItem) -> Unit,
    private val onMinus: (FoodItem) -> Unit
) : ListAdapter<FoodItem, FoodItemAdapter.Holder>(DIFF) {

    inner class Holder(val binding: ItemFoodRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(ItemFoodRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        val b = holder.binding

        b.tvName.text = item.name
        b.tvKcal.text = HtmlCompat.fromHtml(
            String.format(
                Locale.US,
                "<font color=#F5A83C><b>%d</b></font> <font color=#8C8C8C>kcal%s</font>",
                item.kcal,
                unitOf(item)
            ),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        // 收藏：金色实心；未收藏：灰色空心
        b.ivStar.setColorFilter(
            ContextCompat.getColor(
                b.root.context,
                if (item.favorite) R.color.accent_yellow else R.color.text_tertiary
            )
        )

        val count = countOf(item)
        b.btnAdd.isVisible = count == 0
        b.groupStepper.isVisible = count > 0
        b.tvCount.text = count.toString()

        // 未添加：点加号先问份量；已添加：直接加减份数
        b.btnAdd.setOnClickListener { onAdd(item) }
        b.btnPlus.setOnClickListener { onPlus(item) }
        b.btnMinus.setOnClickListener { onMinus(item) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<FoodItem>() {
            override fun areItemsTheSame(a: FoodItem, b: FoodItem): Boolean = a.name == b.name
            override fun areContentsTheSame(a: FoodItem, b: FoodItem): Boolean = a == b
        }
    }
}