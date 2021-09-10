package com.example.englishbytext.Classes.Objects

import android.content.Context
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.WindowManager
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import com.example.englishbytext.Dialogs.MyDialogBuilder
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.Objects.WordsManagement
import com.example.englishbytext.R
import java.lang.Exception
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class D_wordFrequency(context: Context) : MyDialogBuilder(context, R.layout.d_word_frequency) {

    private lateinit var close : ImageView
    private lateinit var editText : AutoCompleteTextView
    private lateinit var result : TextView

    var list = ArrayList<String>()

    override fun initView(builderView: View) {
        dialog.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        )

        close = builderView.findViewById(R.id.d_dismiss)
        editText = builderView.findViewById(R.id.InputName)
        result = builderView.findViewById(R.id.resultTextView)

        result.movementMethod = ScrollingMovementMethod()

        initList()
        onSearch()

        close.setOnClickListener {
            dismiss()
        }

    }

    private fun initList() {
        val l = ArrayList(WordsManagement.wordsFrequency.keys.toList())
        l.sortWith { lhs, rhs ->
            val i1 = lhs.count()
            val i2 = rhs.count()
            when {
                i1 < i2 -> -1
                i1 > i2 -> 1
                else -> 0
            }
        }
        list = l
    }


    private fun onSearch(){
        editText.addTextChangedListener(
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable) {
                        val theWord = s.trim()
                        if(theWord.isNotEmpty() && theWord.count() > 1){
                            thread {
                                val t = theWord.toString()
                                val res = setResult(t)
                                Handler(context.mainLooper).post{
                                    if(t == editText.text.toString())
                                        result.text = res
                                }
                            }
                        }else{
                            result.text = ""
                        }
                    }

                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                }
        )
    }

    private fun setResult(word : String) : String{
        val capacity = 50
        val res = ArrayList<String>(capacity)
        try {
            val regex = Regex("^$word.*")
            for(item in list){
                if(res.count() >= capacity) break
                if(item.matches(regex)){
                    res.add("$item : ${WordsManagement.getWordFrequency(item)}")
                }
            }
        }catch (e : Exception){
            Lib.showMessage(context, "Regex Error")
        }
        return res.joinToString("\n")
    }

}