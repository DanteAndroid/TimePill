package com.example.myapplication.ui.follow

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.data.model.User
import com.example.myapplication.databinding.FragmentFollowBinding
import com.example.myapplication.net.Resource
import com.example.myapplication.net.Status
import com.example.myapplication.util.CommonConfig
import com.example.myapplication.util.getFab
import com.example.myapplication.widget.AvatarDividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FollowingFragment : Fragment() {

    private var _binding: FragmentFollowBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UsersViewModel by viewModels()

    private val adapter: FollowUserAdapter = FollowUserAdapter()

    private var type: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFollowBinding.inflate(inflater)
        subscribeUi()
        return binding.root
    }

    @SuppressLint("ShowToast")
    private fun subscribeUi() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchFollowers(isFollowing = true, isRefresh = true)
        }
        val divider = AvatarDividerItemDecoration(
            ColorDrawable(ResourcesCompat.getColor(resources, R.color.divider, context?.theme))
        )
        binding.recyclerView.addItemDecoration(divider)
        adapter.loadMoreModule.isEnableLoadMoreIfNotFullPage = false
        adapter.loadMoreModule.setOnLoadMoreListener {
            viewModel.fetchFollowers(isFollowing = true)
        }
        adapter.setOnItemClickListener { adapter, view, position ->
            val user = this.adapter.getItem(position)
            parentFragment?.exitTransition = MaterialElevationScale(false).apply {
                duration = resources.getInteger(R.integer.motion_duration_large).toLong()
            }
            val extras =
                FragmentNavigatorExtras(view to getString(R.string.item_user_transition_name))
            val action = FollowHomeFragmentDirections.actionNavigationFollowToNavigationMe(user.id)
            findNavController().navigate(action, extras)
        }
        adapter.setOnItemLongClickListener { adapter, view, position ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cancel_follow_message)
                .setPositiveButton(R.string.cancel_follow) { _, i ->
                    cancelFollow(position)
                }.show()
            return@setOnItemLongClickListener true
        }
        binding.recyclerView.adapter = adapter
        adapter.setEmptyView(R.layout.empty_view)
        viewModel.followers.observe(viewLifecycleOwner, { it ->
            when (it.status) {
                Status.LOADING -> {
                    binding.swipeRefresh.isRefreshing = true
                }
                Status.SUCCESS -> {
                    binding.swipeRefresh.isRefreshing = false
                    if (viewModel.isLoadingMore) {
                        onLoadMore(it)
                    } else {
                        onNewData(it)
                    }
                }
                Status.ERROR -> {
                    binding.swipeRefresh.isRefreshing = false
                    Snackbar.make(
                        binding.recyclerView,
                        it.message ?: getString(R.string.cant_get_followings),
                        Snackbar.LENGTH_SHORT
                    )
                        .setAnchorView(getFab())
                        .show()
                }
            }
        })

        viewModel.fetchFollowers(true)
    }

    private fun onNewData(resource: Resource<List<User>>) {
        resource.data?.let {
            adapter.setList(it)
            if (it.size < CommonConfig.PAGE_SIZE_USERS) {
                adapter.loadMoreModule.loadMoreEnd(true)
            }
            binding.followingCount.text = getString(
                R.string.total_followings,
                it.size
            )
        }
    }

    private fun onLoadMore(resource: Resource<List<User>>) {
        resource.data?.let {
            adapter.addData(it)
            if (it.size < CommonConfig.PAGE_SIZE_USERS) {
                adapter.loadMoreModule.loadMoreEnd(true)
            } else {
                adapter.addData(it)
                adapter.loadMoreModule.loadMoreComplete()
            }
        }
    }

    private fun cancelFollow(position: Int) {
        val data = adapter.getItem(position)
        try {
            viewModel.cancelFollow(data.id)
            adapter.removeAt(position)
            Snackbar.make(
                binding.recyclerView,
                R.string.cancel_follow_success,
                Snackbar.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(
                binding.recyclerView,
                R.string.cancel_follow_failed,
                Snackbar.LENGTH_SHORT
            )
                .setAnchorView(getFab())
                .show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}