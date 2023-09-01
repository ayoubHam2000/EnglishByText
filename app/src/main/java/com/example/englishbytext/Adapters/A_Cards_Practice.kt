package com.example.englishbytext.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.example.englishbytext.Classes.Custom.MyOnSwipeTouchListener
import com.example.englishbytext.Dialogs.D_editItem
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.Objects.WordsManagement
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.Edit
import com.example.englishbytext.Utilites.NextPage
import com.example.englishbytext.Utilites.VoicePractice
import java.util.Locale


class A_Cards_Practice(val context: Context, val practiceType: Int, val event: (Int, Int) -> Unit) : PagerAdapter(){

    //region init

    //====================================
    //++++++++++++++++++++++  vars
    //====================================
    var list = WordsManagement.practiceList
    private val layout = R.layout.a_cards_practice_item
    private val theColors = getColorsFromResources()
    private val checkedWords = hashMapOf<String, Boolean>()

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
        private val voiceTextClickView : TextView = view.findViewById(R.id.voiceTextClickView)
        private val wordFrequencyView : TextView = view.findViewById(R.id.wordFrequencyView)
        private val wordMaster : ImageView = view.findViewById(R.id.masterWord)
        private val skipLeft : ImageView = view.findViewById(R.id.skipLeft)
        private val skipRight : ImageView = view.findViewById(R.id.skipRight)

        fun bindView(position: Int){
            setWordName(position)
            // theWordName.text = list[position].name

            wordFrequencyView.text = WordsManagement.getWordFrequency(list[position].name).toString()

            backgroundView(position)
            progressPages(position)
            favoriteView(position)
            masterWordView(position)

            //buttons
            if (theWordName.text == "?" && practiceType == VoicePractice)
                voiceTextClickView.setOnClickListener { wordVoicePracticeClick(position) }
            favoriteWord.setOnClickListener { favoriteClick(position) }
            editWord.setOnClickListener {editWordClick(position)}
            wordMaster.setOnClickListener { masterWordClick(position) }
            wordMaster.setOnLongClickListener {
                masterWordLongClick(position)
                true
            }
            skipLeft.setOnClickListener { skipLeftClick(position) }
            skipRight.setOnClickListener { skipRightClick(position) }
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
                            DataBaseServices.updateSetVisited(list[position].name)
                            if (list[position].isKnown == 0)
                                list[position].isKnown = 2
                            masterWordView(position)
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

        private fun masterWordView(position: Int){
            when (list[position].isKnown) {
                4 -> Lib.changeBackgroundTint(context.getColor(R.color.master_word_active), wordMaster)
                1 -> Lib.changeBackgroundTint(context.getColor(R.color.archived_word), wordMaster)
                0 -> Lib.changeBackgroundTint(context.getColor(R.color.master_word), wordMaster)
                else -> Lib.changeBackgroundTint(context.getColor(R.color.visited_word), wordMaster)
            }
        }

        private fun setWordName(position: Int) {
            val wordName = list[position].name

            if (practiceType != VoicePractice || checkedWords[wordName] == true) {
                theWordName.text = wordName
            } else {
                theWordName.text = "?"
            }
        }
        private fun backgroundView(position: Int){
            val theColor = theColors[position % theColors.count()]

            background.backgroundTintList = ColorStateList.valueOf(theColor)
            //getHexColor("dd",theColor)
            Lib.changeBackgroundTint(Color.parseColor(getHexColor("dd", theColor)), father)
        }

        //Btn
        private fun wordVoicePracticeClick(position: Int){
            val wordName = list[position].name

            var defDialog : D_editItem? = null
            defDialog = D_editItem(context){
                val theValue = it.trim().lowercase()
                if (wordName.lowercase() == theValue) {
                    theWordName.text = wordName
                    checkedWords[wordName] = true
                    voiceTextClickView.setOnClickListener(null)
                    voiceTextClickView.isClickable = false
                    defDialog!!.dismiss()
                } else {
                    defDialog!!.inputName.error = "Try again"
                }

            }

            defDialog.textHint = "write... "
            defDialog.buildAndDisplay()

            var textToSpeech: TextToSpeech? = null
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech?.language = Locale.US
                    textToSpeech?.speak(wordName, TextToSpeech.QUEUE_ADD, null, "SAY_IT")
                }
            }
        }



        private fun favoriteClick(position: Int){
            val theWord = list[position]

            val f = !theWord.isFavorite
            theWord.isFavorite = f
            DataBaseServices.updateWordFavorite(theWord.name, f)
            favoriteView(position)
        }

        private fun editWordClick(position: Int){
            event(Edit, position)
        }

        private fun masterWordClick(position: Int){
            val theWord = list[position]

            if (theWord.isKnown == 4)
                theWord.isKnown = 0
            else
                theWord.isKnown = 4
            DataBaseServices.updateIsWordKnown(arrayListOf(theWord.name))
            masterWordView(position)
            if (theWord.isKnown == 4)
                event(NextPage, position + 1)
        }

        private fun masterWordLongClick(position: Int){
            val theWord = list[position]
            theWord.isKnown = 1
            DataBaseServices.updateSetArchived(theWord.name)
            masterWordView(position)
            event(NextPage, position + 1)
        }

        private fun skipRightClick(position: Int){
            var nextPos = 0
            while (position + nextPos < list.size && list[position + nextPos].isKnown != 0)
                nextPos++
            event(NextPage, position + nextPos)
        }

        private fun skipLeftClick(position: Int){
            if (position != 0)
                event(NextPage, 0)
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

