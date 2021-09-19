package com.example.englishbytext.Classes.Custom

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.core.view.children
import com.example.englishbytext.R
import java.lang.RuntimeException

open class MyPopUpMenu(
    context : Context,
    view : View,
    menuLayout : Int
    ) : PopupMenu(context, view) {

    init {
        inflate(menuLayout)
        view.setOnClickListener {
            show()
        }
    }


    fun getItemById(id : Int) : MenuItem{
        return getItemById(menu, id)!!
    }

    private fun getItemById(theMenu : Menu, id : Int) : MenuItem?{
        val children = theMenu.children
        for(item in children){
            val res = when{
                item.hasSubMenu() -> getItemById(item.subMenu, id)
                item.itemId == id -> item
                else -> null
            }
            if(res != null) return res
        }
        return null
    }



}