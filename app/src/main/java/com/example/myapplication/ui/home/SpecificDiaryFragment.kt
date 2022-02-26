package com.example.myapplication.ui.home

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentDiariesBinding
import com.example.myapplication.net.Status
import com.example.myapplication.ui.me.MeFragment
import com.example.myapplication.ui.me.MeFragmentDirections
import com.example.myapplication.util.CommonConfig
import com.example.myapplication.util.DIARIES_TYPE_FOLLOWING
import com.example.myapplication.util.getFab
import com.example.myapplication.widget.AvatarDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SpecificDiaryFragment : Fragment() {

    companion object {
        fun newInstance(type: Int, id: Int): SpecificDiaryFragment {
            val args = Bundle()
            args.putInt("diaryType", type)
            args.putInt("id", id)
            val fragment = SpecificDiaryFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentDiariesBinding? = null
    private val binding get() = _binding!!

    private val diaryViewModel: DiaryViewModel by activityViewModels()

    private val adapter = DiaryListAdapter()

    private val args by navArgs<SpecificDiaryFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (parentFragment !is MeFragment) {
            enterTransition = MaterialFadeThrough().apply {
                duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiariesBinding.inflate(inflater, container, false)
//        exitTransition = MaterialFadeThrough().apply {
//            duration =
//                resources.getInteger(R.integer.motion_duration_medium)
//                    .toLong()
//        }
        subscribeUi()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (parentFragment is MeFragment) {
            parentFragment?.postponeEnterTransition()
            view.doOnPreDraw {
                parentFragment?.startPostponedEnterTransition()
            }
        } else {
            postponeEnterTransition()
            view.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    @SuppressLint("ShowToast")
    private fun subscribeUi() {
        binding.swipeRefresh.setOnRefreshListener {
            diaryViewModel.fetchSpecificDiaries(args.diaryType, args.id, true)
        }
        binding.recyclerView.adapter = adapter
        adapter.setEmptyView(R.layout.empty_view)
        val divider = AvatarDividerItemDecoration(
            ColorDrawable(ResourcesCompat.getColor(resources, R.color.divider, context?.theme))
        )
        binding.recyclerView.addItemDecoration(divider)
        adapter.setOnItemClickListener { adapter, view, position ->

            val transitionName =
                getString(R.string.detail_diary_transition_name, this.adapter.getItem(position).id)
            val extras = FragmentNavigatorExtras(view to transitionName)

            val action = if (parentFragment is MeFragment) {
                parentFragment?.exitTransition = MaterialElevationScale(false).apply {
                    duration = resources.getInteger(R.integer.motion_duration_large).toLong()
                }
                MeFragmentDirections.actionNavigationMeToDetailPagerFragment(
                    position,
                    DIARIES_TYPE_FOLLOWING
                )
            } else {
                exitTransition = MaterialElevationScale(false).apply {
                    duration = resources.getInteger(R.integer.motion_duration_large).toLong()
                }
                SpecificDiaryFragmentDirections.actionNavigationFollowDiariesToDetailPagerFragment(
                    position, DIARIES_TYPE_FOLLOWING
                )
            }
            findNavController().navigate(action, extras)
        }
        adapter.loadMoreModule.isEnableLoadMoreIfNotFullPage = false
        adapter.loadMoreModule.setOnLoadMoreListener {
            diaryViewModel.fetchSpecificDiaries(args.diaryType, args.id)
        }
        diaryViewModel.sDiaries.observe(viewLifecycleOwner) { it ->
            when (it.status) {
                Status.LOADING -> {
                    binding.swipeRefresh.isRefreshing = true
                }
                Status.SUCCESS -> {
                    binding.swipeRefresh.isRefreshing = false
                    if (it.data.isNullOrEmpty()) {
                        adapter.setNewInstance(null)
                    } else {
                        when {
                            // 请求到的数据不满一页
                            it.data.size < CommonConfig.PAGE_SIZE_DIARIES -> {
                                adapter.setList(it.data)
                                adapter.loadMoreModule.loadMoreEnd(true)
                            }
                            // 没有请求到更多数据
                            it.data.size > CommonConfig.PAGE_SIZE_DIARIES &&
                                    it.data.size == adapter.data.size -> adapter.loadMoreModule.loadMoreEnd(
                                true
                            )
                            else -> {
                                adapter.setList(it.data)
                                adapter.loadMoreModule.loadMoreComplete()
                            }
                        }
                    }
                }
                Status.ERROR -> {
                    binding.swipeRefresh.isRefreshing = false
                    Snackbar.make(
                        binding.recyclerView,
                        it.message ?: getString(R.string.fetch_diary_error),
                        Snackbar.LENGTH_SHORT
                    )
                        .setAnchorView(getFab())
                        .show()
                }
            }
        }
        diaryViewModel.fetchSpecificDiaries(args.diaryType, args.id, true)
        parentFragment?.startPostponedEnterTransition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}