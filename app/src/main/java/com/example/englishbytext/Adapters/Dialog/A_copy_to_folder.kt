package com.example.englishbytext.Adapters.Dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.FoldersManagement
import com.example.englishbytext.R
import java.util.*
import kotlin.collections.ArrayList

class A_copy_to_folder(val context : Context, val event : ()->Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //region init
    val list = ArrayList<String>()
    private val path = LinkedList<String>()
    private val layout = R.layout.a_copy_to_folder_item

    fun changeList(){
        list.clear()
        getList()
        notifyDataSetChanged()
    }

    private fun getList(){
        val paths = DataBaseServices.getListOfFolders(getPath())
        for(item in paths){
            list.add(item.split("/").last())
        }
        list.sortBy { it }
    }

    //endregion

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        private val folderText : TextView = itemView.findViewById(R.id.folderText)

        fun bindView(position: Int){
            folderText.text = list[position]

            folderText.setOnClickListener {
                path.add(list[position])
                event()
                changeList()
            }
        }
    }

    fun getPath() : String{
        if(path.isEmpty()){
            return "."
        }
        return "./" + path.joinToString("/")
    }

    fun isPathNotEmpty() : Boolean{
        return path.isNotEmpty()
    }

    fun back() : Boolean{
        if(path.isNotEmpty()){
            path.removeLast()
            event()
            changeList()
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