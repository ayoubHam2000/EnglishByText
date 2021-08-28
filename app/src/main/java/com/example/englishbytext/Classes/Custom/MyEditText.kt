package com.example.englishbytext.Classes.Custom

import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.TextView


class MyEditText : androidx.appcompat.widget.AppCompatEditText {

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onTextContextMenuItem(id: Int): Boolean {
        val consumed =  super.onTextContextMenuItem(id)
        when(id){
            android.R.id.paste -> {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val text = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
                val format = text?.replace("\\n[a-z]".toRegex(), " ")
                println("-- $text")
                setText(format)
            }
        }
        return consumed
    }



}
