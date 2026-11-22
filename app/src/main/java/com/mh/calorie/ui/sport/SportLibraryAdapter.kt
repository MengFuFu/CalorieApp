package com.mh.calorie.ui.sport

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mh.calorie.R
import com.mh.calorie.databinding.ItemSportLibraryRowBinding
import java.util.Locale

/** 右侧运动列表；已加入草稿的行显示绿色对勾 */
class SportLibraryAdapter(
    private val onAdd: (SportItem) -> Unit
) : ListAdapter<SportItem, SportLibraryAdapter.Holder>(DIFF) {

    /** 已加入草稿的运动名，用来打勾 */
    private var draftedNames: Set<String> = emptySet()

    fun setDrafted(names: Set<String>) {
        if (names == draftedNames) return
        draftedNames = names
        notifyItemRangeChanged(0, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemSportLibraryRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding, onAdd) { draftedNames }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    class Holder(
        private val binding: ItemSportLibraryRowBinding,
        private val onAdd: (SportItem) -> Unit,
        private val drafted: () -> Set<String>
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SportItem) {
            binding.tvName.text = item.name
            // 普通运动 sampleMinutes=10，自定义运动是用户填的分钟数
            val basis = item.sampleMinutes
            binding.tvKcal.text = formatKcal(item.kcalFor(basis.toFloat(), SportUnit.MIN))
            binding.tvBasis.text = binding.root.context
                .getString(R.string.sport_kcal_per_minutes, basis)

            val isDrafted = item.name in drafted()
            val ctx = binding.root.context
            binding.btnAdd.setBackgroundResource(
                if (isDrafted) R.drawable.bg_add_circle_done else R.drawable.bg_add_circle
            )
            binding.btnAdd.setImageResource(if (isDrafted) R.drawable.ic_check else R.drawable.ic_add)
            binding.btnAdd.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(ctx, if (isDrafted) R.color.white else R.color.text_secondary)
            )
            binding.btnAdd.setOnClickListener {
                // 已经在草稿里就再记一条？不——提示一下，避免误以为是切换
                if (isDrafted) {
                    android.widget.Toast.makeText(
                        ctx, R.string.sport_drafted_tip, android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    onAdd(item)
                }
            }
        }

        /** 90 → "90"，83.3 → "83.3" */
        private fun formatKcal(value: Float): String =
            if (value % 1f == 0f) value.toInt().toString()
            else String.format(Locale.US, "%.1f", value)
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<SportItem>() {
            override fun areItemsTheSame(a: SportItem, b: SportItem): Boolean = a.name == b.name
            override fun areContentsTheSame(a: SportItem, b: SportItem): Boolean = a == b
        }
    }
}