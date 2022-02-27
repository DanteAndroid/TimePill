package com.example.myapplication.ui.detail

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.myapplication.R
import com.example.myapplication.data.model.Diary
import com.example.myapplication.databinding.FragmentDiaryDetailBinding
import com.example.myapplication.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DiaryDetailFragment : Fragment() {

    companion object {
        fun newInstance(diaryId: Int): DiaryDetailFragment {
            return DiaryDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_DIARY_ID, diaryId)
                }
            }
        }
    }

    private var _binding: FragmentDiaryDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DiaryDetailViewModel by viewModels()

    private val adapter = DiaryCommentsAdapter()
    private var diaryId: Int = 0
    private var diary: Diary? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            diaryId = it.getInt(KEY_DIARY_ID)
        }
    }

    fun commentDiary() {
        val view = layoutInflater.inflate(R.layout.comment_layout, null)
        val editText = view.findViewById<EditText>(R.id.commentEt)
        editText.requestFocus()
        MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setPositiveButton(R.string.action_send, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    KeyboardUtils.hideSoftInput(editText)
                    sendComment(editText.text.toString())
                }
            })
            .setOnDismissListener {
                KeyboardUtils.hideSoftInput(editText)
            }
            .show()
        KeyboardUtils.showSoftInput(editText)
    }

    fun commentDiary(recipientId: Int, userName: String) {
        val view = layoutInflater.inflate(R.layout.comment_layout, null)
        val editText = view.findViewById<EditText>(R.id.commentEt)
        editText.hint = getString(R.string.reply_to_xxx, userName)
        editText.requestFocus()
        MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setPositiveButton(
                R.string.action_send
            ) { dialog, which ->
                KeyboardUtils.hideSoftInput(editText)
                sendComment(editText.text.toString(), recipientId)
            }
            .setOnDismissListener {
                KeyboardUtils.hideSoftInput(editText)
            }
            .show()
        KeyboardUtils.showSoftInput(editText)
    }

    private fun sendComment(comment: String, recipientId: Int = 0) {
        if (comment.isEmpty()) return
        val data = HashMap<String, Any>()
        data[CONTENT] = comment
        if (recipientId > 0) {
            data["recipient_id"] = recipientId
        }
        viewModel.postComment(diaryId, data)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiaryDetailBinding.inflate(inflater, container, false)
        subscribeUi()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (parentFragment is DetailPagerFragment) {
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


    @SuppressLint("SetTextI18n")
    private fun subscribeUi() {
        binding.root.transitionName = getString(R.string.detail_diary_transition_name, diaryId)
        binding.commentLayout.commentsList.adapter = adapter
        adapter.setEmptyView(R.layout.empty_comment)
        adapter.setOnItemClickListener { _, view, position ->
            val comment = adapter.getItem(position)
            val items = resources.getStringArray(
                if (diary!!.userId.isMyId())
                    R.array.comment_menu_my
                else R.array.comment_menu
            )
            MaterialAlertDialogBuilder(requireContext())
                .setItems(items) { dialogInterface, p ->
                    when (p) {
                        0 -> {
                            commentDiary(comment.userId, comment.user.name)
                        }
                        1 -> {
                            ClipboardUtils.copyText(adapter.getItem(position).content)
                            ToastUtils.showShort(R.string.copyed)
                        }
                        2 -> {
                            ToastUtils.showShort("暂时不支持举报回复功能，如果您有任何建议欢迎到APP设置里反馈~")
                        }
                        3 -> {
                            viewModel.deleteComment(comment.id) {
                                adapter.removeAt(p)
                            }
                        }
                    }
                }.show()
        }

        viewModel.comment.observe(viewLifecycleOwner) {
            adapter.addData(it)
            binding.scrollView.scrollToBottom()
        }

        viewModel.getDiary(diaryId).observe(viewLifecycleOwner) { diary ->
            if (diary == null) return@observe
            this.diary = diary
            binding.userName.text = diary.user!!.name
            binding.content.text = diary.content
            binding.notebookSubject.text = "${diary.notebookSubject}"
            binding.time.text = TimeUtils.getFriendlyTimeSpanByNow(diary.created)
            binding.menuMore.isVisible = diary.userId.isMyId()
            binding.menuMore.setOnClickListener {
                val popup = PopupMenu(context, it)
                popup.menuInflater.inflate(R.menu.edit_menu_more, popup.menu)
                popup.setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            val action =
                                DiaryDetailFragmentDirections.actionGlobalEditDiaryFragment(diary)
                            findNavController().navigate(action)
                        }
                        R.id.action_delete -> {
                            viewModel.deleteDiary(diaryId) {
                                Snackbar.make(
                                    binding.root,
                                    R.string.diary_delete_success,
                                    Snackbar.LENGTH_SHORT
                                )
                                    .setAnchorView(getFab())
                                    .show()
//                                (parentFragment as? DetailPagerFragment)
                            }
                        }
                        else -> {
                        }
                    }
                    false
                }
                popup.show()
            }
            binding.avatar.transitionName =
                getString(R.string.item_user_transition_name) + diary.userId
            binding.avatar.setOnClickListener { view ->
                if (diary.userId.isMyId()) {
                    Snackbar.make(
                        binding.root,
                        R.string.this_is_yourself,
                        Snackbar.LENGTH_SHORT
                    )
                        .setAnchorView(getFab())
                        .show()
                    return@setOnClickListener
                }

                parentFragment?.exitTransition = MaterialElevationScale(false).apply {
                    duration = resources.getInteger(R.integer.motion_duration_large).toLong()
                }

                val extras =
                    FragmentNavigatorExtras(view to getString(R.string.item_user_transition_name))

                val action =
                    DetailPagerFragmentDirections.actionDetailPagerFragmentToNavigationMe(diary.userId)
                findNavController().navigate(action, extras)
            }

            Glide.with(this).load(diary.user!!.avatarUrl).into(binding.avatar)
            if (!diary.photoUrl.isNullOrEmpty()) {
                binding.picture.isVisible = true
                Glide.with(this)
                    .load(diary.photoUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.picture)
            } else {
                binding.picture.isVisible = false
            }
            viewModel.fetchComments(diaryId)
        }

        viewModel.getComments(diaryId).observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                binding.commentLayout.commentsCount.isVisible = false
//                binding.commentLayout.commentsList.isVisible = false
            } else {
                binding.commentLayout.commentsCount.isVisible = true
//                binding.commentLayout.commentsList.isVisible = true
                binding.commentLayout.commentsCount.text =
                    getString(R.string.total_comments, it.size)
                adapter.setDiffNewData(it.toMutableList())
            }
        }
    }

    private fun deleteComment(commentId: Int) {

    }

    fun reportDiary() {
        diary?.let {
            viewModel.reportDiary(it.userId, diaryId) {
                Snackbar.make(
                    binding.root,
                    R.string.report_success,
                    Snackbar.LENGTH_SHORT
                )
                    .setAnchorView(getFab())
                    .show()
            }
        }
    }

    fun shareDiary() {
        diary?.let {
            Share.shareText(requireContext(), it.content)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share_diary -> {
                shareDiary()
            }
            R.id.report_diary -> {
                reportDiary()
            }
        }
        return true
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.bottom_menu_diary_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


}