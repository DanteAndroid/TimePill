package com.example.myapplication.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.Utils
import com.example.myapplication.R
import com.example.myapplication.data.model.User
import com.example.myapplication.databinding.LoginFragmentBinding
import com.example.myapplication.net.Resource
import com.example.myapplication.net.Status
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.util.DataStoreUtil
import com.example.myapplication.util.getFab
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()
    private var _binding: LoginFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginFragmentBinding.inflate(inflater, container, false)
        subscribeUi()
        return binding.root
    }

    private var resource: Resource<User>? = null

    private fun subscribeUi() {
        viewModel.login.observe(viewLifecycleOwner, {
            if (it == resource) return@observe
            resource = it
            when (it.status) {
                Status.LOADING -> {
                    Snackbar.make(binding.root, R.string.loading, Snackbar.LENGTH_SHORT).show()
                }
                Status.SUCCESS -> {
                    loginSuccess()
                }
                Status.ERROR -> {
                    Snackbar.make(
                        binding.root,
                        it.message ?: getString(R.string.login_failed),
                        Snackbar.LENGTH_SHORT
                    )
                        .setAnchorView(getFab())
                        .show()
                }
            }
        })
        binding.login.setOnClickListener {
            val account = binding.account.text.toString()
            val psw = binding.psw.text.toString()
            val name: String? = binding.name.text?.toString()
            if (binding.login.text == getString(R.string.login)) {
                // Login
                if (!checkValid(account, psw)) return@setOnClickListener
                viewModel.login(account, psw)
            } else {
                // Register
                if (!checkValid(account, psw, name)) return@setOnClickListener
                viewModel.register(account, psw, name!!)
            }
        }
    }

    private fun loginSuccess() {
        startActivity(Intent(Utils.getApp(), MainActivity::class.java))
        activity?.supportFinishAfterTransition()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (DataStoreUtil.isLogin()) {
            loginSuccess()
        }
    }

    private fun checkValid(account: String?, psw: String?, name: String? = "UserName"): Boolean {
        if (account.isNullOrBlank()) {
            binding.accountWrapper.error = getString(R.string.name_or_psw_is_empty)
            return false
        }
        if (psw.isNullOrBlank()) {
            binding.pswWrapper.error = getString(R.string.name_or_psw_is_empty)
            return false
        }
        if (name.isNullOrBlank()) {
            binding.nameWrapper.error = getString(R.string.nickname_too_short)
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}