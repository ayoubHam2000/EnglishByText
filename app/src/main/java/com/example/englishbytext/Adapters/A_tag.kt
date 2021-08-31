package com.example.englishbytext.Adapters

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Classes.schemas.StringId
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.OnSelectMode
import com.example.englishbytext.Utilites.OpenItem
import com.example.englishbytext.Utilites.SelectModeClick
import kotlin.concurrent.thread

class A_tag (context : Context, val event : (Int, String) -> Unit)
    : MySelectAdapter(context) {

    //region Init
    private val layout = R.layout.a_tag_item
    val list = ArrayList<StringId>()
    private var listCount = HashMap<String, Int>()


    fun changeList(){
        list.clear()
        list.addAll(DataBaseServices.getTags())
        setCount()
        notifyDataSetChanged()
    }

    private fun setCount(){
        thread{
            listCount = (DataBaseServices.getTagsCount())
            Handler(context.mainLooper).post {
                notifyDataSetChanged()
            }
        }
    }


    //endregion

    //region view

    inner class ViewHolder(viewItem : View) : RecyclerView.ViewHolder(viewItem){
        private val parentView : RelativeLayout = viewItem.findViewById(R.id.parentView)
        private val itemName : TextView = viewItem.findViewById(R.id.item_name)
        private val itemId : TextView = viewItem.findViewById(R.id.itemId)
        private val countWords : TextView = viewItem.findViewById(R.id.countWords)


        fun bindView(position: Int){
            val id = "#${position + 1}"
            val count = if(listCount[list[position].value] == null) "0" else "${listCount[list[position].value]}"

            itemName.text = list[position].value
            itemId.text = id
            countWords.text = count

            addSelectItem(parentView, position){
                if(!onSelectMode){
                    event(OpenItem, list[position].value)
                }else{
                    event(SelectModeClick, "")
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