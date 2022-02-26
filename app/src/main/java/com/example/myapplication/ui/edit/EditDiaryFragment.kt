package com.example.myapplication.ui.edit

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import com.blankj.utilcode.util.KeyboardUtils
import com.example.myapplication.R
import com.example.myapplication.data.model.Diary
import com.example.myapplication.data.model.Notebook
import com.example.myapplication.databinding.FragmentEditDiaryBinding
import com.example.myapplication.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class EditDiaryFragment : Fragment() {

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 1

        fun newInstance(): EditDiaryFragment {
            val args = Bundle().apply {

            }
            val fragment = EditDiaryFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var isTopic: Boolean = false
    private var _binding: FragmentEditDiaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditViewModel by viewModels()

    private val args by navArgs<EditDiaryFragmentArgs>()
    private lateinit var diary: Diary

    private var shouldShowNoNotebook: Boolean = false
    private var photoFile: File? = null

    private var isEditMode: Boolean = false
    private var notebookId: Int = 0
    private var diaryContent: String? = null

    private val userId = DataStoreUtil.getMyId()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditDiaryBinding.inflate(inflater, container, false)
        subscribeUi()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enterTransition = MaterialContainerTransform().apply {
            startView = requireActivity().findViewById(R.id.fab)
            endView = binding.root
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
            scrimColor = Color.TRANSPARENT
            containerColor = requireContext().themeColor(R.attr.colorSurface)
            startContainerColor = requireContext().themeColor(R.attr.colorSecondary)
            endContainerColor = requireContext().themeColor(R.attr.colorSurface)
        }
        returnTransition = androidx.transition.Slide().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
//                addTarget(R.id.email_card_view)
        }
        checkShowNoNotebook()
    }

    private fun subscribeUi() {
        args.diary?.let {
            diary = it
            isEditMode = true
        }

        viewModel.fetchNotebooks(userId).observe(viewLifecycleOwner, {
            initNotebooks(it)
        })
        binding.run {
            close.setOnClickListener {
                KeyboardUtils.hideSoftInput(content)
                findNavController().navigateUp()
            }
            content.addTextChangedListener(afterTextChanged = {
                diaryContent = it?.toString()?.trim()
                send.isEnabled = !diaryContent.isNullOrEmpty()
            })
            send.isEnabled = false
            send.setOnClickListener {
                send()
            }
            photo.setOnClickListener {
                AppUtils.pickImage(
                    this@EditDiaryFragment,
                    REQUEST_CODE_PICK_IMAGE
                )
            }
            if (isEditMode) {
                val diary: Diary = args.diary!!
                content.setText(diary.content)
                content.setSelection(content.text.length)
            }
        }
    }

    private fun initNotebooks(notebooks: List<Notebook>?) {
        val validSubjects = arrayListOf<Notebook>()
        if (notebooks.isNullOrEmpty()) {
            shouldShowNoNotebook = true
        } else {
            for (n in notebooks) {
                if (!n.isExpired) {
                    validSubjects.add(n)
                }
            }
            if (validSubjects.isEmpty()) {
                KeyboardUtils.hideSoftInput(requireActivity())
                shouldShowNoNotebook = true
            } else {
                shouldShowNoNotebook = false
            }
        }
        if (isEditMode) {
            val adapter = ArrayAdapter(
                requireContext(),
                R.layout.spinner_layout,
                arrayListOf(diary.notebookSubject)
            )
            adapter.setDropDownViewResource(R.layout.spinner_subject_dropdown_item)
            binding.spinner.adapter = adapter
            KeyboardUtils.showSoftInput(binding.content)
        } else {
            initSpinner(validSubjects)
        }
    }

    private fun checkShowNoNotebook() {
        if (enterTransition is Transition) {
            (enterTransition as Transition).addListener(object : TransitionListenerAdapter() {
                override fun onTransitionEnd(transition: Transition) {
                    super.onTransitionEnd(transition)
                    if (shouldShowNoNotebook) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.no_notebook)
                            .setMessage(R.string.no_valid_notebook)
                            .setPositiveButton(R.string.create_notebook) { _, _ ->
                                findNavController().navigate(R.id.action_global_editNotebookFragment)
                            }
                            .setNegativeButton(R.string.deal_later, null)
                            .show()
                    }
                }
            })
        } else {
            if (shouldShowNoNotebook) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.no_notebook)
                    .setMessage(R.string.no_valid_notebook)
                    .setPositiveButton(R.string.create_notebook) { _, _ ->
                        findNavController().navigate(R.id.action_global_editNotebookFragment)
                    }
                    .setNegativeButton(R.string.deal_later, null)
                    .show()
            }
        }
    }


    private fun initSpinner(validNotebooks: List<Notebook>) {
        val subjects = arrayListOf<String>()
        validNotebooks.forEach {
            subjects.add(it.subject)
        }
        if (validNotebooks.isEmpty()) {
            subjects.add(getString(R.string.choose_notebook_hint))
        }
        subjects.add(getString(R.string.create_notebook))
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_layout, subjects)
        adapter.setDropDownViewResource(R.layout.spinner_subject_dropdown_item)
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (subjects[position] == getString(R.string.create_notebook)) {
                    findNavController().navigate(R.id.action_global_editNotebookFragment)
                } else if (subjects[position] == getString(R.string.choose_notebook_hint)) {
                    // nothing
                } else {
                    notebookId = validNotebooks[position].id
//                    KeyboardUtils.showSoftInput(binding.content)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Snackbar.make(
                    binding.spinner,
                    R.string.choose_notebook_hint,
                    Snackbar.LENGTH_SHORT
                )
                    .setAnchorView(getFab())
                    .show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_IMAGE) {
                data?.let {
                    binding.attachPhoto.isVisible = true
                    binding.attachPhoto.setImageURI(it.data)
                    photoFile = AppUtils.fetchImage(requireContext(), it)
                }
            }
        }
    }

    private fun send() {
        if (diaryContent.isNullOrEmpty()) return
        if (diaryContent!!.length < CommonConfig.DIARY_TEXT_LIMIT) {
            Snackbar.make(
                binding.spinner,
                R.string.say_more,
                Snackbar.LENGTH_SHORT
            )
                .setAnchorView(getFab())
                .show()
            return
        }

        if (isEditMode) {
            viewModel.updateDiary(
                diary.id,
                diaryContent!!,
                diary.notebookId
            ).observe(viewLifecycleOwner, {
                KeyboardUtils.hideSoftInput(binding.content)
                Snackbar.make(
                    binding.spinner,
                    R.string.diary_update_success,
                    Snackbar.LENGTH_SHORT
                )
                    .setAnchorView(getFab())
                    .show()

                findNavController().popBackStack()
            })
        } else {
            viewModel.createDiary(
                notebookId,
                diaryContent!!,
                isTopic,
                photoFile
            ).observe(viewLifecycleOwner, {
                KeyboardUtils.hideSoftInput(binding.content)
                Snackbar.make(
                    binding.spinner,
                    R.string.create_diary_success,
                    Snackbar.LENGTH_SHORT
                )
                    .setAnchorView(getFab())
                    .show()

                findNavController().popBackStack()
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}