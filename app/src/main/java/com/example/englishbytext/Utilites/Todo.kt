package com.example.englishbytext.Utilites

import com.example.englishbytext.Classes.Objects.D_ask_color
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.Objects.TextManagement
import com.example.englishbytext.R

/*








TOD : related ✓
TOD : tags ✓
TOD : list suggestion ✓
TOD : word list tag xof wax dima selected tag katkone f l ma7al tyalha ✓
TOD : text style zoom in, zoom out text and image ✓
TOD : fast add, delete selections ✓
TOD : list of last image ✓
TOD : save load data ✓
TOD : process background ✓
TOD : text font  ✓
TOD : setting view ✓
TOD : fix add copy when back press ✓
TOD : fix display images ✓
TOD : check if zip is valid ✓
TOD : loaded saved ✓



TOD : select collection with delete
TOD : allWords (search, favorite, delete)
TOD : tags
TOD : settings


 */

/*

TOD text
TOD tag

//TEXT ITEM
TOD : ADD BUTTON => ask for a title ✓
TOD : create void item with edit icon and color icon ✓
TOD : Long press to select => delete action ✓

//TEXT EDIT Frag
TOD : action bar => title + menu (text color, background color, title) ✓
TOD : ask save when went out ✓

//TEXT display
TOD : action bar => title + edit icon + list words icon(click word detail, long click select action)
TOD : display text with its style, color, font, background color
TOD : long press on word => copier + create new word (go directly to edit word)
TOD : on saved word => click display short detail => detail click => display detail dialog

//Word detail
dialog with word detail

//Edit Word
fragment

//categories frag
//dark mode
//rotation

*/

/*

features

-> text styles and size
-> add costume styles
-> add settings
-> add costume external styles

 */

//region Menu
/*
private fun menuClickItems(){

    popUpMenu.setOnMenuItemClickListener {
        when(it.itemId){
            R.id.textColor -> changeTextColor()
            R.id.backgroundColor -> changeBackgroundColor()
        }
        true
    }
}

private fun saveChanges(){
    val id = theTextId
    val title = titleEditor.text.toString().trim()
    val text = textEditor.text.toString().trim()

    DataBaseServices.updateText(id, title, text)
    Lib.showMessage(gContext, "Save Successful")
}

private fun refreshChanges(){
    TextManagement.getListOfText()
    setLayout()
}

private fun changeTextColor(){
    val old = TextManagement.getText()!!

    val dialog = D_ask_color(gContext){ color ->
        DataBaseServices.updateTextColor(theTextId, color)
        refreshChanges()
    }
    dialog.theIntColor = old.color
    dialog.isAdvanceOpen = true
    dialog.buildAndDisplay()
}

private fun changeBackgroundColor(){
    val old = TextManagement.getText()!!

    val dialog = D_ask_color(gContext){ color ->
        DataBaseServices.updateTextBackgroundColor(theTextId, color)
        refreshChanges()
    }
    dialog.theIntColor = old.backgroundC
    dialog.isAdvanceOpen = true
    dialog.buildAndDisplay()
}


---------------------

searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                whenSearchClick()
                true
            }
            false
        }

*/


/*
scale images

private fun setScaleFunctionality(){
#https://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures/12938787#12938787
        val mScaleGestureDetector = ScaleGestureDetector(context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector?): Boolean {
                    println("--${detector?.scaleFactor}")
                    if(detector != null){
                        val scale = detector.scaleFactor
                        val delta = 0.05
                        if(scale > 1 + delta || scale < 1 - delta){
                            mScaleFactor *= detector.scaleFactor
                            imageView.scaleX = mScaleFactor
                            imageView.scaleY = mScaleFactor
                        }
                    }
                    return true
                }
            }
        )

        imageView.setOnTouchListener { _, event ->
            mScaleGestureDetector.onTouchEvent(event)
            true
        }
    }
 */


//endregion