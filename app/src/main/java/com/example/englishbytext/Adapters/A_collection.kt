package com.example.englishbytext.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Classes.schemas.Collections
import com.example.englishbytext.Objects.CollectionManagement
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.*

class A_collection(val context : Context, val event : (Int, Collections?) -> Unit):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //region init and changeList vars
    val list = CollectionManagement.collections
    //var selectFirst = true

    private val layout = R.layout.a_collection_item

    fun changeList(){
        //selectFirst = true
        CollectionManagement.getListOfCollection()
        when(list.isNotEmpty()){
            true -> event(NotEmpty, null)
            false -> event(Empty, null)
        }
        notifyDataSetChanged()
    }

    //endregion

    inner class ViewHolder(itemView : View?) : RecyclerView.ViewHolder(itemView!!){

        //region vars
        //var
        private lateinit var popUpMenu : PopupMenu

        //view
        private val collectionBackground = itemView?.findViewById<RelativeLayout>(R.id.collectionBackground)
        private val collectionName = itemView?.findViewById<TextView>(R.id.collectionName)
        private val collectionDetail = itemView?.findViewById<TextView>(R.id.collectionDetail)
        private val practiceButton = itemView?.findViewById<TextView>(R.id.practiceButton)
        private val collectionMenu = itemView?.findViewById<ImageView>(R.id.collectionMenu)
        private val collectionTagColor = itemView?.findViewById<ImageView>(R.id.collectionTagColor)

        //endregion

        //region Functionality
        fun bindView(position : Int){
            collectionName?.text = list[position].name
            collectionDetail?.text = getCollectionDetail(list[position])
            //checkCollection?.isChecked = list[position].isChecked
            popUpMenu = Lib.initPopupMenu(context, collectionMenu!!, R.menu.m_collection_menu)

            initFun(position)
        }

        private fun initFun(position: Int){
            changeColorTag(position)
            menuClickItems(position)
            collectionBackground?.setOnClickListener { itemClick(position) }
            practiceButton?.setOnClickListener { practiceClick(position) }
        }

        //endregion


        //region functions
        //region buttons
        private fun itemClick(position: Int){
            event(OpenItem, list[position])
        }

        private fun practiceClick(position: Int){
            event(Practice, list[position])
        }

        //endregion

        private fun changeColorTag(position: Int){
            val setColor = DataBaseServices.getSetColor(list[position].father)
            Lib.changeBackgroundTint(setColor, collectionTagColor)
        }

        private fun getCollectionDetail(collection: Collections) : String{
            return "TEXTS COUNT : ${DataBaseServices.getTextCount(collection.name, collection.father)}"
        }

        private fun menuClickItems(position: Int){

            popUpMenu.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.edit -> event(Edit, list[position])
                    R.id.delete -> event(Delete, list[position])
                }
                true
            }
        }

        //endregion

    }

    //region about recyclerView override
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

    //endregion

}