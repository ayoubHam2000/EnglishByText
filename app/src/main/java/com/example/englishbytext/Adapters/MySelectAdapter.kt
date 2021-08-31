package com.example.englishbytext.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Classes.schemas.StringId
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.R

abstract class MySelectAdapter (val context : Context)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //region Init
    private val selected = HashMap<Int, Boolean>()
    var onSelectMode = false


    //endregion


    //region function
    protected fun addSelectItem(parent : ViewGroup, position: Int, click : () -> Unit){
        val oldView = parent.findViewById<ImageView>(R.id.selectItem)
        val selectedImage = oldView ?: ImageView(context)
        selectedImage.setBackgroundResource(R.color.selectionColor)
        selectedImage.visibility = View.INVISIBLE
        if(oldView == null){
            selectedImage.id = R.id.selectItem
            parent.addView(selectedImage)
        }

        parent.setOnClickListener {
            if(onSelectMode){
                val width = parent.width
                val height = parent.height

                selected[position] = (selected[position] == null || selected[position] == false)
                if(selected[position]!!){
                    selectedImage.layoutParams = RelativeLayout.LayoutParams(width, height)
                    selectedImage.visibility = View.VISIBLE
                }else{
                    selectedImage.visibility = View.INVISIBLE
                }
            }
            click()
        }

        parent.setOnLongClickListener {
            if(!onSelectMode){
                onSelectMode = true
                parent.performClick()
                onSelectModeActive()
            }
            true
        }

    }

    fun getSelected() : ArrayList<Int>{
        val res = ArrayList<Int>()
        for (item in selected.keys) {
            if(selected[item]!!)
                res.add(item)
        }
        return res
    }

    fun deaSelect() : Boolean{
        if(onSelectMode){
            onSelectMode = false
            selected.clear()
            onDeaSelect()
            return true
        }
        return false
    }

    fun getSelectedCount() : Int{
        var res = 0
        for(item in selected.keys){
            if(selected[item] == true)
                res++
        }
        return res
    }

    abstract fun onSelectModeActive()
    open fun onDeaSelect(){}

    //endregion


}