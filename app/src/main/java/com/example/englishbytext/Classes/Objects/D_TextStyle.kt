package com.example.englishbytext.Classes.Objects

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import androidx.core.view.get
import com.example.englishbytext.Dialogs.MyDialogBuilder
import com.example.englishbytext.Objects.MainSetting
import com.example.englishbytext.R

class D_TextStyle(context : Context, val event : () -> Unit) : MyDialogBuilder(context, R.layout.d_text_style_option) {

    lateinit var listRadioButton : RadioGroup

    override fun initView(builderView: View) {
        val commit = builderView.findViewById<ImageView>(R.id.d_add)
        listRadioButton = builderView.findViewById(R.id.listRadioButton)
        addFonts()


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
        return MainSetting.selectedTextStyle.get()
    }

    private fun commit(){
        val id = listRadioButton.findViewById<RadioButton>(listRadioButton.checkedRadioButtonId)
        val selected = listRadioButton.indexOfChild(id)
        MainSetting.selectedTextStyle.set(selected)
        dismiss()
    }

    private fun addFonts(){
        val fonts = MainSetting.getFonts()
        for(item in fonts){
            val typeface = Typeface.create(item, Typeface.NORMAL)
            val newRadio = RadioButton(context)
            val layout = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT)
            layout.marginStart = 20
            layout.marginEnd = 20
            newRadio.layoutParams = layout
            newRadio.setPadding(30, 0, 30, 0)
            newRadio.text = item.replace("[_\\-]".toRegex(), " ")
            newRadio.typeface = typeface
            listRadioButton.addView(newRadio)
        }
    }
}