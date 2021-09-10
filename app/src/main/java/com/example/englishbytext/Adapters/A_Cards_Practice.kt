package com.example.englishbytext.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.example.englishbytext.Classes.Custom.MyOnSwipeTouchListener
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.Objects.WordsManagement
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.Edit
import com.example.englishbytext.Utilites.NextPage


class A_Cards_Practice(val context: Context, val event: (Int, Int) -> Unit) : PagerAdapter(){

/*
TODO : is known
 */
    //region init

    //====================================
    //++++++++++++++++++++++  vars
    //====================================
    var list = WordsManagement.wordList
    private val layout = R.layout.a_cards_practice_item
    private val theColors = getColorsFromResources()

    //====================================
    //++++++++++++++++++++++  Views
    //====================================


    //====================================
    //++++++++++++++++++++++  Init
    //====================================
    /*fun changeList(){
        notifyDataSetChanged()
    }*/

    //endregion

    //region pageViewer
    private fun configView(view: View, position: Int){
        val viewHolder = ViewHolder(view)
        viewHolder.bindView(position)
    }

    inner class ViewHolder(val view : View){
        private val father : RelativeLayout = view.findViewById(R.id.father)
        private val background : RelativeLayout = view.findViewById(R.id.background)
        private val progressNumber : TextView = view.findViewById(R.id.progressNumber)
        private val progressBar : ProgressBar = view.findViewById(R.id.progressBar)
        private val favoriteWord : ImageView = view.findViewById(R.id.favoriteWord)
        private val editWord : ImageView = view.findViewById(R.id.editWord)
        private val theWordName : TextView = view.findViewById(R.id.theWord)
        private val wordFrequencyView : TextView = view.findViewById(R.id.wordFrequencyView)
        private val isKnownView : ImageView = view.findViewById(R.id.isKnown)

        fun bindView(position: Int){
            theWordName.text = list[position].name
            wordFrequencyView.text = WordsManagement.getWordFrequency(list[position].name).toString()

            backgroundView(position)
            progressPages(position)
            favoriteView(position)
            isKnownView(position)

            //buttons
            favoriteWord.setOnClickListener { favoriteClick(position) }
            editWord.setOnClickListener {editWordClick(position)}
            //isKnownView.setOnClickListener { isKnownClick(position) }

            //fun
            onSweep(position)
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun onSweep(position: Int){
            background.setOnTouchListener(
                    @SuppressLint("ClickableViewAccessibility")
                    object : MyOnSwipeTouchListener(context) {
                        override fun onSwipeLeft() {
                            super.onSwipeLeft()
                            event(NextPage, position + 1)
                        }

                        override fun onSwipeRight() {
                            super.onSwipeRight()
                            event(NextPage, position - 1)
                        }

                        override fun onDoubleClick() {
                            super.onDoubleClick()
                            event(Edit, position)
                        }

                    })
        }

        private fun progressPages(position: Int){
            val len = list.count()
            val text = "${position + 1} / $len"
            progressBar.max = len
            progressBar.progress = position + 1
            progressNumber.text = text
        }

        private fun favoriteView(position: Int){
            val favorite = list[position].isFavorite
            if(favorite){
                favoriteWord.setBackgroundResource(R.drawable.ic_favorite_active)
            }else{
                favoriteWord.setBackgroundResource(R.drawable.ic_favorite)
            }
        }

        private fun isKnownView(position: Int){
            val isKnown = list[position].isKnown
            if(isKnown){
                isKnownView.setBackgroundResource(R.drawable.ic_resource_package)
            }else{
                isKnownView.setBackgroundResource(R.drawable.ic__edit)
            }
        }

        private fun backgroundView(position: Int){
            val theColor = theColors[position % theColors.count()]

            background.backgroundTintList = ColorStateList.valueOf(theColor)
            getHexColor("dd",theColor)
            Lib.changeBackgroundTint(Color.parseColor(getHexColor("dd", theColor)), father)
        }

        //Btn
        private fun favoriteClick(position: Int){
            val theWord = list[position]

            val f = !theWord.isFavorite
            theWord.isFavorite = f
            DataBaseServices.updateWordFavorite(theWord.name, f)
            favoriteView(position)
        }

        private fun isKnownClick(position: Int){
            val theWord = list[position].name
            list[position].isKnown = !list[position].isKnown
            DataBaseServices.updateIsWordKnown(arrayListOf(theWord))
            isKnownView(position)
        }

        private fun editWordClick(position: Int){
            event(Edit, position)
        }

    }

    //region Utilities

    private fun getColorsFromResources() : ArrayList<Int>{
        val ta = context.resources.getStringArray(R.array.backgroundColors)
        val stringColor = ta[0].split(" ")

        val colors = ArrayList<Int>()
        for (c in stringColor){
            if(c.count() != 0){
                colors.add(Color.parseColor(c))
            }
        }
        return colors
    }

    private fun getHexColor(alpha : String, color : Int) : String{
        //println(">> ${String.format("#%06X", 0xFFFFFF and color)}")
        return String.format("#$alpha%06X", 0xFFFFFF and color)
    }

    //endregion

    //endregion

    //region override
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(layout, container, false)

        configView(view, position)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getCount(): Int {
        return list.count()
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as (RelativeLayout)
    }


    //endregion

}

