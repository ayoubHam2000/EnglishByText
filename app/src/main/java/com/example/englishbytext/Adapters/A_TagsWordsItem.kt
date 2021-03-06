package com.example.englishbytext.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Classes.schemas.WordInfoId
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.OnSelectMode
import com.example.englishbytext.Utilites.OpenItem

class A_TagsWordsItem (context : Context, val wordName : String, val event : (Int, String) -> Unit)
    : MySelectAdapter(context) {

    //region Init
    private val layout = R.layout.a_related_item
    private val list = ArrayList<WordInfoId>()


    fun changeList(){
        list.clear()
        list.addAll(DataBaseServices.getWordTags(wordName))
        notifyDataSetChanged()
    }

    //endregion

    //region view

    inner class ViewHolder(viewItem : View) : RecyclerView.ViewHolder(viewItem){
        private val parentView : RelativeLayout = viewItem.findViewById(R.id.parentView)
        private val itemName : TextView = viewItem.findViewById(R.id.item_name)


        fun bindView(position: Int){
            if(wordName == list[position].word){
                itemName.text = list[position].value
            }else{
                val t = "#${list[position].value}"
                itemName.text = t
            }

            addSelectItem(parentView, position){
                if(!onSelectMode){
                    event(OpenItem, list[position].value)
                }
            }
        }
    }

    //endregion

    fun getSelectedIds() : ArrayList<Int>{
        val l = getSelected()
        val res = ArrayList<Int>()
        for(item in l)
            res.add(list[item].id)
        return res
    }


    //region override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bindView(position)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onSelectModeActive() {
        event(OnSelectMode, "")
    }

    override fun onDeaSelect(){
        changeList()
    }

    //endregion
}