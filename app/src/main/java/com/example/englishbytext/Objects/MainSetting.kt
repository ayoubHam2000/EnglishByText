package com.example.englishbytext.Objects

import android.graphics.Typeface
import com.example.englishbytext.Classes.Custom.BoolVarBase
import com.example.englishbytext.Classes.Custom.IntVarBase
import com.example.englishbytext.Classes.Custom.StringVarBase
import com.example.englishbytext.Utilites.*

object MainSetting {

    val selectedSet = StringVarBase(AllSet, V_SelectedSet)
    val categorySection = BoolVarBase(true, V_CategorySection)
    val collectionSection = BoolVarBase(true, V_CollectionSection)
    val isDarkMode = BoolVarBase(false, V_DarkMode)
    val selectedTextSize = IntVarBase(2, V_TextSize)
    val selectedTextStyle = IntVarBase(0, V_TextFont)
    val selectedTextFontType = IntVarBase(0, V_TextStyle)
    val onRegexSearch = BoolVarBase(false, V_OnRegexSearch)
    val onSortPractice = BoolVarBase(true, V_sortPractice)
    val sortTypeWordList = IntVarBase(SORT_CREATED_TIME_DESC, V_SortTypeWordList)
    val selectedExamplesCollection = IntVarBase(DEFAULT_EXAMPLE_COLLECTION, V_examplesCollection)

    //SetManagement.setSelectedSet(selectedSet)

    //not save in the data base
    var isRandomSortIsActive = false


    //region Text
    private val textSizeOptions = arrayOf(0.5f, 0.75f, 1f, 1.5f, 2f)
    private val textFontOptions = arrayOf(
            "sans-serif-thin",
            "casual",
            "cursive",
            "monospace",
            "sans-serif",
            "sans-serif-condensed",
    )
    private val textFontTypeOptions = arrayOf(
            Typeface.NORMAL,
            Typeface.BOLD,
            Typeface.ITALIC,
            Typeface.BOLD_ITALIC
    )

    fun getTextSize() : Float{
        return textSizeOptions[selectedTextSize.get()]
    }

    fun getTextStyle() : String{
        return textFontOptions[selectedTextStyle.get()]
    }

    fun getFontsType() : Int{
        return textFontTypeOptions[selectedTextFontType.get()]
    }

    fun getFonts() : Array<String>{
        return textFontOptions
    }

    fun getTextSizeText() : String{
        val textSize = arrayListOf(
            "Smallest",
            "Small",
            "Default",
            "Large",
            "Largest"
        )
        return textSize[selectedTextSize.get()]
    }

    fun getTextStyleText() : String{
        return textFontOptions[selectedTextStyle.get()].replace("_-".toRegex(), " ")
    }

    fun getTextFontText() : String{
        val textFont = arrayListOf(
            "Normal",
            "Bold",
            "Italic",
            "Bold Italic"
        )
        return textFont[selectedTextFontType.get()]
    }

    //endregion

}