package com.example.englishbytext.Fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.view.children
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Adapters.A_WordList
import com.example.englishbytext.Classes.Objects.D_ask
import com.example.englishbytext.Dialogs.D_editItem
import com.example.englishbytext.Interfaces.NotifyActivity
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.Objects.Setting
import com.example.englishbytext.Objects.WordsManagement
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.*
import java.lang.Exception
import kotlin.concurrent.thread


class F_WordsList : Fragment() {

    //region init
    private var listener : NotifyActivity? = null
    private lateinit var gContext : Context
    private lateinit var wordListAdapter : A_WordList
    private val layout = R.layout.f_words_list
    lateinit var popupMenu : PopupMenu
    private var continueSearch = true
    private var oldSearch = ""
    var selectedTag = ""

    //view
    private lateinit var navController : NavController
    private lateinit var wordListRV : RecyclerView
    private lateinit var wordListSearch : EditText
    private lateinit var addWordList : ImageView
    private lateinit var filterMode : LinearLayout
    private lateinit var deleteWords : ImageView
    private lateinit var makeFavorite : ImageView

    private lateinit var favoriteActiveLabel : TextView
    private lateinit var regexActiveLabel : TextView

    private fun initFun(view : View){
        listener?.notifyActivity(OpenWordList)
        navController = Navigation.findNavController(view)

        wordListSearch = activity?.findViewById(R.id.wordListSearch)!!
        addWordList = activity?.findViewById(R.id.addWordList)!!
        filterMode = activity?.findViewById(R.id.filterMode)!!
        deleteWords = activity?.findViewById(R.id.deleteWords)!!
        makeFavorite = activity?.findViewById(R.id.makeFavorite)!!
        favoriteActiveLabel = activity?.findViewById(R.id.favoriteActiveLabel)!!
        regexActiveLabel = activity?.findViewById(R.id.regexActiveLabel)!!

        wordListRV = view.findViewById(R.id.wordListRV)
        intFun()
    }

    private fun intFun(){
        initWordListRV()
        initActionBar()
        startSearch(wordListSearch.text.toString())
    }

    //endregion

    //region actionBar

    private fun initActionBar(){
        onSelectMode(false)

        popupMenu = Lib.initPopupMenu(gContext, filterMode,R.menu.m_filter_mode)
        filterModeSetUp()

        wordListSearch.addTextChangedListener(searchWords())
        wordListSearch.setOnEditorActionListener(actionDone())
        if(oldSearch.isNotEmpty()) wordListSearch.setText(oldSearch)

        addWordList.setOnClickListener { addWordList() }
        deleteWords.setOnClickListener { deleteWords() }
        makeFavorite.setOnClickListener { makeItFavorite() }
    }

    private fun addWordList(){
        Lib.printLog("addWordItem")
        var dialogEditItem : D_editItem? = null
        dialogEditItem = D_editItem(gContext){
            if(DataBaseServices.isWordNotExist(it)){
                DataBaseServices.insertWord(it)
                if(selectedTag.isNotEmpty()){
                    DataBaseServices.insertWordTag(it, selectedTag)
                }
                wordListAdapter.changeList()
                dialogEditItem?.dismiss()
            }else{
                dialogEditItem?.inputName?.error = "This word is already exist"
            }
        }
        dialogEditItem.textHint = gContext.getString(R.string.addWord)
        dialogEditItem.buildAndDisplay()
    }

    private fun deleteWords(){
        println("--Delete Words")
        val mainPath = gContext.getExternalFilesDir("/")!!.absolutePath
        if(selectedTag.isNotEmpty()){
            val ask = D_ask(gContext, "Delete From ?"){
                val list = wordListAdapter.getSelected()
                if(it){
                    DataBaseServices.deleteWordsFromTag(selectedTag, list)
                }else{
                    DataBaseServices.deleteWords(mainPath, list)
                }
                deaSelectMode()
                wordListAdapter.changeList()
            }
            ask.approveText = "From Tag"
            ask.denyText = "Permanently"
            ask.buildAndDisplay()
        }else{
            val ask = D_ask(gContext, "ARE YOU SURE ?"){
                if(it){
                    val list = wordListAdapter.getSelected()
                    DataBaseServices.deleteWords(mainPath, list)
                    deaSelectMode()
                    wordListAdapter.changeList()
                }
            }
            ask.buildAndDisplay()
        }

    }

    private fun makeItFavorite(){
        println("--Favorite Words")
        val list = wordListAdapter.getSelected()
        DataBaseServices.updateIsFavoriteWords(list)
        deaSelectMode()
        wordListAdapter.changeList()
        startSearch(wordListSearch.text.toString())
    }

