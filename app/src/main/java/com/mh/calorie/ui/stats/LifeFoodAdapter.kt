package com.mh.calorie.ui.stats

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mh.calorie.R
import com.mh.calorie.core.domain.model.FoodLog
import com.mh.calorie.core.domain.model.MealType
import com.mh.calorie.databinding.ItemLifeFoodPhotoBinding
import com.mh.calorie.ui.common.labelRes

/** 生活美食统计：一天的食物照片横滑条 */
class LifeFoodAdapter : ListAdapter<FoodLog, LifeFoodAdapter.Holder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(
            ItemLifeFoodPhotoBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: Holder, position: Int) =
        holder.bind(getItem(position))

    class Holder(private val binding: ItemLifeFoodPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(log: FoodLog) {
            binding.tvMealTag.setText(log.meal.labelRes)
            binding.tvMealTag.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(binding.root.context, tagColor(log.meal))
            )
            // 无障碍：读屏会念「米饭 午餐」
            binding.root.contentDescription = "${log.name} ${log.meal}"
            // TODO M12 接到 AI 识别的照片后：Coil 加载 log.photoUri 到 ivPhoto
        }

        /** 早=灰、午=绿、晚=紫、加餐=粉，和设计稿一致 */
        private fun tagColor(meal: MealType): Int = when (meal) {
            MealType.BREAKFAST -> R.color.meal_tag_breakfast
            MealType.LUNCH -> R.color.meal_tag_lunch
            MealType.DINNER -> R.color.meal_tag_dinner
            MealType.SNACK -> R.color.meal_tag_snack
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<FoodLog>() {
            override fun areItemsTheSame(a: FoodLog, b: FoodLog) = a.id == b.id
            override fun areContentsTheSame(a: FoodLog, b: FoodLog) = a == b
        }
    }
}