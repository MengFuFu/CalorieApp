package com.mh.calorie.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mh.calorie.R
import com.mh.calorie.data.temp.AuthError
import com.mh.calorie.data.temp.TempAuthRepository
import com.mh.calorie.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** 登录页 */
@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    @Inject
    lateinit var authRepo: TempAuthRepository

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentLoginBinding.bind(view)

        binding.btnClose.setOnClickListener { findNavController().navigateUp() }

        binding.tvGoRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnLogin.setOnClickListener { doLogin() }

        // 注册页回来会带账号，直接填好、光标落到密码
        parentFragmentManager.setFragmentResultListener(
            RegisterFragment.REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            bundle.getString(RegisterFragment.RESULT_ACCOUNT)?.let { account ->
                binding.etAccount.setText(account)
                binding.etPassword.setText("")
                binding.etPassword.requestFocus()
            }
        }
    }

    private fun doLogin() {
        val account = binding.etAccount.text.toString()
        val password = binding.etPassword.text.toString()

        val error = authRepo.login(account, password)
        if (error != null) {
            toast(error.messageRes)
            return
        }
        toast(R.string.auth_login_ok)
        // 登录页是盖在首页上的，navigateUp 就回到首页
        findNavController().navigateUp()
    }

    private fun toast(resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/** 错误类型 → 文案（两个页面共用，所以放文件顶层） */
val AuthError.messageRes: Int
    get() = when (this) {
        AuthError.EMPTY_ACCOUNT -> R.string.auth_err_empty_account
        AuthError.EMPTY_PASSWORD -> R.string.auth_err_empty_password
        AuthError.TOO_SHORT -> R.string.auth_err_short
        AuthError.ACCOUNT_EXISTS -> R.string.auth_err_account_exists
        AuthError.ACCOUNT_NOT_FOUND -> R.string.auth_err_account_not_found
        AuthError.WRONG_PASSWORD -> R.string.auth_err_wrong_password
        AuthError.CONFIRM_MISMATCH -> R.string.auth_err_confirm
    }