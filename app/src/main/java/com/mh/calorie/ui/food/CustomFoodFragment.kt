package com.mh.calorie.ui.food

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mh.calorie.R
import com.mh.calorie.core.domain.model.CustomFood
import com.mh.calorie.data.temp.TempCustomFoodRepository
import com.mh.calorie.databinding.FragmentCustomFoodBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** 添加自定义食物：名称/数量/单位/热量必填，三大营养素选填 */
@AndroidEntryPoint
class CustomFoodFragment : Fragment(R.layout.fragment_custom_food) {

    @Inject
    lateinit var customRepo: TempCustomFoodRepository

    private var _binding: FragmentCustomFoodBinding? = null
    private val binding get() = _binding!!

    private var photoUri: String? = null

    private val pickPhoto = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if(uri == null) return@registerForActivityResult
        photoUri = uri.toString()
        binding.ivPhoto.setPadding(0, 0, 0, 0)
        binding.ivPhoto.setImageURI(uri)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentCustomFoodBinding.bind(view)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnPhoto.setOnClickListener {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val name = binding.etName.text.toString().trim()
        val unit = binding.etUnit.text.toString().trim()
        val qty = binding.etQty.text.toString().toFloatOrNull()
        val kcal = binding.etKcal.text.toString().toFloatOrNull()

        // 四项必填，缺一个就提示（与设计稿上的红色 * 对应）
        if (name.isEmpty() || unit.isEmpty() || qty == null || qty <= 0f || kcal == null || kcal <= 0f) {
            toast(R.string.custom_food_required)
            return
        }

        val food = CustomFood(
            name = name,
            qty = qty,
            unit = unit,
            kcal = kcal,
            protein = binding.etProtein.text.toString().toFloatOrNull() ?: 0f,
            fat = binding.etFat.text.toString().toFloatOrNull() ?: 0f,
            carb = binding.etCarb.text.toString().toFloatOrNull() ?: 0f,
            photoUri = photoUri,
            favorite = binding.switchFavorite.isChecked
        )

        customRepo.add(food)
        toast(R.string.custom_food_saved)
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