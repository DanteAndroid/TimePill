package com.example.myapplication.ui.notifications

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentNotificationsBinding
import com.example.myapplication.net.Status
import com.example.myapplication.util.CommonConfig
import com.example.myapplication.util.getFab
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationsViewModel by viewModels()

    private val adapter: NotificationAdapter = NotificationAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        subscribeUi()
        return binding.root
    }

    @SuppressLint("ShowToast")
    private fun subscribeUi() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchNotifications(isRefresh = true)
        }
        binding.recyclerView.adapter = adapter
        adapter.setEmptyView(R.layout.empty_view)
        viewModel.tipResults.observe(viewLifecycleOwner, { it ->
            when (it.status) {
                Status.LOADING -> {
                    binding.swipeRefresh.isRefreshing = true
                }
                Status.SUCCESS -> {
                    binding.swipeRefresh.isRefreshing = false
                    if (!it.data.isNullOrEmpty()) {
                        if (it.data.size < CommonConfig.PAGE_SIZE_USERS) {
                            adapter.loadMoreModule.loadMoreEnd()
                        }
                        var unread = 0
                        it.data.map {
                            if (it.read == 0) unread++
                        }
                        binding.unreadCount.text = getString(R.string.total_tips, unread)
                        adapter.setList(it.data)
                    }
                }
                Status.ERROR -> {
                    binding.swipeRefresh.isRefreshing = false
                    Snackbar.make(
                        binding.recyclerView,
                        it.message ?: getString(R.string.cant_get_notifications),
                        Snackbar.LENGTH_SHORT
                    )
                        .setAnchorView(getFab())
                        .show()
                }
            }
        })

        viewModel.fetchNotifications(true)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}