package com.example.englishbytext.Classes.Objects

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import com.example.englishbytext.Classes.schemas.FilterData
import com.example.englishbytext.Dialogs.MyDialogBuilder
import com.example.englishbytext.R

class D_filter_word_list(
    context : Context,
    private val filterData: FilterData,
    private val event : () -> Unit
) : MyDialogBuilder(context, R.layout.d_filter_word_list) {

    private lateinit var acceptView : ImageView
    private lateinit var dismissView : ImageView
    private lateinit var filtersContainer : LinearLayout

    override fun initView(builderView: View) {
        acceptView = builderView.findViewById(R.id.d_add)
        dismissView = builderView.findViewById(R.id.d_dismiss)
        filtersContainer = builderView.findViewById(R.id.filtersContainer)

        acceptView.setOnClickListener {
            acceptClick()
        }
        dismissView.setOnClickListener {
            dismissClick()
        }

        setValues()
    }


    private fun dismissClick() {
        dismiss()
    }

    private fun acceptClick() {
        getValues()
        event()
        dismiss()
    }

    private fun getItemAt(itemIndex : Int, radioItem : Int) : RadioButton{
        val item = filtersContainer.getChildAt(itemIndex) as LinearLayout
        val group = item.getChildAt(1) as RadioGroup
        return group.getChildAt(radioItem) as RadioButton
    }

    private fun getValues(){
        for(i in 0..2){
            if(getItemAt(0, i).isChecked) filterData.isFavorite = FilterData.getOptionType(i)
            if(getItemAt(1, i).isChecked) filterData.isMastered = FilterData.getOptionType(i)
            if(getItemAt(2, i).isChecked) filterData.hasDefinition = FilterData.getOptionType(i)
            if(getItemAt(3, i).isChecked) filterData.hasExample = FilterData.getOptionType(i)
            if(getItemAt(4, i).isChecked) filterData.hasImage = FilterData.getOptionType(i)
            if(getItemAt(5, i).isChecked) filterData.hasAudio = FilterData.getOptionType(i)
            if(getItemAt(6, i).isChecked) filterData.hasTag = FilterData.getOptionType(i)
            if(getItemAt(7, i).isChecked) filterData.hasFolder = FilterData.getOptionType(i)
            if(getItemAt(8, i).isChecked) filterData.hasText = FilterData.getOptionType(i)
        }


    }

    private fun setValues(){
        for(i in 0..2){
            if(filterData.isFavorite == FilterData.getOptionType(i)) getItemAt(0, i).isChecked = true
            if(filterData.isMastered == FilterData.getOptionType(i)) getItemAt(1, i).isChecked = true
            if(filterData.hasDefinition == FilterData.getOptionType(i)) getItemAt(2, i).isChecked = true
            if(filterData.hasExample == FilterData.getOptionType(i)) getItemAt(3, i).isChecked = true
            if(filterData.hasImage == FilterData.getOptionType(i)) getItemAt(4, i).isChecked = true
            if(filterData.hasAudio == FilterData.getOptionType(i)) getItemAt(5, i).isChecked = true
            if(filterData.hasTag == FilterData.getOptionType(i)) getItemAt(6, i).isChecked = true
            if(filterData.hasFolder == FilterData.getOptionType(i)) getItemAt(7, i).isChecked = true
            if(filterData.hasText == FilterData.getOptionType(i)) getItemAt(8, i).isChecked = true
        }

    }

}