package com.example.englishbytext.Fragments

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.englishbytext.Classes.Custom.MyForegroundColorSpan
import com.example.englishbytext.Classes.Objects.D_ask
import com.example.englishbytext.Classes.schemas.WordPosItem
import com.example.englishbytext.Interfaces.NotifyActivity
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.Objects.TextManagement
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.OpenTextEditFrag
import com.example.englishbytext.Utilites.SaveChanges


class F_EditText : MyFragment() {

    //region var, int

    //====================================
    //++++++++++++++++++++++  vars
    //====================================
    private var theTextId : Int = -1
    private var saveDone = false


    //====================================
    //++++++++++++++++++++++  view
    //====================================
    private lateinit var titleEditor : EditText
    private lateinit var textEditor : EditText


    //====================================
    //++++++++++++++++++++++  init
    //====================================
    override fun getMainLayout(): Int {
        return R.layout.f_edit_text
    }

    override fun getNotifyListenerId(): Int {
        return OpenTextEditFrag
    }

    override fun initVar(view: View) {
        titleEditor = view.findViewById(R.id.titleEditor)
        textEditor = view.findViewById(R.id.textEditor)
    }

    override fun initFun() {
        theTextId = TextManagement.selectedItem
        setLayout()
    }


    //endregion

    //region Layout

    private fun setLayout(){
        val t = TextManagement.getText()!!

        titleEditor.setText(t.title)
        textEditor.setText(t.text)
        setSpanText()
    }

    private fun setSpanText(){
        val t = TextManagement.getText()!!
        val content = t.text
        val wordsPos = TextManagement.getWordPos()

        textEditor.movementMethod = LinkMovementMethod.getInstance()
        textEditor.setText(content, TextView.BufferType.SPANNABLE)
        val spans = textEditor.text as Spannable

        for(item in wordsPos){
            val s = item.s
            val e = item.e
            val str = content.substring(s, e)
            val clickSpan = MyForegroundColorSpan(gContext.getColor(R.color.colorPrimary), str)

            spans.setSpan(clickSpan, s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    //endregion

    //region Changes

    private fun saveChanges(){
        val id = theTextId
        val title = titleEditor.text.toString().trim()
        val text = textEditor.text.toString().trim()

        TextManagement.updateWordPos(getUpdatedWordsPos())
        DataBaseServices.updateTextTitle(id, title)
        DataBaseServices.updateTextText(id, text)
        Lib.showMessage(gContext, "Save Successful")
    }

    fun askSaveChanges() : Boolean{
        if(saveDone || !isThereIsSomeChanges())
            return false
        val ask = D_ask(gContext, "SAVE CHANGES ?"){
            if(it){
                saveChanges()
            }
            saveDone = true
            listener?.notifyActivity(SaveChanges)
        }
        ask.buildAndDisplay()
        return true
    }

    private fun isThereIsSomeChanges() : Boolean{
        val t = TextManagement.getText()!!
        val title = titleEditor.text.toString().trim()
        val text = textEditor.text.toString().trim()

        if(t.title != title) return true
        if(t.text != text) return true
        return false
    }

    //endregion

    //region Methods

    private fun getUpdatedWordsPos() : ArrayList<WordPosItem>{
        val spans = textEditor.text as Spannable
        val res = ArrayList<WordPosItem>()
        val obs = spans.getSpans(0, textEditor.text.toString().length, MyForegroundColorSpan::class.java)
        for (ob in obs) {
            val s = spans.getSpanStart(ob)
            val e = spans.getSpanEnd(ob)
            val str = textEditor.text.toString().substring(s, e)
            val oldStr = (ob as MyForegroundColorSpan).value
            if(str == oldStr)
                res.add(WordPosItem(s, e, str))
        }
        return res
    }

    //endregion


}