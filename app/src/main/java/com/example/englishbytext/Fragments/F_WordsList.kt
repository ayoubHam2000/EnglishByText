package com.example.englishbytext.Fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Adapters.A_WordList
import com.example.englishbytext.Classes.Objects.D_ask
import com.example.englishbytext.Classes.Objects.D_copy_to_folder
import com.example.englishbytext.Dialogs.D_editItem
import com.example.englishbytext.Interfaces.NotifyActivity
import com.example.englishbytext.Objects.*
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.*
import java.lang.Exception
import kotlin.concurrent.thread


class F_WordsList : MyFragment() {

    //region init
    /*
    fgType -> addWordList, deleteWords
     */
    //====================================
    //++++++++++++++++++++++  Vars
    //====================================
    private lateinit var wordListAdapter : A_WordList
    lateinit var popupMenu : PopupMenu
    lateinit var dialogEditItem : D_editItem
    //search
    private var continueSearch = true
    private var oldSearch = ""
    //used by previous fragment (tags, folders)
    var fgType = ""
    var passedData = ""

    //====================================
    //++++++++++++++++++++++  Views
    //====================================
    private lateinit var pathView : TextView
    private lateinit var wordListRV : RecyclerView
    private lateinit var wordListSearch : EditText
    private lateinit var addWordList : ImageView
    private lateinit var practiceBtn : ImageView
    private lateinit var filterMode : LinearLayout
    private lateinit var deleteWords : ImageView
    private lateinit var makeFavorite : ImageView
    private lateinit var copyToFolder : ImageView
    private lateinit var selectAll : ImageView
    private lateinit var updateOrderView : ImageView
    private lateinit var favoriteActiveLabel : TextView
    private lateinit var regexActiveLabel : TextView

    //====================================
    //++++++++++++++++++++++  Init
    //====================================
    override fun getMainLayout(): Int {
        return R.layout.f_words_list
    }

    override fun getNotifyListenerId(): Int {
        return OpenWordList
    }

    override fun initVar(view: View) {
        wordListSearch = activity?.findViewById(R.id.wordListSearch)!!
        addWordList = activity?.findViewById(R.id.addWordList)!!
        filterMode = activity?.findViewById(R.id.filterMode)!!
        deleteWords = activity?.findViewById(R.id.deleteWords)!!
        makeFavorite = activity?.findViewById(R.id.makeFavorite)!!
        copyToFolder = activity?.findViewById(R.id.copyToFolder)!!
        selectAll = activity?.findViewById(R.id.selectAll)!!
        updateOrderView = activity?.findViewById(R.id.updateOrderView)!!
        favoriteActiveLabel = activity?.findViewById(R.id.favoriteActiveLabel)!!
        regexActiveLabel = activity?.findViewById(R.id.regexActiveLabel)!!
        practiceBtn = activity?.findViewById(R.id.practiceBtn)!!

        pathView = view.findViewById(R.id.pathView)
        wordListRV = view.findViewById(R.id.wordListRV)
    }

    override fun initFun() {
        setPathView()
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
        practiceBtn.setOnClickListener { practiceBtnClick() }
        deleteWords.setOnClickListener { deleteWords() }
        makeFavorite.setOnClickListener { makeItFavorite() }
        copyToFolder.setOnClickListener { copyToFolderClick() }
        selectAll.setOnClickListener { selectAllClick() }
        updateOrderView.setOnClickListener { updateOrderViewClick() }

        addWordList.setOnLongClickListener {
            getFileUri()
            true
        }
    }

    //region addWord
    private fun addWordList(){
        Lib.printLog("addWordItem")

        dialogEditItem = D_editItem(gContext){
            when(fgType){
                "Main" -> addWordInListWord(it)
                "Tags" -> addWordInTag(it)
                "Folders" -> addWordInFolder(it)
            }
        }
        dialogEditItem.textHint = gContext.getString(R.string.addWord)
        dialogEditItem.buildAndDisplay()
    }

    private fun practiceBtnClick(){
        navController.navigate(R.id.action_f_WordsList_to_f_CardsPractice)
    }

    private fun addWordInListWord(name : String){
        if(DataBaseServices.isWordNotExist(name)){
            DataBaseServices.insertWord(name)
            wordListAdapter.changeList()
            dialogEditItem.dismiss()
        }else{
            dialogEditItem.inputName.error = "This word is already exist"
        }
    }

    private fun addWordInTag(name : String){
        if(passedData.isNotEmpty()){
            if(DataBaseServices.isWordNotExist(name)){
                DataBaseServices.insertWord(name)
            }
            if(DataBaseServices.isWordTagNotExist(name, passedData)){
                DataBaseServices.insertWordTag(name, passedData)
                wordListAdapter.changeList()
                dialogEditItem.dismiss()
            }else{
                dialogEditItem.inputName.error = "This word is already exist"
            }
        }
    }
    private fun addWordInFolder(name : String){
        if(passedData.isNotEmpty()){
            if(DataBaseServices.isWordNotExist(name)){
                DataBaseServices.insertWord(name)
            }
            if(!DataBaseServices.isWordExistInFolder(passedData, name)){
                DataBaseServices.insertWordInFolder(passedData, name)
                wordListAdapter.changeList()
                dialogEditItem.dismiss()
            }else{
                dialogEditItem.inputName.error = "This word is already exist"
            }
        }
    }



    //endregion

    //region delete
    private fun deleteWords(){
        println(">>>Delete Words")
        when(fgType){
            "Main" -> deleteFromMainList()
            "Tags" -> deleteFromTag()
            "Folders" -> deleteFromFolder()
            "Text" -> deleteFromText()
        }
    }

