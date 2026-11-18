package com.mh.calorie.ui.weight.record

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * 两页固定内容的 pager。
 * 页数少、不做回收，onCreate 直接交出已经 inflate 好的 View，
 * Fragment 那边就能一直持有 binding 引用，不用每次翻页重新找控件。
 */
class RecordPagerAdapter(
    private val pages: List<View>
) : RecyclerView.Adapter<RecordPagerAdapter.PageHolder>() {

    class PageHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun getItemCount(): Int = pages.size

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder =
        PageHolder(pages[viewType])

    override fun onBindViewHolder(holder: PageHolder, position: Int) = Unit
}