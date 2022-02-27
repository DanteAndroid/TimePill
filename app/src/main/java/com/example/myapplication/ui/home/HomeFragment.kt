package com.example.myapplication.ui.home

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.Diary
import com.example.myapplication.databinding.FragmentDiariesBinding
import com.example.myapplication.net.Resource
import com.example.myapplication.net.Status
import com.example.myapplication.util.CommonConfig
import com.example.myapplication.util.DIARIES_TYPE_HOME
import com.example.myapplication.util.getFab
import com.example.myapplication.widget.AvatarDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentDiariesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DiaryViewModel by activityViewModels()

    private val adapter = DiaryListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiariesBinding.inflate(inflater, container, false)
        exitTransition = MaterialFadeThrough().apply {
            duration =
                resources.getInteger(com.example.myapplication.R.integer.motion_duration_medium)
                    .toLong()
        }
        subscribeUi()
        return binding.root
    }

    private var resource: Resource<List<Diary>>? = null

    private fun subscribeUi() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchDiaries(true)
        }
        binding.recyclerView.adapter = adapter
        adapter.setEmptyView(R.layout.empty_view)
        val divider = AvatarDividerItemDecoration(
            ColorDrawable(ResourcesCompat.getColor(resources, R.color.divider, context?.theme))
        )
        binding.recyclerView.addItemDecoration(divider)
        adapter.setOnItemClickListener { adapter, view, position ->
            exitTransition = MaterialElevationScale(false).apply {
                duration = resources.getInteger(R.integer.motion_duration_large).toLong()
            }
            val transitionName =
                getString(R.string.detail_diary_transition_name, this.adapter.getItem(position).id)
            val extras = FragmentNavigatorExtras(view to transitionName)

            val action = HomeFragmentDirections.actionNavigationHomeToDetailPagerFragment(
                DIARIES_TYPE_HOME,
                position
            )
            findNavController().navigate(action, extras)
        }
//        adapter.addChildClickViewIds(R.id.avatar)
//        adapter.setOnItemChildClickListener { adapter, view, position ->
//            val user = this.adapter.getItem(position)
//            exitTransition = MaterialElevationScale(false).apply {
//                duration = resources.getInteger(R.integer.motion_duration_large).toLong()
//            }
//            val extras = FragmentNavigatorExtras(view to getString(R.string.item_user_transition_name))
//            val action = FollowHomeFragmentDirections.actionNavigationFollowToNavigationMe(user.userId)
//            findNavController().navigate(action, extras)
//        }
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val manager = recyclerView.layoutManager as LinearLayoutManager
                if (manager.findLastVisibleItemPosition() > manager.itemCount - CommonConfig.PRELOAD_COUNT) {
                    viewModel.fetchDiaries()
                }
            }
        })

        viewModel.diaries.observe(viewLifecycleOwner, { it ->
            if (it == resource) return@observe
            resource = it
            when (it.status) {
                Status.LOADING -> {
                    binding.swipeRefresh.isRefreshing = true
                }
                Status.SUCCESS -> {
                    binding.swipeRefresh.isRefreshing = false
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
        })
        viewModel.diariesCached.observe(viewLifecycleOwner, {
            adapter.setDiffNewData(it.toMutableList())
        })
        viewModel.fetchDiaries(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}