package com.example.myapplication.ui.detail

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.data.model.Diary

class DiaryDetailPagerAdapter(fragment: Fragment, private var diaries: List<Diary>) :
    FragmentStateAdapter(fragment) {

    fun setData(diariesList: List<Diary>) {
        diaries = diariesList
        notifyItemRangeInserted(diariesList.size, diaries.size - diariesList.size)
    }

    override fun getItemCount(): Int = diaries.size

    override fun createFragment(position: Int): Fragment =
        DiaryDetailFragment.newInstance(diaries[position].id)

}
