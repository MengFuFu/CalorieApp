package com.mh.calorie.ui.sport

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mh.calorie.R
import com.mh.calorie.core.domain.model.CustomSport
import com.mh.calorie.data.temp.TempCustomSportRepository
import com.mh.calorie.databinding.FragmentCustomSportBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** 添加自定义运动：名称 / 时长 / 热量 / 照片，四个字段一页表单 */
@AndroidEntryPoint
class CustomSportFragment : Fragment(R.layout.fragment_custom_sport) {

    private var _binding: FragmentCustomSportBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var customRepo: TempCustomSportRepository

    private var photoUri: Uri? = null

    // 系统相册选择器（Android 13+ 用照片选择器，低版本自动回退）
    private val pickPhoto = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        photoUri = uri
        binding.ivPhoto.setImageURI(uri)
        binding.ivPhoto.isVisible = true
        binding.ivCamera.isVisible = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentCustomSportBinding.bind(view)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.boxPhoto.setOnClickListener {
            pickPhoto.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val name = binding.etName.text.toString().trim()
        val minutes = binding.etDuration.text.toString().trim().toIntOrNull()
        val kcal = binding.etKcal.text.toString().trim().toFloatOrNull()

        when {
            name.isEmpty() -> return toast(R.string.custom_sport_err_name)
            minutes == null || minutes !in MIN_MINUTES..MAX_MINUTES ->
                return toast(R.string.custom_sport_err_range)
            kcal == null || kcal !in MIN_KCAL..MAX_KCAL ->
                return toast(R.string.custom_sport_err_range)
        }

        customRepo.add(CustomSport(name, minutes!!, kcal!!, photoUri?.toString()))
        toast(R.string.custom_sport_added)
        findNavController().navigateUp()
    }

    private fun toast(resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val MIN_MINUTES = 1
        const val MAX_MINUTES = 600
        const val MIN_KCAL = 1f
        const val MAX_KCAL = 5000f
    }
}