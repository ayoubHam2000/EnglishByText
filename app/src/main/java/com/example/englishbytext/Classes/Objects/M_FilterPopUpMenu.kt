package com.example.englishbytext.Classes.Objects

import android.content.Context
import android.view.View
import com.example.englishbytext.Classes.Custom.MyPopUpMenu
import com.example.englishbytext.Objects.MainSetting
import com.example.englishbytext.R

class M_FilterPopUpMenu(
    context: Context,
    view: View,
    menuLayout: Int
) : MyPopUpMenu(context, view, menuLayout)
{

    init {
        val regex = getItemById(R.id.isOnRegex)
        val random = getItemById(R.id.randomSort)

        regex.isChecked = MainSetting.onRegexSearch
        random.isChecked = MainSetting.isRandomSortIsActive
    }



}