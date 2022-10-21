package com.example.englishbytext.Adapters

import android.content.Context
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Classes.Custom.MyForegroundColorSpan
import com.example.englishbytext.Classes.schemas.StringId
import com.example.englishbytext.Classes.schemas.WordInfoId
import com.example.englishbytext.Dialogs.D_editItem
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.OpenDefinition
import com.example.englishbytext.Utilites.OpenExample

class A_def_exp(val context : Context, val wordName : String, val type : Int) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val layout = R.layout.a_wordedit_info_item
    val list = ArrayList<WordInfoId>()
    var selectedCollection = 0

    fun changeList(){
        list.clear()
        when(type){
            OpenDefinition->{
                val l = DataBaseServices.getWordDefinitions(wordName)
                l.sortBy{it.value}
                list.addAll(l)
            }
            OpenExample->{
                val l = DataBaseServices.getWordExamples(wordName, selectedCollection)
                l.sortBy{it.value}
                list.addAll(l)
            }
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView : View?) : RecyclerView.ViewHolder(itemView!!){
        private val item = itemView!!.findViewById<TextView>(R.id.itemInfo)

        fun bindView(position : Int){
            val relatedWord = list[position].word
            val theSpanRelated = ForegroundColorSpan(context.getColor(R.color.relatedWord))


            if(list[position].word != wordName){
                item.movementMethod = LinkMovementMethod.getInstance()

                val text = list[position].word + ": " + list[position].value
                item.setText(text, TextView.BufferType.SPANNABLE)
                val spans = item.text as Spannable


                spans.setSpan(theSpanRelated, 0, relatedWord.length + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else{
                item.setText(list[position].value, TextView.BufferType.SPANNABLE)
                val spans = item.text as Spannable

                val theWord = list[position].word
                var pos = item.text.indexOf(theWord, ignoreCase=true)
                while (pos != -1)
                {
                    val theSpanExample = ForegroundColorSpan(context.getColor(R.color.exampleWord))
                    spans.setSpan(theSpanExample, pos, pos + theWord.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    pos = item.text.indexOf(theWord, pos + theWord.length, ignoreCase=true)
                }
            }

            item?.setOnClickListener {
                when(type){
                    OpenExample -> editExample(position)
                    OpenDefinition -> editDefinition(position)
                }
            }
            item?.setOnLongClickListener {
                Lib.copyContent(context, "ExpDef", list[position].value)
                Lib.showMessage(context, "Copied")
                true
            }
        }

        private fun editExample(position: Int){
            var exampleDialog : D_editItem? = null

            exampleDialog = D_editItem(context){
                val id = list[position].id

                DataBaseServices.updateWordExample(id, it)
                exampleDialog!!.dismiss()
                changeList()
            }
            exampleDialog.textHint = context.getString(R.string.add_example)
            exampleDialog.textInput = list[position].value
            exampleDialog.buildAndDisplay()
        }

        private fun editDefinition(position: Int){
            var definitionDialog : D_editItem? = null

            definitionDialog = D_editItem(context){
                val id = list[position].id

                DataBaseServices.updateWordDefinition(id, it)
                definitionDialog!!.dismiss()
                changeList()
            }
            definitionDialog.textHint = context.getString(R.string.add_definition)
            definitionDialog.textInput = list[position].value
            definitionDialog.buildAndDisplay()
        }

    }

    //region override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(layout, parent, false)
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