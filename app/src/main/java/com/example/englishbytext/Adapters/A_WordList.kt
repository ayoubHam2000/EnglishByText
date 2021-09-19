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
import com.example.englishbytext.Classes.schemas.FilterData
import com.example.englishbytext.Classes.schemas.Word
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.WordsManagement
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.OnSelectMode
import com.example.englishbytext.Utilites.OpenItem
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class A_WordList(
    val context : Context,
    private val fgType : String,
    private val passedData : String,
    val event : (Int, String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //region init


    private val layout = R.layout.a_wordlist_item
    private val list = WordsManagement.wordList
    private val filterList = ArrayList<Word>()
    private var onSelectMode = false
    private val selectedHashMap = HashMap<String, Boolean>()

    private var hasImagesMap = HashMap<String, Boolean>()
    private var hasAudiosMap = HashMap<String, Boolean>()
    private var hasTagMap = HashMap<String, Boolean>()
    private var hasFolderMap = HashMap<String, Boolean>()
    private var hasTextMap = HashMap<String, Boolean>()
    private var hasRelatedMap = HashMap<String, Boolean>()
    private var hasExampleMap = HashMap<String, Boolean>()
    private var hasDefinitionMap = HashMap<String, Boolean>()

    fun changeList(filterData: FilterData){
        WordsManagement.updateWordList(fgType, passedData)
        //filterSearch()
        //notifyDataSetChanged()
        getHasMedia(filterData)
    }

    private fun getHasMedia(filterData: FilterData){
        thread{

            hasImagesMap = DataBaseServices.getWordsHasImages()
            hasAudiosMap = DataBaseServices.getWordsHasAudios()

            hasTagMap = DataBaseServices.getWordsHasTag()
            hasFolderMap = DataBaseServices.getWordsHasFolder()
            hasTextMap = DataBaseServices.getWordsHasText()
            hasRelatedMap = DataBaseServices.getWordsHasRelated()
            hasDefinitionMap = DataBaseServices.getWordsHasDefinition()
            hasExampleMap = DataBaseServices.getWordsHasExample()

            Handler(context.mainLooper).post {
                filterSearch(filterData)
                notifyDataSetChanged()
            }
        }
    }
    //endregion

    //region filter
    fun filterSearch(filterData : FilterData){
        filterList.clear()
        if(filterData.onRegex){
            regexSearch(filterData)
        }else{
            containsSearch(filterData)
        }
    }

    private fun regexSearch(filterData: FilterData){
        val s = filterData.searchWord
        val regex = s.toRegex()
        for(item in list){
            if(regex.matches(item.name) && isOnCategory(item, filterData))
                filterList.add(item)
        }
    }

    private fun containsSearch(filterData: FilterData){
        val s = filterData.searchWord
        for(item in list){
            if(item.name.contains(s) && isOnCategory(item, filterData))
                filterList.add(item)
        }
    }

    private fun isOnCategory(item : Word, filterData: FilterData) : Boolean{
        val set1 = arrayListOf(
            filterData.isFavorite,
            filterData.hasDefinition,
            filterData.hasExample

        )
        val set2 = arrayListOf(
            item.isFavorite,
            hasDefinitionMap[item.name] == true,
            hasExampleMap[item.name] == true
        )
        val set3 = ArrayList<Boolean>(3)
        for(i in 0 until set1.count()){
            if(set1[i]){
                set3.add(set2[i])
            }
        }
        if (set3.isEmpty()) return true
        return false !in set3
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
        private val wordFrequencyView : TextView = itemView.findViewById(R.id.wordFrequencyView)
        private val hasAudios : ImageView = itemView.findViewById(R.id.hasAudios)
        private val hasImage : ImageView = itemView.findViewById(R.id.hasImage)
        private val hasTag : ImageView = itemView.findViewById(R.id.hasTag)
        private val hasFolder : ImageView = itemView.findViewById(R.id.hasFolder)
        private val hasText : ImageView = itemView.findViewById(R.id.hasText)
        private val hasRelated : ImageView = itemView.findViewById(R.id.hasRelated)
        private val hasDefinition : TextView = itemView.findViewById(R.id.hasDefinition)
        private val hasExample : TextView = itemView.findViewById(R.id.hasExample)

        fun bindView(position: Int){
            wordNbr.text = (position + 1).toString()
            wordName.text = filterList[position].name
            wordFrequencyView.text = WordsManagement.getWordFrequency(filterList[position].name).toString()

            setFavoriteView(position)
            setHasMedia(position)
            setSelectedView(position)
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

            hasTag.visibility = if(hasTagMap[filterList[position].name] != null) View.VISIBLE else View.GONE
            hasFolder.visibility = if(hasFolderMap[filterList[position].name] != null) View.VISIBLE else View.GONE
            hasText.visibility = if(hasTextMap[filterList[position].name] != null) View.VISIBLE else View.GONE
            hasRelated.visibility = if(hasRelatedMap[filterList[position].name] != null) View.VISIBLE else View.GONE
            hasDefinition.visibility = if(hasDefinitionMap[filterList[position].name] != null) View.VISIBLE else View.GONE
            hasExample.visibility = if(hasExampleMap[filterList[position].name] != null) View.VISIBLE else View.GONE
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
                selectedHashMap[n] = selectedHashMap[n] == null || selectedHashMap[n] == false
                setSelectedView(position)
            }else{
                event(OpenItem, filterList[position].name)
            }
        }

        private fun setSelectedView(position: Int){
            val n = filterList[position].name
            val s = (selectedHashMap)[n] == true
            println("--> ${position + 1} $s")
            if(s){
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
                selectedHashMap[n] = true
                setSelectedView(position)
                onSelectMode = true
            }
        }

    }

    fun getSelected() : ArrayList<String>{
        val res = ArrayList<String>()
        if(onSelectMode){
            for(item in selectedHashMap.keys){
                if(selectedHashMap[item]!!){
                    res.add(item)
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

    fun selectAll(){
        for(item in filterList){
            val n = item.name
            selectedHashMap[n] = true
        }
        notifyDataSetChanged()
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