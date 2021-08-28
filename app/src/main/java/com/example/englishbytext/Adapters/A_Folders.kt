package com.example.englishbytext.Adapters

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Objects.FoldersManagement
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.OnSelectMode
import com.example.englishbytext.Utilites.OpenFolderContent
import com.example.englishbytext.Utilites.OpenItem

class A_Folders(val context : Context, val event : (Int, String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    //Init
    var onSelectMode = false
    val selectedHashMap = HashMap<Int, Boolean>()

    val layout = R.layout.a_folder_item
    val list = FoldersManagement.list

    fun changeList(){
        FoldersManagement.updateFolderList()
        notifyDataSetChanged()
    }

    //endregion

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        private val parentLayout : RelativeLayout = itemView.findViewById(R.id.parentLayout)
        private val folderItem : TextView = itemView.findViewById(R.id.folderItem)
        private val openFolderContent : ImageView = itemView.findViewById(R.id.openFolderContent)
        private val selectedView : ImageView = ImageView(context)

        fun bindView(position : Int){
            folderItem.text = list[position]

            parentLayout.setOnLongClickListener {
                println("-->Select Mode")
                onSelectMode(position)
                true
            }

            parentLayout.setOnClickListener {
                if(!onSelectMode){
                    openFolderContentClick(position)
                }else{
                    selectedHashMap[position] = selectedHashMap[position] == null || selectedHashMap[position] == false
                    notifyItemChanged(position)
                }
            }

            openFolderContent.setOnClickListener {
                event(OpenFolderContent, list[position])
            }

            //functions

            setSelectedView(position)
        }

        private fun openFolderContentClick(position: Int){
            event(OpenItem, list[position])
        }

        private fun onSelectMode(position: Int){
            if(!onSelectMode){
                selectedHashMap[position] = true
                setSelectedView(position)
                onSelectMode = true
                event(OnSelectMode, "")
            }
        }

        private fun setSelectedView(position: Int){
            if(selectedHashMap[position] == null || selectedHashMap[position] == false){
                selectedView.visibility = View.INVISIBLE
            }else{
                selectedView.setBackgroundResource(R.color.folder_selectItem)
                if(selectedView.parent == null) parentLayout.addView(selectedView)
                selectedView.visibility = View.VISIBLE
                val width = parentLayout.width
                val height = parentLayout.height
                selectedView.layoutParams = RelativeLayout.LayoutParams(width, height)
            }
        }

    }

    fun getSelected() : ArrayList<String>{
        val res = ArrayList<String>()
        if(onSelectMode){
            for(item in selectedHashMap.keys){
                if(selectedHashMap[item]!!){
                    res.add(FoldersManagement.getPath() + "/" + list[item])
                }
            }
        }
        return res
    }

    fun deaSelectMode() : Boolean{
        if(onSelectMode){
            selectedHashMap.clear()
            notifyDataSetChanged()
            onSelectMode = false
            return true
        }
        return false
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
    //endregion

}