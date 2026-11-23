package com.mh.calorie.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mh.calorie.R
import com.mh.calorie.data.temp.TempAuthRepository
import com.mh.calorie.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** 注册页：账号 + 密码 + 确认密码 */
@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    @Inject
    lateinit var authRepo: TempAuthRepository

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentRegisterBinding.bind(view)

        binding.btnClose.setOnClickListener { findNavController().navigateUp() }
        binding.tvGoLogin.setOnClickListener { findNavController().navigateUp() }
        binding.btnRegister.setOnClickListener { doRegister() }
    }

    private fun doRegister() {
        val account = binding.etAccount.text.toString()
        val password = binding.etPassword.text.toString()
        val confirm = binding.etConfirm.text.toString()

        val error = authRepo.register(account, password, confirm)
        if (error != null) {
            Toast.makeText(requireContext(), error.messageRes, Toast.LENGTH_SHORT).show()
            return
        }

        // 把账号回传给登录页，省得再手输一遍
        parentFragmentManager.setFragmentResult(
            REQUEST_KEY,
            bundleOf(RESULT_ACCOUNT to account.trim())
        )
        Toast.makeText(requireContext(), R.string.auth_register_ok, Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQUEST_KEY = "auth_register_request"
        const val RESULT_ACCOUNT = "auth_register_account"
    }
}