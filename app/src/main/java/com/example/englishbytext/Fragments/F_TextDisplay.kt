package com.example.englishbytext.Fragments

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.Selection
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.*
import android.widget.*
import com.example.englishbytext.Classes.Custom.MySelectMenu
import com.example.englishbytext.Classes.schemas.WordPosItem
import com.example.englishbytext.Objects.*
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.FgType
import com.example.englishbytext.Utilites.OpenTextDisplayFrag
import com.example.englishbytext.Utilites.PassedData


class F_TextDisplay : MyFragment() {

    //region Int
    //====================================
    //++++++++++++++++++++++  Views
    //====================================
    private lateinit var titleView : TextView
    private lateinit var textView : TextView
    private lateinit var textScroller : ScrollView
    private lateinit var selectionAction : LinearLayout
    private lateinit var menuView : ImageView

    //====================================
    //++++++++++++++++++++++  Init
    //====================================
    override fun getMainLayout(): Int {
        return R.layout.f_text_display
    }

    override fun getNotifyListenerId(): Int {
        return OpenTextDisplayFrag
    }

    override fun initVar(view: View) {
        titleView = view.findViewById(R.id.titleView)
        textView = view.findViewById(R.id.textView)
        textScroller = view.findViewById(R.id.textScroller)
        selectionAction = view.findViewById(R.id.selectionAction)
        menuView = view.findViewById(R.id.menuView)
    }

    override fun initFun() {
        initMenu()
        setLayout()
    }


    //endregion

    //region fragment

    private fun setLayout(){
        val t = TextManagement.getText()!!
        titleView.text = t.title
        textView.requestFocus()
        setTextView()
        setSelectableWord()

        Handler(gContext.mainLooper).postDelayed({
            val orientation = resources.configuration.orientation
            val oldPosition = if (orientation == 1) {
                DataBaseServices.getTextPosX(t.id)
            } else {
                DataBaseServices.getTextPosY(t.id)
            }
            textScroller.scrollTo(0, oldPosition)

        }, 200)

    }

    //region Menu

