package com.mh.calorie.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mh.calorie.R
import com.mh.calorie.data.temp.TempAuthRepository
import com.mh.calorie.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** 「我的」页：账号信息 + 四个功能分组，除退出登录外都先弹提示 */
@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    @Inject
    lateinit var authRepo: TempAuthRepository

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentProfileBinding.bind(view)

        renderUser()
        bindRows()

        binding.btnCopyId.setOnClickListener { copyId() }
        binding.rowLogout.setOnClickListener {
            authRepo.logout()
            Toast.makeText(requireContext(), R.string.profile_logged_out, Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    private fun renderUser() {
        val account = authRepo.currentUser.value
        val name = if (account.isNullOrBlank()) {
            getString(R.string.profile_guest)
        } else {
            getString(R.string.profile_user_name, account)
        }
        binding.tvUserName.text = name
        binding.tvUserId.text =
            getString(R.string.profile_id_format, account ?: getString(R.string.profile_guest))
    }

    private fun bindRows() {
        // 这些页还没做，先统一提示
        listOf(
            binding.rowMall, binding.rowFriends, binding.rowFridge, binding.rowLock,
            binding.rowAlarm, binding.rowCanIEat, binding.rowFoodPk, binding.rowScan,
            binding.rowRecipe, binding.rowWidget, binding.rowTheme, binding.rowNotice,
            binding.rowSettings, binding.rowChallenge, binding.rowEditProfile,
            binding.rowBind, binding.rowEmergency, binding.rowMinor,
            binding.rowSurvey, binding.rowFeedback, binding.rowService, binding.rowAbout
        ).forEach { row ->
            row.setOnClickListener { toastComingSoon() }
        }
    }

    private fun copyId() {
        val id = authRepo.currentUser.value.orEmpty()
        if (id.isEmpty()) return
        requireContext().getSystemService<ClipboardManager>()
            ?.setPrimaryClip(ClipData.newPlainText("id", id))
        Toast.makeText(requireContext(), R.string.profile_id_copied, Toast.LENGTH_SHORT).show()
    }

    private fun toastComingSoon() {
        Toast.makeText(requireContext(), R.string.profile_coming_soon, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}