    private fun filterModeSetUp(){
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.isFavorite -> {
                    val item = popupMenu.menu.getItem(0)
                    item.isChecked = !item.isChecked
                    Setting.setFavoriteSearch(item.isChecked.toString())
                    favoriteActiveLabel.visibility = if(Setting.onFavoriteSearch) View.VISIBLE else View.GONE
                }
                R.id.isOnRegex -> {
                    val item = popupMenu.menu.getItem(1)
                    item.isChecked = !item.isChecked
                    Setting.setRegexSearch(item.isChecked.toString())
                    regexActiveLabel.visibility = if(Setting.onRegexSearch) View.VISIBLE else View.GONE
                }
            }
            wordListAdapter.notifyDataSetChanged()
            startSearch(wordListSearch.text.toString())

            true
        }
        popupMenu.menu.getItem(0).isChecked = Setting.onFavoriteSearch
        popupMenu.menu.getItem(1).isChecked = Setting.onRegexSearch
        favoriteActiveLabel.visibility = if(Setting.onFavoriteSearch) View.VISIBLE else View.GONE
        regexActiveLabel.visibility = if(Setting.onRegexSearch) View.VISIBLE else View.GONE
    }

    //region search
    private fun actionDone() : TextView.OnEditorActionListener{
        return TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val countList = wordListAdapter.itemCount
                val message = gContext.getString(R.string.elements_found)
                Lib.showMessage(gContext, "$countList $message")
            }
            false
        }
    }

    private fun searchWords() : TextWatcher{
        return object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(continueSearch){
                    thread{
                        continueSearch = false
                        if(s != null){
                            startSearch(wordListSearch.text.toString())
                            Handler(gContext.mainLooper).post {
                                wordListAdapter.notifyDataSetChanged()
                                continueSearch = true
                            }
                        }else{
                            continueSearch = true
                        }
                    }
                }
                oldSearch = wordListSearch.text.toString()
            }
        }
    }

    private fun startSearch(s : String){
        val favoriteOn = popupMenu.menu.getItem(0)
        val regexOn = popupMenu.menu.getItem(1)
        if(regexOn.isChecked){
            if(isValidRegex(s)){
                wordListAdapter.filterSearch(s, true, favoriteOn.isChecked)
            }else{
                Handler(gContext.mainLooper).post {
                    wordListSearch.error = "Invalid regex"
                }
            }
        }else{
            wordListAdapter.filterSearch(s, false, favoriteOn.isChecked)
        }
    }

    private fun isValidRegex(regex : String) : Boolean{
        return try{
            regex.toRegex()
            true
        }catch (e : Exception){
            false
        }
    }

    //endregion

    //endregion

    //region RV

    private fun initWordListRV(){
        val layoutManager = LinearLayoutManager(gContext)
        wordListAdapter = A_WordList(gContext, selectedTag){event, wordName ->
            recyclerViewEvent(event, wordName)
        }
        wordListAdapter.changeList()

        wordListRV.adapter = wordListAdapter
        wordListRV.layoutManager = layoutManager

    }

    private fun recyclerViewEvent(event : Int, word : String){
        when(event){
            OpenItem -> openItem(word)
            OnSelectMode -> onSelectMode(true)
        }
    }

    private fun openItem(word : String){
        val bundle = Bundle()
        bundle.putString("WORD_NAME", word)
        Lib.hideKeyboardFrom(gContext, wordListSearch)
        navController.navigate(R.id.action_f_WordsList_to_f_WordEdit, bundle)
    }

    private fun onSelectMode(on : Boolean){
        addWordList.visibility = if(!on) View.VISIBLE else View.GONE
        filterMode.visibility = if(!on) View.VISIBLE else View.GONE
        wordListSearch.visibility = if(!on) View.VISIBLE else View.GONE
        deleteWords.visibility = if(on) View.VISIBLE else View.GONE
        makeFavorite.visibility = if(on) View.VISIBLE else View.GONE
        Lib.hideKeyboardFrom(gContext, wordListSearch)
    }

    //endregion

    //region fragment

    fun deaSelectMode() : Boolean{
        onSelectMode(false)
        return wordListAdapter.deaSelectMode()
    }

    fun notifyList(){
        wordListAdapter.changeList()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if(bundle != null){
            selectedTag = bundle.getString("SELECTED_TAG")!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gContext = view.context

        initFun(view)
    }

    //endregion

}