    private fun initMenu(){
        val menu =  Lib.initPopupMenu(gContext, menuView, R.menu.m_displayed_text)

        menu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.listOfWords -> openListOfWords()
            }
            true
        }
    }

    private fun openListOfWords(){
        val bundle = Bundle()

        bundle.putString(FgType, "Text")
        bundle.putString(PassedData, TextManagement.getText()?.title)
        navController.navigate(R.id.f_WordsList, bundle)
    }

    //endregion

    //endregion

    //region selectMenu

    @SuppressLint("ClickableViewAccessibility")
    private fun setSelectableWord(){
        initSelectionBar()
        textView.setOnTouchListener { _, event ->
            if (MotionEvent.ACTION_UP == event.action) {
                if (textView.hasSelection()) {
                    selectionAction.visibility = View.VISIBLE
                }else{
                    selectionAction.visibility = View.GONE
                }
            }
            false
        }

        textView.customSelectionActionModeCallback = MySelectMenu{
            selectionAction.visibility = View.GONE
        }
    }

    private fun initSelectionBar(){
        val add = selectionAction.getChildAt(0) as TextView
        val copy = selectionAction.getChildAt(1) as TextView
        val delete = selectionAction.getChildAt(2) as TextView
        val selectAll = selectionAction.getChildAt(3) as TextView
        val findWords = selectionAction.getChildAt(4) as TextView

        add.setOnClickListener { addWord() }
        delete.setOnClickListener { deleteWords() }
        copy.setOnClickListener { copyAction() }
        selectAll.setOnClickListener { selectAllAction() }
        findWords.setOnClickListener { findWords() }
    }

    private fun findWords(){
        val mainText = textView.text.toString()
        val start = textView.selectionStart
        val end = textView.selectionEnd
        val i = DataBaseServices.findAndInsertTextWordPos(mainText, start, end)
        selectionAction.visibility = View.GONE
        Lib.showMessage(gContext, "$i added")
        setTextView()
    }

    private fun addWord(){
        val start = textView.selectionStart
        val end = textView.selectionEnd
        val selected = textView.text.substring(start, end)
        val wordPosItem = WordPosItem(start, end, selected)

        if(isNotSubSelected(start, end)){
            DataBaseServices.insertWord(selected)
            DataBaseServices.insertTextPos(TextManagement.selectedItem, wordPosItem)
            setTextView()
        }else{
            Lib.showMessage(gContext, "Already Selected")
        }
        selectionAction.visibility = View.GONE
    }

    private fun deleteWords(){
        val deleted = ArrayList<WordPosItem>()
        val id = TextManagement.selectedItem
        val start = textView.selectionStart
        val end = textView.selectionEnd

        selectedIteration{ s, e ->
            if((s >= start && e <= end)){
                deleted.add(WordPosItem(s, e, ""))
                //println("--->($s, $e, ${textView.text.substring(s, e)})")
            }
        }
        DataBaseServices.deleteTextWordPos(id, deleted)
        setTextView()
        selectionAction.visibility = View.GONE
    }

    private fun copyAction(){
        val s = textView.selectionStart
        val e = textView.selectionEnd
        val str = textView.text.subSequence(s, e).toString()
        Lib.copyContent(gContext, "TextCopy", str)
        selectionAction.visibility = View.GONE
        Selection.setSelection(textView.text as Spannable, textView.selectionStart)
    }

    private fun selectAllAction(){
        val text = textView.text as Spannable
        Selection.selectAll(text)
    }

    //endregion

    //region clickable Word

    private fun setTextView() {
        TextManagement.getListOfText()
        val t = TextManagement.getText()!!
        val content = t.text


        textStyle()
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.setText(content, TextView.BufferType.SPANNABLE)
        val spans = textView.text as Spannable

        selectedIteration{ s, e->
            val target = content.substring(s, e)
            val clickSpan: ClickableSpan = getClickableSpan(target)

            spans.setSpan(clickSpan, s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun textStyle(){
        val size = 16 * MainSetting.getTextSize()
        val typeface = Typeface.create(MainSetting.getTextFont(), MainSetting.getFontsType())

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
        textView.typeface = typeface
    }

    private fun getClickableSpan(wordName: String) : ClickableSpan {
        return object : ClickableSpan() {

            override fun onClick(textView: View) {
                selectionAction.visibility = View.GONE
                openWordEdit(wordName)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = gContext.getColor(R.color.spanWords)
            }

        }
    }

    private fun openWordEdit(w: String){
        //WordsManagement.selectedWordName = w
        val bundle = Bundle()
        bundle.putString("WORD_NAME", w)
        navController.navigate(R.id.action_f_TextDisplay_to_f_WordEdit, bundle)
    }

    //endregion

    //region Methods

    private fun selectedIteration(event: (Int, Int) -> Unit){
        //iterate the selected items at the target text
        val wordsPos = TextManagement.getWordPos()

        for(item in wordsPos){
            event(item.s, item.e)
        }
    }

    private fun isNotSubSelected(start: Int, end: Int) : Boolean{
        val wordsPos = TextManagement.getWordPos()

        for(item in wordsPos){
            val s = item.s
            val e = item.e
            if(start in s..e || end in s..e)
                return false
        }
        return true
    }



    //endregion

    //region override

    override fun onPause() {
        super.onPause()
        val pos = textScroller.scrollY
        val orientation = resources.configuration.orientation
        if(orientation == 1){
            DataBaseServices.updateTextPosX(TextManagement.selectedItem, pos)
        }else{
            DataBaseServices.updateTextPosY(TextManagement.selectedItem, pos)
        }
        val s = textView.selectionStart
        val e = textView.selectionEnd
        if(s in 1 until e){
            val str = textView.text.subSequence(s, e).toString()
            Lib.copyContent(gContext, "TextCopy", str)
            Toast.makeText(context, "$str copied", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onStart() {
        super.onStart()

        if(textView.hasSelection()){
            selectionAction.visibility = View.VISIBLE
        }
    }


    //endregion
}