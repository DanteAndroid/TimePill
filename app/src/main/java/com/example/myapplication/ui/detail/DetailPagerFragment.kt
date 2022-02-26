package com.example.myapplication.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.data.model.Diary
import com.example.myapplication.databinding.FragmentDetailPagerBinding
import com.example.myapplication.ui.home.DiaryViewModel
import com.example.myapplication.util.DIARIES_TYPE_FOLLOWING
import com.example.myapplication.util.DIARIES_TYPE_HOME
import com.example.myapplication.util.themeColor
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailPagerFragment : Fragment() {

    private lateinit var adapter: DiaryDetailPagerAdapter
    private var _binding: FragmentDetailPagerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DiaryViewModel by activityViewModels()

    private val args by navArgs<DetailPagerFragmentArgs>()

    private var lastPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
//            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
        }
        postponeEnterTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailPagerBinding.inflate(inflater, container, false)
        subscribeUi()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    private fun subscribeUi() {
        getDataSource().observe(viewLifecycleOwner, {
            if (binding.pager.adapter == null) {
                adapter = DiaryDetailPagerAdapter(this, it)
                binding.pager.adapter = adapter
                binding.pager.offscreenPageLimit = 1
                binding.pager.setCurrentItem(
                    if (lastPosition > 0) lastPosition else args.position,
                    false
                )
                binding.pager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        if (position != args.position) {
                            sharedElementReturnTransition = null
                        } else {
                            sharedElementReturnTransition = MaterialContainerTransform().apply {
                                drawingViewId = R.id.nav_host_fragment
                                duration =
                                    resources.getInteger(R.integer.motion_duration_large).toLong()
                                //            scrimColor = Color.TRANSPARENT
                                setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
                            }
                        }
                        lastPosition = position
                    }
                })

            } else {
                adapter.setData(it)
            }
        })
    }

    fun currentFragment(): Fragment? {
        try {
            return childFragmentManager.findFragmentByTag("f" + binding.pager.currentItem)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getDataSource(): LiveData<List<Diary>> = when (args.diaryType) {
        DIARIES_TYPE_HOME -> viewModel.diariesCached
        DIARIES_TYPE_FOLLOWING -> viewModel.specificDiariesCached
        else -> viewModel.specificDiariesCached
    }

}