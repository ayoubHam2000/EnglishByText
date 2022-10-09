package com.example.englishbytext.Fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Adapters.A_WordList
import com.example.englishbytext.Classes.Objects.D_ask
import com.example.englishbytext.Classes.Objects.D_copy_to_folder
import com.example.englishbytext.Classes.Objects.D_filter_word_list
import com.example.englishbytext.Classes.Objects.M_FilterPopUpMenu
import com.example.englishbytext.Classes.schemas.FilterData
import com.example.englishbytext.Dialogs.D_editItem
import com.example.englishbytext.Objects.*
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.*
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
    lateinit var popupMenu : M_FilterPopUpMenu
    lateinit var dialogEditItem : D_editItem
    //search
    private var continueSearch = true
    private var oldSearch = ""
    private var filterData = FilterData()
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
        //startSearch(wordListSearch.text.toString())
    }

    //endregion

    //region actionBar

    private fun initActionBar(){
        onSelectMode(false)

        popupMenu = M_FilterPopUpMenu(gContext, filterMode, R.menu.m_filter_mode)
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

        addWordList.setOnLongClickListener {
            getFileUri()
            true
        }
        practiceBtn.setOnLongClickListener {
            practiceBtnLongClick()
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


    //startSearch
    private fun addWordInListWord(name: String){
        if(DataBaseServices.isWordNotExist(name)){
            DataBaseServices.insertWord(name)
            onAddWord()
        }else{
            dialogEditItem.inputName.error = "This word is already exist"
        }
    }

    private fun addWordInTag(name: String){
        if(passedData.isNotEmpty()){
            if(DataBaseServices.isWordNotExist(name)){
                DataBaseServices.insertWord(name)
            }
            if(DataBaseServices.isWordTagNotExist(name, passedData)){
                DataBaseServices.insertWordTag(name, passedData)
                onAddWord()
            }else{
                dialogEditItem.inputName.error = "This word is already exist"
            }
        }
    }

    private fun addWordInFolder(name: String){
        if(passedData.isNotEmpty()){
            if(DataBaseServices.isWordNotExist(name)){
                DataBaseServices.insertWord(name)
            }
            if(!DataBaseServices.isWordExistInFolder(passedData, name)){
                DataBaseServices.insertWordInFolder(passedData, name)
                onAddWord()
            }else{
                dialogEditItem.inputName.error = "This word is already exist"
            }
        }
    }

    private fun onAddWord(){
        notifyList()
        dialogEditItem.dismiss()
        /*val s = wordListSearch.text.toString()
        if(s.isNotEmpty()){
            startSearch(s)
        }*/
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
                afterDelete()
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
            afterDelete()
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
            afterDelete()
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
            afterDelete()
        }
        ask.approveText = "From Text"
        ask.denyText = "Permanently"
        ask.buildAndDisplay()
    }

    private fun afterDelete(){
        deaSelectMode()
        notifyList()
        /*if(wordListSearch.text.toString().isNotEmpty())
            startSearch(wordListSearch.text.toString())*/
    }

    //endregion

    //region buttons
    private fun practiceBtnClick(){
        WordsManagement.setPracticeWordList(wordListAdapter.filterList)
        if (wordListAdapter.filterList.size == 0)
            Lib.showMessage(gContext, R.string.practice_list_empty)
        else
            navController.navigate(R.id.action_f_WordsList_to_f_CardsPractice)
    }
    private fun practiceBtnLongClick(){
        WordsManagement.setPracticeWordList(WordsManagement.wordList)
        navController.navigate(R.id.action_f_WordsList_to_f_CardsPractice)
    }

    private fun makeItFavorite(){
        println(">>>Favorite Words")
        val list = wordListAdapter.getSelected()
        DataBaseServices.updateIsFavoriteWords(list)
        deaSelectMode()
        notifyList()
        //startSearch(wordListSearch.text.toString())
    }

    private fun copyToFolderClick(){
        val copyToFolderDialog = D_copy_to_folder(gContext, fgType){ path->
            DataBaseServices.copyWordsToFolder(path, wordListAdapter.getSelected())
            Lib.showMessage(gContext, "Copy Done")
        }
        copyToFolderDialog.buildAndDisplay()
    }

    private fun selectAllClick(){
        wordListAdapter.selectAll()
    }


    //endregion

    //region filter
    private fun filterModeSetUp(){
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.isSortPractice -> isSortPracticeClick()
                R.id.isOnRegex -> onRegexFilterClick()

                R.id.filter -> onFilterClick()

                R.id.createdTimeSort -> onCreatedTimeFilterClick(SORT_CREATED_TIME_ASC)
                R.id.masteredSort -> onCreatedTimeFilterClick(SORT_MASTERED_ASC)
                R.id.randomSort-> onRandomFilterClick()
            }
            true
        }
        initPopUpMenu()
    }

    private fun initPopUpMenu(){


        regexActiveLabel.visibility = if(MainSetting.onRegexSearch) View.VISIBLE else View.GONE
        filterView()
    }

    private fun filterView(){
        //sort
        val createdTime = popupMenu.getItemById(R.id.createdTimeSort)
        val masterSort = popupMenu.getItemById(R.id.masteredSort)

        val selectedOrder = arrayListOf("Created Time DESC", "Created Time ASC", "Mastered DESC", "Mastered ASC")
        val defaultValue = arrayListOf(gContext.getString(R.string.created_time), gContext.getString(R.string.master_sort))
        val sortItemView = arrayListOf(createdTime, masterSort)
        val sortType = MainSetting.sortTypeWordList

        //reset items to default value
        for(i in 0..1){
            val spanString = SpannableString(defaultValue[i])
            val color = gContext.getColor(R.color.black)
            spanString.setSpan(ForegroundColorSpan(color), 0, spanString.length, 0)
            sortItemView[i].title = spanString
        }
        //change the selected one
        val spanString = SpannableString(selectedOrder[sortType])
        spanString.setSpan(ForegroundColorSpan(Color.GREEN), 0, spanString.length, 0)
        sortItemView[sortType/2].title = spanString
    }

    private fun isSortPracticeClick()
    {
        val item = popupMenu.getItemById(R.id.isSortPractice)
        item.isChecked = !item.isChecked
        MainSetting.setPracticeSort(item.isChecked.toString())
    }
    
    private fun onRegexFilterClick(){
        val item = popupMenu.getItemById(R.id.isOnRegex)
        item.isChecked = !item.isChecked
        filterData.onRegex = !filterData.onRegex
        MainSetting.setRegexSearch(item.isChecked.toString())
        regexActiveLabel.visibility = if(MainSetting.onRegexSearch) View.VISIBLE else View.GONE
        notifyList()
    }

    //---------------------------
    private fun onFilterClick(){
        val dialog = D_filter_word_list(gContext, filterData){
            notifyList()
            wordListAdapter.displayListCount()
        }
        dialog.buildAndDisplay()
    }

    //---------------------------
    private fun onCreatedTimeFilterClick(clickedItem : Int){
        val newSortType = if (clickedItem == MainSetting.sortTypeWordList)
            (MainSetting.sortTypeWordList + 1) % 2 + clickedItem
        else
            clickedItem
        MainSetting.setSortTypeWordList(newSortType.toString())
        filterView()
        notifyList()
    }

    private fun onRandomFilterClick(){
        val randomViewItem = popupMenu.getItemById(R.id.randomSort)

        MainSetting.isRandomSortIsActive = !MainSetting.isRandomSortIsActive
        randomViewItem.isChecked = MainSetting.isRandomSortIsActive
        notifyList()
    }

    //---------------------------
    private fun notifyList(){
        wordListAdapter.changeList(filterData)
    }

    //endregion

    //region search
    private fun actionDone() : TextView.OnEditorActionListener{
        return TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                wordListAdapter.displayListCount()
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

    private fun startSearch(s: String){
        val regexOn = popupMenu.getItemById(R.id.isOnRegex)

        if(!isValidRegex(s)){
            Handler(gContext.mainLooper).post {
                wordListSearch.error = "Invalid regex"
            }
            return
        }

        filterData.searchWord = s
        notifyList()
    }

    private fun isValidRegex(regex: String) : Boolean{
        return try{
            regex.toRegex()
            true
        }catch (e: Exception){
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
        notifyList()

        wordListRV.adapter = wordListAdapter
        wordListRV.layoutManager = layoutManager

    }

    private fun recyclerViewEvent(event: Int, word: String){
        when(event){
            OpenItem -> openItem(word)
            OnSelectMode -> onSelectMode(true)
            END -> onListSearchEnd()
        }
    }

    private fun openItem(word: String){
        val bundle = Bundle()
        bundle.putString("WORD_NAME", word)
        Lib.hideKeyboardFrom(gContext, wordListSearch)
        navController.navigate(R.id.action_f_WordsList_to_f_WordEdit, bundle)
    }

    private fun onSelectMode(on: Boolean){
        addWordList.visibility = if(!on && fgType != "Text") View.VISIBLE else View.GONE
        filterMode.visibility = if(!on) View.VISIBLE else View.GONE
        wordListSearch.visibility = if(!on) View.VISIBLE else View.GONE
        practiceBtn.visibility = if(!on) View.VISIBLE else View.GONE
        deleteWords.visibility = if(on) View.VISIBLE else View.GONE
        makeFavorite.visibility = if(on) View.VISIBLE else View.GONE
        selectAll.visibility = if(on) View.VISIBLE else View.GONE

        copyToFolder.visibility = if(on) View.VISIBLE else View.GONE
        Lib.hideKeyboardFrom(gContext, wordListSearch)
    }

    private fun onListSearchEnd(){
        if(saveStatesMap["View1"] != null)
        wordListRV.layoutManager?.onRestoreInstanceState(saveStatesMap["View1"])
    }

    //endregion

    //region fragment

    private fun deaSelectMode() : Boolean{
        onSelectMode(false)
        return wordListAdapter.deaSelectMode()
    }



    //endregion

    //region getWordsFrom Pdf

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes


            val filePath = result.data?.data?.path
            if(filePath != null){
                importFile(result.data?.data!!)
            }else{
                Lib.showMessage(gContext, "Something went wrong")
                Log.d("ERROR", "filePath = NULL")
            }
        }
    }

    private fun getFileUri(){
        if(Lib.isStoragePermissionGranted(gContext, this)){
            val intent = Intent()
            intent.type = "*/*"
            intent.action = Intent.ACTION_GET_CONTENT
            val mimetypes = arrayOf("text/plain", "application/pdf")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
            resultLauncher.launch(intent)
        }
    }

    private fun importFile(filePath: Uri){
        FileManagement.fgType = fgType
        FileManagement.passedData = passedData

        FileManagement.startWorking(gContext, filePath){
            val mainHandler =  Handler(gContext.mainLooper)
            val myRunnable =  Runnable {
                Lib.showMessage(gContext, "Complete")
                notifyList()
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
        //clear focus from search field when the input is focus
        if(wordListSearch.isFocused){
            wordListSearch.clearFocus()
            return true
        }
        if (deaSelectMode()) return true
        return false
    }

    override fun onPause() {
        super.onPause()
        saveStatesMap["View1"] = wordListRV.layoutManager?.onSaveInstanceState()
    }

    override fun onResume() {
        super.onResume()
        //onListSearchEnd() -> View1
        //wordListRV.layoutManager?.onRestoreInstanceState(saveStatesMap["View1"])
    }

    //endregion


}