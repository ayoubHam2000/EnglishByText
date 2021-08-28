package com.example.englishbytext.Adapters

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.Objects.Lib.colorWhiter
import com.example.englishbytext.Objects.SetManagement
import com.example.englishbytext.Objects.TextManagement
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.*
import kotlin.concurrent.thread
import kotlin.math.min

class A_textItem(val context : Context, val event : (Int, Int) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val layoutItem = R.layout.a_text_item
    private val list = TextManagement.texts
    private val selected = HashMap<Int, Boolean>()
    private val setColor = SetManagement.getSet()!!.tagColor
    private var wordsCountMap = HashMap<Int, Int>()
    var onSelectMode = false

    fun changeList(){
        TextManagement.getListOfText()
        when(list.isNotEmpty()){
            true -> event(NotEmpty, -1)
            false -> event(Empty, -1)
        }
        notifyDataSetChanged()
        selected.clear()
        getWordsCount()
    }

    fun getWordsCount(){
        thread {
            wordsCountMap = DataBaseServices.getTextWordsCount()
            Handler(context.mainLooper).post {
                notifyDataSetChanged()
            }
        }
    }

    fun deactivateSelection(){
        onSelectMode = false
        selected.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems() : ArrayList<Int>{
        val res = ArrayList<Int>()

        for(item in selected.keys){
            if(selected[item]!!)
                res.add(item)
        }
        return res
    }

    inner class ViewHolder(viewItem : View?) : RecyclerView.ViewHolder(viewItem!!){
        private val cardView : ImageView = viewItem!!.findViewById(R.id.cardView)
        private val father : RelativeLayout = viewItem!!.findViewById(R.id.father)
        private val itemColor : ImageView = viewItem!!.findViewById(R.id.item_color)
        private val mainLayout : RelativeLayout = viewItem!!.findViewById(R.id.mainLayout)
        private val itemName : TextView = viewItem!!.findViewById(R.id.item_name)
        private val textContent : TextView = viewItem!!.findViewById(R.id.textContent)
        private val itemEdit : ImageView = viewItem!!.findViewById(R.id.item_edit)
        private val wordsCount : TextView = viewItem!!.findViewById(R.id.wordsCount)

        fun bindView(position: Int){
            itemName.text = list[position].title
            textContent.text = getText(position)

            val count = wordsCountMap[list[position].id] ?: 0
            wordsCount.text = count.toString()

            functions(position)
        }

        private fun functions(position: Int){
            itemEdit.setOnClickListener { event(Edit, list[position].id) }
            father.setOnClickListener{onSelectClick(position)}
            father.setOnLongClickListener {
                onSelectMode = true
                selected[position] = true
                notifyItemChanged(position)
                event(OnSelectMode, -1)
                true
            }
            selectAction(position)
            changeBackgroundColor()
        }

        private fun changeBackgroundColor(){
            val color = setColor.colorWhiter(WhitePercentage)
            Lib.changeBackgroundTint(color, mainLayout)
            Lib.changeBackgroundTint(setColor, itemColor)
        }

        private fun onSelectClick(position: Int){
            if(onSelectMode){
                selected[position] = selected[position] == null || selected[position] == false
                notifyItemChanged(position)
            }else{
                event(OpenItem, list[position].id)
            }
        }

        private fun selectAction(position: Int){
            if(selected[position] == null || selected[position] == false){
                cardView.setBackgroundColor(context.getColor(R.color.transparent2))
            }else{
                cardView.setBackgroundColor(context.getColor(R.color.selectionColor))
            }
        }

        private fun getText(position: Int) : String{
            val str = list[position].text
            val minChar = min(str.length, MaxTextChars)

            if(minChar > 0){
                var res = str.substring(0, minChar)
                if(res[minChar - 1] != '.')
                    res += if(minChar == str.length) "." else "..."
                return res
            }
            return "Empty"
        }

    }



    //region override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(layoutItem, parent, false)
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