package com.example.englishbytext.Classes.Objects

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.get
import com.example.englishbytext.Dialogs.MyDialogBuilder
import com.example.englishbytext.Objects.Setting
import com.example.englishbytext.R

class D_textSize(context : Context, val event : () -> Unit) : MyDialogBuilder(context, R.layout.d_text_size_option) {

    lateinit var listRadioButton : RadioGroup

    override fun initView(builderView: View) {
        val commit = builderView.findViewById<ImageView>(R.id.d_add)
        listRadioButton = builderView.findViewById(R.id.listRadioButton)


        dialog.setOnShowListener {
            val selected = checkedItem()

            (listRadioButton[selected] as RadioButton).isChecked = true
            commit.setOnClickListener {
                commit()
                event()
            }
        }

    }

    private fun checkedItem() : Int{
        return Setting.selectedTextSize
    }

    private fun commit(){
        val id = listRadioButton.findViewById<RadioButton>(listRadioButton.checkedRadioButtonId)
        val selected = listRadioButton.indexOfChild(id)
        Setting.setTextSize(selected.toString())
        dismiss()
    }

}