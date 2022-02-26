package com.example.myapplication.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentBottomMenuBinding
import com.example.myapplication.ui.home.SpecificDiaryFragmentDirections
import com.example.myapplication.util.DIARIES_TYPE_FOLLOWING
import com.example.myapplication.util.DIARIES_TYPE_TOPIC
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.transition.MaterialFadeThrough


class BottomMenuFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomMenuBinding.inflate(inflater, container, false)
        subscribeUi()
        return binding.root
    }

    private fun subscribeUi() {
        val radius = resources.getDimension(R.dimen.bottom_menu_radius)
        val model = ShapeAppearanceModel().toBuilder()
            .setTopRightCornerSize(radius)
            .setTopLeftCornerSize(radius)
            .build()

        binding.navigationView.background = MaterialShapeDrawable(model)
            .apply {
                fillColor = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.secondaryColor
                )
            }

        binding.navigationView.setNavigationItemSelectedListener {
            (activity as MainActivity).currentNavigationFragment?.exitTransition =
                MaterialFadeThrough().apply {
                    duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
                }

            when (it.itemId) {
                R.id.latest_diaries -> {
                    findNavController().navigate(R.id.action_global_homeFragment)
                }
                R.id.following_diaries -> {
                    val directions =
                        SpecificDiaryFragmentDirections.actionGlobalSpecificdiariesFragment(
                            DIARIES_TYPE_FOLLOWING, DIARIES_TYPE_FOLLOWING
                        )
                    findNavController().navigate(directions)
                }
                R.id.topic_diaries -> {
                    val directions =
                        SpecificDiaryFragmentDirections.actionGlobalSpecificdiariesFragment(
                            DIARIES_TYPE_TOPIC, DIARIES_TYPE_TOPIC
                        )
                    findNavController().navigate(directions)
                }
            }
            dismissAllowingStateLoss()
            return@setNavigationItemSelectedListener true
        }

    }

}