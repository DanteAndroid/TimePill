package com.example.myapplication.ui.edit

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.RadioGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.Fade
import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.myapplication.R
import com.example.myapplication.data.model.Notebook
import com.example.myapplication.databinding.FragmentEditNotebookBinding
import com.example.myapplication.util.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.Calendar


@AndroidEntryPoint
class EditNotebookFragment : Fragment() {

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 1

        const val PRIVACY_PRIVATE = 1
        const val PRIVACY_PUBLIC = 10
    }

    private var _binding: FragmentEditNotebookBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditViewModel by viewModels()
    private val args by navArgs<EditNotebookFragmentArgs>()
    private lateinit var notebook: Notebook

    private var isEditMode = false
    private var notebookChanged = false

    private var notebookSubject: String = ""
    private var expireDate: String = ""
    private var description: String? = null
    private var isPrivate: Boolean = false

    private var coverFile: File? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditNotebookBinding.inflate(inflater, container, false)
        subscribeUi()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enterTransition = Fade()
//        enterTransition = MaterialContainerTransform().apply {
//            startView = requireActivity().findViewById(R.id.fab)
//            endView = binding.root
//            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
//            scrimColor = Color.TRANSPARENT
//            containerColor = requireContext().themeColor(R.attr.colorSurface)
//            startContainerColor = requireContext().themeColor(R.attr.colorSecondary)
//            endContainerColor = requireContext().themeColor(R.attr.colorSurface)
//        }
//        returnTransition = androidx.transition.Slide().apply {
//            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
////                addTarget(R.id.email_card_view)
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_IMAGE) {
                data?.let {
                    binding.notebookCover.setImageURI(it.data)
                    coverFile = AppUtils.fetchImage(requireContext(), it)
                }
            }
        }
    }

    private fun subscribeUi() {
        args.notebook?.let {
            notebook = it
            isEditMode = true
            notebookSubject = notebook.subject
            description = notebook.description
            expireDate = notebook.expired
        }

        binding.run {
            close.setOnClickListener {
                KeyboardUtils.hideSoftInput(subject)
                findNavController().popBackStack()
            }
            notebookCover.setOnClickListener {
                AppUtils.pickImage(this@EditNotebookFragment, REQUEST_CODE_PICK_IMAGE)
            }
            title.setText(if (isEditMode) R.string.edit_notebook else R.string.create_notebook)
            send.isEnabled = false
            send.setOnClickListener {
                send()
            }
            privacy.setOnCheckedChangeListener { buttonView, isChecked ->
                isPrivate = isChecked.not()
            }
            subject.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    val result = s.toString().trim()
                    notebookSubject = result
                    if (result.isEmpty()) {
                        subjectWrapper.error = getString(R.string.subject_cant_be_empty)
                    } else if (result.length > 20) {
                        subjectWrapper.error = getString(R.string.subject_is_long)
                    } else {
                        notifyNotebookChanged()
                        subjectWrapper.error = null
                    }
                }
            })
            desc.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // nothing
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    notifyNotebookChanged()
                    description = s.toString().trim()
                }
            })
        }
        if (isEditMode) {
            binding.privacy.isChecked = notebook.isPublic
            binding.subject.let {
                it.setText(notebook.subject)
                it.setSelection(notebook.subject.length)
            }
            binding.desc.let {
                it.setText(notebook.description)
                notebook.description?.let { desc ->
                    it.setSelection(desc.length)
                }
            }
            binding.expireTimeGroup.isVisible = false
            binding.noteBookExpireTime.text = notebook.expired
        } else {
            initExpireTimeGroup()
        }
    }

    private fun send() {
        if (notebookSubject.isEmpty()) {
            ToastUtils.showShort(R.string.subject_cant_be_empty)
            return
        }
        val data = hashMapOf<String, Any>().also {
            it[SUBJECT] = notebookSubject
            description?.let { desc ->
                it[DESCRIPTION] = desc
            }
            it[PRIVACY] =
                if (isPrivate) PRIVACY_PRIVATE else PRIVACY_PUBLIC
            it[EXPIRED] = expireDate
        }

        if (isEditMode) {
            viewModel.updateNotebook(notebook.id, data, coverFile).observe(viewLifecycleOwner, {
                Snackbar.make(
                    binding.root,
                    getString(R.string.notebook_update_success),
                    Snackbar.LENGTH_SHORT
                )
                    .setAnchorView(getFab())
                    .show()
                findNavController().popBackStack()
            })

        } else {
            viewModel.createNotebook(data, coverFile).observe(viewLifecycleOwner, {
                Snackbar.make(
                    binding.root,
                    getString(R.string.create_notebook_success, notebookSubject),
                    Snackbar.LENGTH_SHORT
                )
                    .setAnchorView(getFab())
                    .show()
                findNavController().popBackStack()
            })
        }

    }

    private fun initExpireTimeGroup() {
        binding.run {
            expireTimeGroup.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
                when (checkedId) {
                    R.id.month -> expireDate =
                        TimeUtils.date2String(
                            TimeUtils.getDateByNow(30, TimeConstants.DAY),
                            CommonConfig.DATE_FORMAT_DAY
                        )
                    R.id.halfYear -> expireDate =
                        TimeUtils.date2String(
                            TimeUtils.getDateByNow(6 * 30, TimeConstants.DAY),
                            CommonConfig.DATE_FORMAT_DAY
                        )
                    R.id.aYear -> expireDate =
                        TimeUtils.date2String(
                            TimeUtils.getDateByNow(12 * 30, TimeConstants.DAY),
                            CommonConfig.DATE_FORMAT_DAY
                        )
                    R.id.custom -> showPicker()
                    else -> expireDate =
                        TimeUtils.date2String(
                            TimeUtils.getDateByNow(30, TimeConstants.DAY),
                            CommonConfig.DATE_FORMAT_DAY
                        )
                }
                noteBookExpireTime.text = expireDate
            }
            expireTimeGroup.check(R.id.month)

        }
    }

    private fun showPicker() {
        val calendar = Calendar.getInstance().also {
            it.time = TimeUtils.getDateByNow(30, TimeConstants.DAY)
        }
        val pickerDialog = DatePickerDialog(
            requireContext(), { view: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                expireDate = year.toString() + "-" + (month + 1) + "-" + dayOfMonth
                binding.noteBookExpireTime.text = expireDate
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
        pickerDialog.show()
    }

    private fun notifyNotebookChanged() {
        notebookChanged = true
        binding.send.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}