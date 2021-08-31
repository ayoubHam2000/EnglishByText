package com.example.englishbytext.Adapters

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.FoldersManagement
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.OnSelectMode
import com.example.englishbytext.Utilites.OpenFolderContent
import com.example.englishbytext.Utilites.OpenItem
import com.example.englishbytext.Utilites.SelectModeClick
import kotlin.concurrent.thread

class A_Folders(context : Context, val event : (Int, String) -> Unit)
    : MySelectAdapter(context) {


    //Init
    val layout = R.layout.a_folder_item
    val list = FoldersManagement.list
    private var numberOfWordsHashMap = HashMap<String, Int>()

    fun changeList(){
        FoldersManagement.updateFolderList()
        notifyDataSetChanged()
        getExtraInfo()
    }

    private fun getExtraInfo(){
        thread {
            numberOfWordsHashMap = DataBaseServices.getFoldersWordsNumber()
            Handler(context.mainLooper).post { notifyDataSetChanged() }
        }
    }

    //endregion

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        private val parentLayout : RelativeLayout = itemView.findViewById(R.id.parentLayout)
        private val folderItem : TextView = itemView.findViewById(R.id.folderItem)
        private val openFolderContent : ImageView = itemView.findViewById(R.id.openFolderContent)
        private val folderIndex : TextView = itemView.findViewById(R.id.folderIndex)
        private val wordsNumberView : TextView = itemView.findViewById(R.id.wordsNumberView)
        //vars

        fun bindView(position : Int){
            val index = "#" + (position + 1)
            folderItem.text = list[position]
            folderIndex.text = index

            val absPath = FoldersManagement.getPath() + "/"
            val wordsNbr = numberOfWordsHashMap[absPath + list[position]]
            wordsNumberView.text = wordsNbr?.toString() ?: "0"

            openFolderContent.setOnClickListener {
                event(OpenFolderContent, list[position])
            }

            //functions
            addSelectItem(parentLayout, position){
                if(!onSelectMode){
                    openFolderContentClick(position)
                }else{
                    event(SelectModeClick, "")
                }
            }
        }

        private fun openFolderContentClick(position: Int){
            event(OpenItem, list[position])
        }

    }

    fun getSelectedFormat() : ArrayList<String>{
        val res = ArrayList<String>()
        val selectedPosition = getSelected()
        val path = FoldersManagement.getPath() + "/"

        for(item in selectedPosition){
            res.add(path + list[item])
        }

        return res
    }


    //region override
    override fun onSelectModeActive() {
        event(OnSelectMode, "")
    }

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

    override fun onDeaSelect() {
        changeList()
    }
    //endregion

}