    private fun deleteFromMainList(){
        val mainPath = gContext.getExternalFilesDir("/")!!.absolutePath

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

    private fun deleteFromTag(){
        if(passedData.isEmpty()) return

        val mainPath = gContext.getExternalFilesDir("/")!!.absolutePath

        val ask = D_ask(gContext, "Delete From ?"){
            val list = wordListAdapter.getSelected()
            if(it){
                DataBaseServices.deleteWordsFromTag(passedData, list)
            }else{
                DataBaseServices.deleteWords(mainPath, list)
            }
            deaSelectMode()
            wordListAdapter.changeList()
        }
        ask.approveText = "From Tag"
        ask.denyText = "Permanently"
        ask.buildAndDisplay()
    }

    private fun deleteFromFolder(){
        if(passedData.isEmpty()) return

        val mainPath = gContext.getExternalFilesDir("/")!!.absolutePath

        val ask = D_ask(gContext, "Delete From ?"){
            val list = wordListAdapter.getSelected()
            if(it){
                DataBaseServices.deleteWordsFromFolder(passedData, list)
            }else{
                DataBaseServices.deleteWords(mainPath, list)
            }
            deaSelectMode()
            wordListAdapter.changeList()
        }
        ask.approveText = "From Folder"
        ask.denyText = "Permanently"
        ask.buildAndDisplay()
    }

    private fun deleteFromText(){
        val mainPath = gContext.getExternalFilesDir("/")!!.absolutePath

        val ask = D_ask(gContext, "Delete From ?"){
            val list = wordListAdapter.getSelected()
            if(it){
                DataBaseServices.deleteTextWords(TextManagement.selectedItem, list)
            }else{
                DataBaseServices.deleteWords(mainPath, list)
            }
            deaSelectMode()
            wordListAdapter.changeList()
        }
        ask.approveText = "From Text"
        ask.denyText = "Permanently"
        ask.buildAndDisplay()
    }

    //endregion

    private fun makeItFavorite(){
        println(">>>Favorite Words")
        val list = wordListAdapter.getSelected()
        DataBaseServices.updateIsFavoriteWords(list)
        deaSelectMode()
        wordListAdapter.changeList()
        startSearch(wordListSearch.text.toString())
    }

    private fun copyToFolderClick(){
        val copyToFolderDialog = D_copy_to_folder(gContext){path->
            DataBaseServices.copyWordsToFolder(path, wordListAdapter.getSelected())
            Lib.showMessage(gContext, "Copy Done")
        }
        copyToFolderDialog.buildAndDisplay()
    }

    private fun selectAllClick(){
        wordListAdapter.selectAll()
    }

    private fun updateOrderViewClick(){
        DataBaseServices.updateWordLevelOrder(wordListAdapter.getSelected())
        deaSelectMode()
        wordListAdapter.changeList()
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

    //region path

    private fun setPathView(){
        val t = when(fgType){
            "Main" -> {
                "Main"
            }
            else ->{
                passedData
            }
        }
        pathView.text = t
    }

    //endregion

    //region RV

    private fun initWordListRV(){
        val layoutManager = LinearLayoutManager(gContext)
        wordListAdapter = A_WordList(gContext, fgType, passedData){ event, wordName ->
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
        addWordList.visibility = if(!on && fgType != "Text") View.VISIBLE else View.GONE
        filterMode.visibility = if(!on) View.VISIBLE else View.GONE
        wordListSearch.visibility = if(!on) View.VISIBLE else View.GONE
        practiceBtn.visibility = if(!on) View.VISIBLE else View.GONE
        deleteWords.visibility = if(on) View.VISIBLE else View.GONE
        makeFavorite.visibility = if(on) View.VISIBLE else View.GONE
        selectAll.visibility = if(on) View.VISIBLE else View.GONE
        updateOrderView.visibility = if(on) View.VISIBLE else View.GONE

        copyToFolder.visibility = if(on) View.VISIBLE else View.GONE
        Lib.hideKeyboardFrom(gContext, wordListSearch)
    }

    //endregion

    //region fragment

    private fun deaSelectMode() : Boolean{
        onSelectMode(false)
        return wordListAdapter.deaSelectMode()
    }



    //endregion

    //region getWordsFrom Pdf

    private fun getFileUri(){
        if(Lib.isStoragePermissionGranted(gContext, this)){
            val intent = Intent()
            intent.type = "*/*"
            intent.action = Intent.ACTION_GET_CONTENT
            val mimetypes = arrayOf("application/pdf")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
            startActivityForResult(intent, 100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 100){
            if(resultCode == Activity.RESULT_OK){
                val filePath = data?.data?.path
                if(filePath != null){
                    importFile(data.data!!)
                }else{
                    Lib.showMessage(gContext, "Something went wrong")
                    Log.d("ERROR", "filePath = NULL")
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun importFile(filePath: Uri){
        FileManagement.fgType = fgType
        FileManagement.passedData = passedData

        FileManagement.startWorking(gContext, filePath){
            val mainHandler =  Handler(gContext.mainLooper)
            val myRunnable =  Runnable {
                Lib.showMessage(gContext, "Complete")
                wordListAdapter.changeList()
            }
            mainHandler.post(myRunnable)
        }

    }

    //endregion

    //region override

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if(bundle != null){
            fgType = bundle.getString(FgType)!!
            passedData = bundle.getString(PassedData)!!
        }
    }

    override fun onBackPress(): Boolean {
        if (deaSelectMode()) return true
        return false
    }

    //endregion

}