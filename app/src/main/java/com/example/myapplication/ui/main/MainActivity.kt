package com.example.myapplication.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainCollapseBinding
import com.example.myapplication.ui.detail.DetailPagerFragment
import com.example.myapplication.ui.detail.DiaryDetailFragment
import com.example.myapplication.ui.edit.EditNotebookFragmentDirections
import com.example.myapplication.ui.home.SpecificDiaryFragmentDirections
import com.example.myapplication.ui.me.MeFragmentDirections
import com.example.myapplication.util.DIARIES_TYPE_FOLLOWING
import com.example.myapplication.util.DIARIES_TYPE_TOPIC
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainCollapseBinding
    private lateinit var navController: NavController

    private val viewModel: MainViewModel by viewModels()
    private val bottomMenuFragment: BottomMenuFragment = BottomMenuFragment()

    val currentNavigationFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.childFragmentManager
            ?.fragments
            ?.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainCollapseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initController()
        subscribeUi()
    }

    private fun subscribeUi() {
        binding.collapseContent.setOnClickListener {
            val directions =
                SpecificDiaryFragmentDirections.actionGlobalSpecificdiariesFragment(
                    DIARIES_TYPE_TOPIC, 0
                )
            navController.navigate(directions)
        }
        viewModel.topic.observe(this, {
//            binding.collapseContent.isVisible = true
            Glide.with(this).load(it.imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop().into(binding.topicImage)
        })
        viewModel.fetchTopic()
    }

    private fun initController() {
        navController = findNavController(R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            when (destination.id) {
                R.id.navigation_home -> {
//                    binding.collapseContent.isVisible = viewModel.topic.value != null
                    binding.bottomAppBarContentContainer.isVisible = true
//                    getBottomAppBar().setFabAlignmentModeAndReplaceMenu(
//                        BottomAppBar.FAB_ALIGNMENT_MODE_CENTER,
//                        R.menu.bottom_nav_menu
//                    )
                    switchAlignMode(false)
                    showBottomBar(true)
                    binding.fab.let {
                        it.setImageResource(R.drawable.ic_baseline_edit_24)
                        it.setOnClickListener {
                            navigateToCompose()
                        }
                    }
                }
                R.id.navigation_me -> {
                    arguments?.let {
                        if (it.getInt("userId") == 0) {
                            switchAlignMode()
                            showBottomBar(true)
                        } else {
                            showBottomBar(false)
                        }
                    }
                    binding.fab.let {
                        it.setImageResource(R.drawable.ic_baseline_edit_24)
                        it.setOnClickListener {
                            navigateToCompose()
                        }
                    }
                }
                R.id.detailPagerFragment -> {
                    binding.collapseContent.isVisible = false
                    binding.bottomAppBarContentContainer.isVisible = false
//                    getBottomAppBar().setFabAlignmentModeAndReplaceMenu(
//                        BottomAppBar.FAB_ALIGNMENT_MODE_END,
//                        R.menu.bottom_menu_diary_detail
//                    )
                    switchAlignMode(true)
                    showBottomBar(false)
                    binding.fab.let {
                        it.setImageResource(R.drawable.fab_comment)
                        it.setOnClickListener {
                            currentDiary()?.commentDiary()
                        }
                    }
                }
                R.id.navigation_specific_diaries -> {
                    switchAlignMode()
                    showBottomBar(true)
                    arguments?.let {
                        val type = it.getInt("diaryType")
                        if (type == DIARIES_TYPE_FOLLOWING) {
                            binding.bottomAppBarTitle.setText(R.string.following_diary)
                        } else if (type == DIARIES_TYPE_TOPIC) {
                            binding.collapseContent.isVisible = false
                            binding.bottomAppBarTitle.setText(R.string.topic_diaries)
                            TransitionManager.beginDelayedTransition(binding.root)
                        }
                    }
                }
                R.id.editNotebookFragment -> {
                    showBottomBar(false)
                }
                R.id.editDiaryFragment -> {
                    showBottomBar(false)
                }
                else -> {
                    switchAlignMode()
                    showBottomBar(true)
                }
            }

        }

        binding.run {
            setSupportActionBar(bottomAppBar)
            bottomAppBar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.navigation_follow -> {
                        navController.navigate(R.id.action_global_followFragment)
                    }
                    R.id.navigation_me -> {
                        val action = MeFragmentDirections.actionGlobalMeFragment(0)
                        navController.navigate(action)
                    }
                    R.id.share_diary -> {
                        currentDiary()?.shareDiary()
                    }
                    R.id.report_diary -> {
                        currentDiary()?.reportDiary()
                    }
                }
                return@setOnMenuItemClickListener true
            }
            bottomAppBarContentContainer.setOnClickListener {
                val current = findNavController(R.id.nav_host_fragment).currentDestination?.id
                if (current == R.id.navigation_home || current == R.id.navigation_specific_diaries) {
                    // 打开 bottomSheet
                    bottomMenuFragment.show(supportFragmentManager, null)
                } else {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_global_homeFragment)
                }
            }

            fab.setOnClickListener {
                navigateToCompose()
            }
        }
    }

    private fun currentDiary(): DiaryDetailFragment? {
        val detail =
            (currentNavigationFragment as? DetailPagerFragment)?.currentFragment() as? DiaryDetailFragment
        return detail
    }

    private fun showBottomBar(visible: Boolean) {
        if (visible) {
            binding.bottomAppBar.let {
                if (it.translationY != 0f) it.performShow()
            }
            binding.fab.let {
                if (!it.isVisible) {
                    it.show()
                }
            }
        } else {
            binding.bottomAppBar.let {
                if (it.translationY == 0f) it.performHide()
            }
            binding.fab.hide()
        }
    }

    private fun navigateToCompose() {
        currentNavigationFragment?.apply {
            exitTransition = MaterialElevationScale(false).apply {
                duration = resources.getInteger(R.integer.motion_duration_large).toLong()
            }
            reenterTransition = MaterialElevationScale(true).apply {
                duration = resources.getInteger(R.integer.motion_duration_large).toLong()
            }
        }
        val directions = EditNotebookFragmentDirections.actionGlobalEditDiaryFragment(null)
        findNavController(R.id.nav_host_fragment).navigate(directions)
    }

    private fun switchAlignMode(isEnd: Boolean = false) {
        binding.run {
//            bottomAppBarContentContainer.isVisible = !isEnd
//            bottomAppBar.post {
            bottomAppBar.fabAlignmentMode =
                if (isEnd) BottomAppBar.FAB_ALIGNMENT_MODE_END else BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
//            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_follow -> {
                navController.navigate(R.id.action_global_followFragment)
            }
            R.id.navigation_me -> {
                val action = MeFragmentDirections.actionGlobalMeFragment(0)
                navController.navigate(action)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bottom_nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun getBottomAppBar(): BottomAppBar = binding.bottomAppBar
    fun getFab(): FloatingActionButton = binding.fab

}