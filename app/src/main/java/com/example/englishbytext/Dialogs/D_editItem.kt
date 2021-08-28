package com.example.englishbytext.Dialogs

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import com.example.englishbytext.R


class D_editItem(context: Context, val event: (String) -> Unit) :
        MyDialogBuilder(context, R.layout.d_input) {

    lateinit var inputName : AutoCompleteTextView
    var maxLine = -1
    var textInput = ""
    var textHint = ""
    var maxChar = -1
    var listSuggestion = ArrayList<String>()
    private val message1 = "input is empty"


    override fun initView(builderView: View) {
        val add = builderView.findViewById<ImageView>(R.id.d_add)
        inputName = builderView.findViewById(R.id.InputName)
        setListSuggestion()

        dialog.setOnShowListener {
            setView()
            add.setOnClickListener { addItem(inputName) }
            //Lib.dialogMotivateKeyboard(context, 100)
        }
        dialog.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setOnCancelListener {
            maxChar = -1
            inputName.setText("")
        }
    }

    private fun setListSuggestion(){
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(context,
                android.R.layout.simple_dropdown_item_1line, listSuggestion)
        inputName.setAdapter(adapter)
    }

    private fun setView(){
        defineMaxChar(inputName)
        inputName.hint = if(textHint.isEmpty()) context.getString(R.string.write_something_here) else textHint
        if(maxLine != -1) {
            inputName.inputType = InputType.TYPE_CLASS_TEXT
            inputName.maxLines = maxLine
        }
        if(textInput.isNotEmpty()){
            inputName.setText(textInput)
            inputName.setSelection(inputName.text.count())
        }
        inputName.requestFocus()
        //Lib.showKeyboardToDialog(dialog)
        inputName.setSelection(inputName.text.count())
    }

    private fun defineMaxChar(editText: EditText){
        if(maxChar != -1){
            editText.addTextChangedListener(
                    object : TextWatcher {
                        override fun afterTextChanged(s: Editable) {
                            if (s.count() > maxChar) {
                                editText.error = "Max character is $maxChar"
                            }
                        }

                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                    }
            )
        }
    }

    private fun addItem(input: EditText){
        val text = input.text.trim()
        if(text.isNotEmpty()){
            event(text.toString())
        }else{
            input.error = message1
        }
    }

}