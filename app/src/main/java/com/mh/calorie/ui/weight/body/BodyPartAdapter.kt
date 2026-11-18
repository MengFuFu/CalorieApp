package com.mh.calorie.ui.weight.body

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.Hold
import com.mh.calorie.R
import com.mh.calorie.core.domain.model.BodyPart
import com.mh.calorie.databinding.ItemBodyPartBinding
import java.util.Locale

/** 我的围度 3 列网格；格子右上角的 + 交给外部打开记录面板 */
class BodyPartAdapter(
    private val onAddClick: (BodyPart) -> Unit
) : RecyclerView.Adapter<BodyPartAdapter.Holder>() {

    private var rows: List<BodyPartRow> = emptyList()

    fun submit(list: List<BodyPartRow>) {
        rows = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemBodyPartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding)
    }

    override fun getItemCount(): Int = rows.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val row = rows[position]
        holder.bind(row)
        holder.binding.btnAddPart.setOnClickListener { onAddClick(row.part) }
    }

    inner class Holder(val binding: ItemBodyPartBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: BodyPartRow) {
            val context = binding.root.context
            binding.ivPartIcon.setImageResource(row.part.iconRes())
            binding.tvPartName.setText(row.part.labelRes())
            binding.tvPartValue.text = if(row.valueCm == null) {
                context.getString(R.string.body_value_empty)
            } else {
                context.getString(R.string.body_value_cm, format(row.valueCm))
            }
        }

        private fun format(value: Float): String =
            if(value % 1f == 0f) value.toInt().toString()
            else String.format(Locale.US, "%.1f", value)
    }




}