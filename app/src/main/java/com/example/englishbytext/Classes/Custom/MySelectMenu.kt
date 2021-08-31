package com.example.englishbytext.Classes.Custom




import android.view.*
import androidx.appcompat.widget.ActionBarContextView
import com.example.englishbytext.R



class MySelectMenu(val event : () -> Unit) : ActionMode.Callback {
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {

        //val inflater: MenuInflater = mode!!.menuInflater
        //inflater.inflate(R.menu.m_select_menu, menu)
        //menu?.clear()


        return true
    }



    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        menu?.clear()
        return false
    }



    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        //event(item!!.itemId)
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {

        event()
    }

}