package com.example.myapplication.ui.me

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.model.User
import com.example.myapplication.databinding.FragmentMeBinding
import com.example.myapplication.net.Status
import com.example.myapplication.ui.follow.UsersViewModel
import com.example.myapplication.ui.home.SpecificDiaryFragment
import com.example.myapplication.util.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.BlurTransformation

@AndroidEntryPoint
class MeFragment : Fragment() {

    private val viewModel: UsersViewModel by viewModels()
    private var _binding: FragmentMeBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<MeFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (args.userId > 0) {
            sharedElementEnterTransition = MaterialContainerTransform().apply {
                drawingViewId = R.id.nav_host_fragment
                duration = resources.getInteger(R.integer.motion_duration_large).toLong()
//                scrimColor = Color.TRANSPARENT
                setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
            }

        } else {
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
        _binding = FragmentMeBinding.inflate(inflater, container, false)
        postponeEnterTransition()
        subscribeUi()
        return binding.root
    }

    private var userId: Int = 0

    @SuppressLint("ShowToast")
    private fun subscribeUi() {
        binding.run {
            val titles = resources.getStringArray(R.array.me_title)
            userId = if (args.userId <= 0) DataStoreUtil.getMyId() else args.userId
            binding.pager.adapter = MeFragmentAdapter(this@MeFragment, userId)
            TabLayoutMediator(tabLayout, binding.pager) { tab, position ->
                tab.text = titles[position]
            }.attach()

            if (userId.isMyId()) {
                userLayout.follow.isVisible = false
            }
        }
        viewModel.fetchUser(userId).observe(viewLifecycleOwner, { it ->
            when (it.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    it.data?.let { _user ->
                        populateUi(_user)
                    }
                }
                Status.ERROR -> {
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

    private fun updateFollow(hasFollow: Boolean) {
        val drawableStart =
            if (hasFollow) R.drawable.ic_baseline_check_24 else R.drawable.ic_baseline_add_24
        binding.userLayout.follow.let {
            it.setOnClickListener {
                if (hasFollow) {
                    viewModel.cancelFollow(userId)
                    updateFollow(false)
                    ToastUtils.showShort(R.string.cancel_follow_success)

                } else {
                    viewModel.follow(userId).observe(viewLifecycleOwner, { followed ->
                        if (followed) {
                            updateFollow(true)
                            ToastUtils.showShort(R.string.follow_success)
                        } else ToastUtils.showShort(R.string.follow_failed)
                    })
                }
            }
            it.setCompoundDrawablesRelativeWithIntrinsicBounds(
                drawableStart,
                0,
                0,
                0
            )
            it.setText(if (hasFollow) R.string.title_followed else R.string.title_follow)
        }
    }

    private fun populateUi(user: User) {
        viewModel.hasFollow(userId).observe(viewLifecycleOwner, { hasFollow ->
            updateFollow(hasFollow)
        })
        binding.userLayout.run {
            avatar.setOnClickListener {
                // view avatar
            }
            name.text = user.name
            Glide.with(this@MeFragment).load(user.avatarUrl)
                .transform(BlurTransformation(120, 4))
                .into(background)
            Glide.with(this@MeFragment).load(user.avatarUrl)
                .circleCrop()
                .into(avatar)
            created.text = getString(
                R.string.enter_timepill,
                TimeUtils.getFriendlyTimeSpanByNow(user.created)
            )
            intro.text = user.intro
        }
    }

    class MeFragmentAdapter(fragment: Fragment, private val userId: Int) :
        FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            if (position == 0) {
                return SpecificDiaryFragment.newInstance(DIARIES_TYPE_HOME, userId)
            } else {
                return NotebookFragment.newInstance(userId)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}