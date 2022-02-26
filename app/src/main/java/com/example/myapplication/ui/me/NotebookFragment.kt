package com.example.myapplication.ui.me

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.data.model.Notebook
import com.example.myapplication.databinding.FragmentNotebookBinding
import com.example.myapplication.net.Status
import com.example.myapplication.ui.edit.EditViewModel
import com.example.myapplication.ui.follow.UsersViewModel
import com.example.myapplication.util.DIARIES_TYPE_NOTEBOOK
import com.example.myapplication.util.DataStoreUtil
import com.example.myapplication.util.getFab
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotebookFragment : Fragment() {

    companion object {

        const val ARGS_USER_ID = "userId"
        fun newInstance(userId: Int = 0): NotebookFragment {
            val args = Bundle()
            args.putInt(ARGS_USER_ID, userId)
            val fragment = NotebookFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: UsersViewModel by viewModels()
    private val editViewModel: EditViewModel by viewModels()

    private var _binding: FragmentNotebookBinding? = null
    private val binding get() = _binding!!

    private val adapter by lazy { NotebookAdapter() }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotebookBinding.inflate(inflater, container, false)
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
        val userId = arguments?.getInt(ARGS_USER_ID) ?: DataStoreUtil.getMyId()
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchNotebooks(userId)
        }
        binding.recyclerView.adapter = adapter
        adapter.setEmptyView(R.layout.empty_view)
        adapter.addChildClickViewIds(R.id.more, R.id.cover)
        adapter.setOnItemChildClickListener { _, view, position ->
            if (view.id == R.id.more) {
                moreClicked(view, position)
            } else {
                val directions =
                    MeFragmentDirections.actionNavigationMeToNavigationSpecificDiaries(
                        DIARIES_TYPE_NOTEBOOK,
                        adapter.getItem(position).id
                    )
                findNavController().navigate(directions)
            }
        }
        viewModel.fetchNotebooks(userId).observe(viewLifecycleOwner, { it ->
            when (it.status) {
                Status.LOADING -> {
                    binding.swipeRefresh.isRefreshing = true
                }
                Status.SUCCESS -> {
                    binding.swipeRefresh.isRefreshing = false
                    it.data?.let { _notebooks ->
                        populateUi(_notebooks)
                    }
                }
                Status.ERROR -> {
                    binding.swipeRefresh.isRefreshing = false
                    Snackbar.make(
                        binding.root,
                        it.message ?: getString(R.string.fetch_user_error),
                        Snackbar.LENGTH_SHORT
                    )
                        .setAnchorView(getFab())
                        .show()
                }
            }
        })
    }

    private fun moreClicked(view: View, position: Int) {
        val n = adapter.getItem(position)
        val popup = PopupMenu(context, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.edit_menu_more, popup.menu)
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_edit -> {
                    val action = MeFragmentDirections.actionGlobalEditNotebookFragment(
                        adapter.getItem(position)
                    )
                    findNavController().navigate(action)
                }
                R.id.action_delete -> deleteNotebook(view, position, n)
                else -> {

                }
            }
            false
        }
        popup.show()
    }

    private fun deleteNotebook(view: View, position: Int, n: Notebook) {
        editViewModel.deleteNotebook(n.id).observe(viewLifecycleOwner) {
            Snackbar.make(
                binding.recyclerView,
                if (it) (R.string.delete_notebook_success) else R.string.delete_notebook_failed,
                Snackbar.LENGTH_SHORT
            )
                .setAnchorView(getFab())
                .show()
            if (it) adapter.removeAt(position)
        }
    }

    private fun populateUi(notebooks: List<Notebook>) {
        adapter.setNewInstance(notebooks.toMutableList())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}