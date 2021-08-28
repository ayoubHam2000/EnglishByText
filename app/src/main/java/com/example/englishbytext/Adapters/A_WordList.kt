package com.example.englishbytext.Adapters

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Classes.schemas.Word
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.WordsManagement
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.OnSelectMode
import com.example.englishbytext.Utilites.OpenItem
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class A_WordList(val context : Context, val fgType : String, val passedData : String, val event : (Int, String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //region init
    private val layout = R.layout.a_wordlist_item
    private val list = WordsManagement.wordList
    private val filterList = ArrayList<Word>()
    private var onSelectMode = false
    private val selected = HashMap<String, Boolean>()
    private var hasImagesMap = HashMap<String, Boolean>()
    private var hasAudiosMap = HashMap<String, Boolean>()

    fun changeList(){
        WordsManagement.updateWordList(fgType, passedData)
        filterSearch()
        notifyDataSetChanged()
        getHasMedia()
    }

    private fun getHasMedia(){
        thread{
            hasImagesMap = DataBaseServices.getWordsHasImages()
            hasAudiosMap = DataBaseServices.getWordsHasAudios()
            Handler(context.mainLooper).post { notifyDataSetChanged() }
        }
    }
    //endregion

    //region filter
    fun filterSearch(s : String = "", regex : Boolean = false, favorite : Boolean = false){
        filterList.clear()
        if(regex){
            regexSearch(s, favorite)
        }else{
            containsSearch(s, favorite)
        }
    }

    private fun regexSearch(s : String, favorite: Boolean){
        val regex = s.toRegex()
        for(item in list){
            if(regex.matches(item.name) && (item.isFavorite || !favorite))
                filterList.add(item)
        }
    }

    private fun containsSearch(s : String, favorite: Boolean){
        for(item in list){
            if(item.name.contains(s) && (item.isFavorite || !favorite))
                filterList.add(item)
        }
    }
    //endregion

    //endregion

    //region functions

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        private val mainLayout : RelativeLayout = itemView.findViewById(R.id.mainLayout)
        private val selectedItem : ImageView = itemView.findViewById(R.id.selectedItem)
        private val favoriteBtn : ImageView = itemView.findViewById(R.id.favoriteBtn)
        private val wordName : TextView = itemView.findViewById(R.id.wordName)
        private val wordNbr : TextView = itemView.findViewById(R.id.wordNbr)
        private val hasAudios : ImageView = itemView.findViewById(R.id.hasAudios)
        private val hasImage : ImageView = itemView.findViewById(R.id.hasImage)

        fun bindView(position: Int){
            wordNbr.text = (position + 1).toString()
            wordName.text = filterList[position].name

            setFavoriteView(position)
            setHasMedia(position)
            setSelectedView(false)
            favoriteBtn.setOnClickListener {makeFavorite(position) }
            mainLayout.setOnClickListener { itemClick(position) }
            mainLayout.setOnLongClickListener {
                onSelectMode(position)
                true
            }
        }

        private fun setHasMedia(position: Int){
            hasImage.visibility = if(hasImagesMap[filterList[position].name] != null) View.VISIBLE else View.GONE
            hasAudios.visibility = if(hasAudiosMap[filterList[position].name] != null) View.VISIBLE else View.GONE
        }

        private fun setFavoriteView(position: Int){
            val view = if(filterList[position].isFavorite) R.drawable.ic_favorite_active else R.drawable.ic_favorite
            favoriteBtn.setBackgroundResource(view)
        }

        private fun makeFavorite(position: Int){
            if(!onSelectMode){
                val word = filterList[position].name
                val isFavorite = filterList[position].isFavorite

                filterList[position].isFavorite = !isFavorite
                DataBaseServices.updateWordFavorite(word, !isFavorite)
                notifyItemChanged(position)
            }
        }

        private fun itemClick(position: Int){
            if(onSelectMode){
                val n = filterList[position].name
                selected[n] = selected[n] == null || selected[n] == false
                setSelectedView(selected[n]!!)
            }else{
                event(OpenItem, filterList[position].name)
            }
        }

        private fun setSelectedView(selected: Boolean){
            if(selected){
                selectedItem.visibility = View.VISIBLE
                val width = mainLayout.width
                val height = mainLayout.height
                selectedItem.layoutParams = RelativeLayout.LayoutParams(width, height)
            }else{
                selectedItem.visibility = View.INVISIBLE
            }
        }

        private fun onSelectMode(position: Int){
            if(!onSelectMode){
                val n = filterList[position].name
                event(OnSelectMode, "")
                selected[n] = true
                setSelectedView(true)
                onSelectMode = true
            }
        }

    }

    fun getSelected() : ArrayList<String>{
        val res = ArrayList<String>()
        if(onSelectMode){
            for(item in selected.keys){
                if(selected[item]!!){
                    res.add(item)
                }
            }
        }
        return res
    }

    fun deaSelectMode() : Boolean{
        if(onSelectMode){
            selected.clear()
            notifyDataSetChanged()
            onSelectMode = false
            return true
        }
        return false
    }


    //endregion

    //region override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bindView(position)
    }

    override fun getItemCount(): Int {
        return filterList.count()
    }
    //endregion
}