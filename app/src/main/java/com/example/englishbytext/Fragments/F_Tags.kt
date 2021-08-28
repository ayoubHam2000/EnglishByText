package com.example.englishbytext.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Adapters.A_tag
import com.example.englishbytext.Classes.Objects.D_ask
import com.example.englishbytext.Dialogs.D_editItem
import com.example.englishbytext.Interfaces.NotifyActivity
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.WordsManagement
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.*

class F_Tags : Fragment() {

    //region init
    private var listener : NotifyActivity? = null
    private lateinit var gContext : Context
    private val layout = R.layout.f_tags
    lateinit var tagAdapter : A_tag

    //view
    private lateinit var navController : NavController
    private lateinit var addBtn : ImageView
    private lateinit var deleteBtn : ImageView
    private lateinit var tagRv : RecyclerView

    private fun initFun(view : View){
        listener?.notifyActivity(OpenTagFg)
        navController = Navigation.findNavController(view)

        addBtn = activity?.findViewById(R.id.addTag)!!
        deleteBtn = activity?.findViewById(R.id.deleteTag)!!
        tagRv = view.findViewById(R.id.tagRv)

        intFun()
    }

    private fun intFun(){
        addBtn.setOnClickListener { addTag() }
        deleteBtn.setOnClickListener { deleteTags() }
        initRv()
    }



    //endregion

    //region ragRv

    private fun addTag(){
        var dialog : D_editItem? = null
        dialog = D_editItem(gContext){tag->
            when{
                DataBaseServices.isTagNotExist(tag) ->{
                    DataBaseServices.insertTag(tag)
                    tagAdapter.changeList()
                    dialog!!.dismiss()
                }
                else->{
                    dialog!!.inputName.error = "Tag Already Exist In the List"
                }
            }
        }
        dialog.textHint = "Tag"
        dialog.maxChar = MaxTagChars
        dialog.buildAndDisplay()
    }

    private fun deleteTags(){
        val ask = D_ask(gContext, "ARE YOU SURE ?"){
            if(it){
                DataBaseServices.deleteTags(tagAdapter.getSelectedIds())
                deaSelectTag()
            }
        }
        ask.buildAndDisplay()
    }

    private fun initRv(){
        val layout = GridLayoutManager(gContext, 2)
        tagAdapter = A_tag(gContext){event, item ->
            rvEvent(event, item)
        }

        tagAdapter.changeList()
        tagRv.adapter = tagAdapter
        tagRv.layoutManager = layout
    }

    private fun rvEvent(event : Int, item : String){
        when(event){
            OnSelectMode -> activeDeleteTags()
            OpenItem -> openTag(item)
        }
    }

    private fun openTag(tag : String){
        val bundle = Bundle()

        bundle.putString(FgType, "Tags")
        bundle.putString(PassedData, tag)
        navController.navigate(R.id.f_WordsList, bundle)
    }

    private fun activeDeleteTags(){
        addBtn.visibility = View.GONE
        deleteBtn.visibility = View.VISIBLE
    }

    private fun activeAddTag(){
        addBtn.visibility = View.VISIBLE
        deleteBtn.visibility = View.GONE
    }

    //endregion

    //region functions

    fun deaSelectTag() : Boolean{
        activeAddTag()
        return tagAdapter.deaSelect()
    }

    //endregion

    //region override
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? NotifyActivity
        if (listener == null) {
            throw ClassCastException("$context must implement OnArticleSelectedListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(layout, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gContext = view.context
        initFun(view)
    }

    //endregion

}