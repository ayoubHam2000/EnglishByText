package com.example.englishbytext.Classes.Objects

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Adapters.A_def_exp
import com.example.englishbytext.Adapters.A_imageMedia
import com.example.englishbytext.Dialogs.D_editItem
import com.example.englishbytext.Dialogs.MyDialogBuilder
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.OnSelectMode
import com.example.englishbytext.Utilites.OpenDefinition
import com.example.englishbytext.Utilites.OpenExample


class D_WordInfo(context : Context, val wordName : String) : MyDialogBuilder(context, R.layout.f_word_edit) {

    //region init
    private lateinit var definitionAdapter : A_def_exp
    private lateinit var exampleAdapter : A_def_exp
    private lateinit var imagesAdapter : A_imageMedia

    private lateinit var parentLayout : RelativeLayout
    private lateinit var wordNameTextView : TextView
    private lateinit var defRV : RecyclerView
    private lateinit var expRV : RecyclerView
    private lateinit var imgRV : RecyclerView
    private lateinit var audRV : RecyclerView
    private lateinit var addDef : ImageView
    private lateinit var addExp : ImageView
    private lateinit var addImg : ImageView
    private lateinit var addAud : ImageView

    override fun initView(builderView: View) {

        parentLayout = builderView.findViewById(R.id.parentLayout)
        wordNameTextView = builderView.findViewById(R.id.wordName)
        defRV = builderView.findViewById(R.id.defRV)
        expRV = builderView.findViewById(R.id.expRV)
        imgRV = builderView.findViewById(R.id.imgRv)
        audRV = builderView.findViewById(R.id.audRV)
        addDef = builderView.findViewById(R.id.addDef)
        addExp = builderView.findViewById(R.id.addExample)
        addImg = builderView.findViewById(R.id.addImage)
        addAud = builderView.findViewById(R.id.addAudio)


        dialog.setOnShowListener {
            intFun()
        }
        dialog.window?.setBackgroundDrawableResource(R.color.transparentForWordInfo)
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        dialog.window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

    }

    private fun intFun(){
        wordNameTextView.text = wordName
        parentLayout.setBackgroundColor(context.getColor(R.color.transparent2))
        initDefinitionRv()
        initExampleRv()
        initImagesRV()
        initAudioRV()
        addDef.setOnClickListener { addDef() }
        addExp.setOnClickListener { addExp() }
        addImg.setOnClickListener { addImage() }
        addAud.setOnClickListener { addAudio() }
    }
    //endregion

    //region definition, examples
    //region definition RV

    private fun initDefinitionRv(){
        definitionAdapter = A_def_exp(context, wordName, OpenDefinition)


        val layout = LinearLayoutManager(context)
        definitionAdapter.changeList()
        defRV.layoutManager = layout
        defRV.adapter = definitionAdapter

        val itemTouchHelper = ItemTouchHelper(getItemTouchDeleter(OpenDefinition))
        itemTouchHelper.attachToRecyclerView(defRV)
    }

    private fun addDef(){
        var defDialog : D_editItem? = null
        defDialog = D_editItem(context){
            DataBaseServices.insertDefinition(wordName, it)
            defDialog!!.dismiss()
            definitionAdapter.changeList()
        }
        defDialog.textHint = context.getString(R.string.add_definition)
        defDialog.buildAndDisplay()
    }

    //endregion

    //region example RV

    private fun initExampleRv(){
        exampleAdapter = A_def_exp(context, wordName, OpenExample)

        val layout = LinearLayoutManager(context)
        exampleAdapter.changeList()
        expRV.layoutManager = layout
        expRV.adapter = exampleAdapter

        val itemTouchHelper = ItemTouchHelper(getItemTouchDeleter(OpenExample))
        itemTouchHelper.attachToRecyclerView(expRV)
    }

    private fun addExp(){
        var exampleDialog : D_editItem? = null
        exampleDialog = D_editItem(context){
            DataBaseServices.insertExamples(wordName, it, exampleAdapter.selectedCollection)
            exampleDialog!!.dismiss()
            exampleAdapter.changeList()
        }
        exampleDialog.textHint = context.getString(R.string.add_example)
        exampleDialog.buildAndDisplay()
    }

    //endregion

    //region delete item

    private fun getItemTouchDeleter(type : Int) : ItemTouchHelper.SimpleCallback{
        return object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when(type){
                    OpenDefinition ->{
                        val id = definitionAdapter.list[viewHolder.adapterPosition].id
                        println("Remove Definition N ${viewHolder.adapterPosition} | id -> $id")
                        removeDefinition(id)
                        definitionAdapter.changeList()
                    }
                    OpenExample ->{
                        val id = exampleAdapter.list[viewHolder.adapterPosition].id
                        println("Remove Example N ${viewHolder.adapterPosition} | id -> $id")
                        removeExample(id)
                        exampleAdapter.changeList()
                    }
                }

            }
        }
    }

    private fun removeExample(id : Int){
        DataBaseServices.deleteExample(id)
    }

    private fun removeDefinition(id : Int){
        DataBaseServices.deleteDefinition(id)
    }

    //endregion
    //endregion

    //region images
    fun notifyImageAdapter(){
        imagesAdapter.changeList()
    }
    private fun addImage(){
        println(">>>ADD IMAGE")
        //event(AddImage)
    }

    private fun initImagesRV(){
        imagesAdapter = A_imageMedia(context, wordName){ event ->
            recyclerViewImageEvent(event)
        }

        val layout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        imagesAdapter.changeList()
        imgRV.layoutManager = layout
        imgRV.adapter = imagesAdapter
    }

    private fun recyclerViewImageEvent(event : Int){
        when(event){
            OnSelectMode -> selectMode()
        }
    }

    private fun selectMode(){
        addImg.setBackgroundResource(R.drawable.ic_delete_24)
        addImg.setOnClickListener {
            println("delete")
        }
    }

    //endregion

    //region Audio

    private fun addAudio(){
        println(">>> Add audio")
    }

    private fun initAudioRV(){

    }

    //endregion

}