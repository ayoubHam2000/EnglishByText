package com.example.englishbytext


import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Adapters.A_setItem
import com.example.englishbytext.Classes.Objects.D_ask
import com.example.englishbytext.Classes.Objects.D_ask_color
import com.example.englishbytext.Dialogs.D_editItem
import com.example.englishbytext.Fragments.*
import com.example.englishbytext.Interfaces.NotifyActivity
import com.example.englishbytext.Objects.*
import com.example.englishbytext.Objects.Lib.printLog
import com.example.englishbytext.Utilites.*
import com.google.android.material.navigation.NavigationView
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), NotifyActivity {

    //region vars, init, view
    //region vars
    lateinit var dialogEditItem : D_editItem
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navigationView : NavigationView
    private lateinit var navController : NavController
    private lateinit var setItemAdapter : A_setItem

    //view
    private lateinit var setRecyclerView : RecyclerView
    private lateinit var progressProcess : View

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        //window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        //window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFun()


    }



    private fun initFun(){
        initView()
        initApp()
        initSet()
    }

    private fun initApp(){
        DataBaseServices.initDataBase(this)
        initSlideBar()
        initDarkMode()

        DataBaseServices.deleteFolderTable()
    }

    private fun initView(){
        progressProcess = findViewById(R.id.progressProcess)
        setRecyclerView = findViewById(R.id.set_RecyclerView)
        navigationView = findViewById(R.id.navigationView)
        drawerLayout = findViewById(R.id.drawerLayout)
        navController = (supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment).navController
    }

    private fun initDarkMode(){
        if(Setting.isDarkMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }


    //endregion

    //region slideBar

    private fun initSlideBar(){
        onOpenSlideBar()
        collapse()
        openSettings()
        openAllWords()
        openTagsWords()
        openFolders()
    }

    //region settings

    private fun onOpenSlideBar(){
        val openDrawerListener = object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {
                refreshWordsItems()
            }

            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        }

        drawerLayout.addDrawerListener(openDrawerListener)
    }

    private fun openSettings(){
        val settingsIcon = findViewById<ImageView>(R.id.goToSetting)

        settingsIcon.setOnClickListener {
            navigateToFg(R.id.f_Settings)
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun openAllWords(){
        val allWords = findViewById<LinearLayout>(R.id.allWords)

        allWords.setOnClickListener {
            navigateToWordList()
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun openTagsWords(){
        val allTags = findViewById<LinearLayout>(R.id.allTags)

        allTags.setOnClickListener {
            navigateToFg(R.id.f_Tags)
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun openFolders(){
        val allFolders = findViewById<LinearLayout>(R.id.folders)

        allFolders.setOnClickListener {
            navigateToFg(R.id.f_Folders)
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    //endregion

    private fun refreshWordsItems(){
        val allWordsNbr = findViewById<TextView>(R.id.allWordsNbr)
        val allTags = findViewById<TextView>(R.id.allTagsNbr)
        val allFolders = findViewById<TextView>(R.id.allFoldersNbr)

        thread {
            val totalWords = DataBaseServices.tableCountByQuery("SELECT $A_word FROM $T_words")

            val tagsNbr = DataBaseServices.tableCountByQuery("SELECT $A_tag FROM $T_tags")
            val wordsInTags = DataBaseServices.tableCountByQuery("SELECT distinct $A_word FROM $T_wordTags")

            val foldersNbr = DataBaseServices.tableCountByQuery("SELECT $A_path FROM $T_folders")
            val wordsInFolder = DataBaseServices.tableCountByQuery("SELECT distinct $A_word FROM $T_words_Folder")

            Handler(this.mainLooper).post{
                allWordsNbr.text = totalWords.toString()
                allTags.text = "$tagsNbr ($wordsInTags)"
                allFolders.text = "$foldersNbr ($wordsInFolder)"
            }

        }

    }

    //region collapse
    private fun collapseCategory(action: Boolean){
        val categorySection = findViewById<LinearLayout>(R.id.categorySection)
        val openCloseCategoriesImg = findViewById<ImageView>(R.id.openCloseCategoriesImg)

        categorySection.visibility = if(action) {
            openCloseCategoriesImg.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_24)
            View.VISIBLE
        }else {
            openCloseCategoriesImg.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_24)
            View.GONE
        }
        Setting.setCategorySection(action.toString())
    }

    private fun collapseCollection(action: Boolean){
        val collectionSection = findViewById<LinearLayout>(R.id.collectionSection)
        val openCloseCollectionsImg = findViewById<ImageView>(R.id.openCloseCollectionsImg)

        collectionSection.visibility = if(action) {
            openCloseCollectionsImg.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_24)
            View.VISIBLE
        } else {
            openCloseCollectionsImg.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_24)
            View.GONE
        }
        Setting.setCollectionSection(action.toString())
    }

    private fun collapse(){
        val openCloseCategories = findViewById<LinearLayout>(R.id.openCloseCategories)
        val openCloseCollection = findViewById<LinearLayout>(R.id.openCloseCollection)

        openCloseCategories.setOnClickListener {
            collapseCategory(!Setting.categorySection)
        }

        openCloseCollection.setOnClickListener {
            collapseCollection(!Setting.collectionSection)
        }

        collapseCategory(Setting.categorySection)
        collapseCollection(Setting.collectionSection)
    }
    //endregion

    //endregion

    //region Set Manager
    private fun initSet(){
        val layoutManager = LinearLayoutManager(this)
        setItemAdapter = A_setItem(this){ event, selectedItem->
            recyclerViewEvent(event, selectedItem)
        }

        setItemAdapter.changeList()
        setRecyclerView.adapter = setItemAdapter
        setRecyclerView.layoutManager = layoutManager

        //functions
        addSet()
    }

    private fun recyclerViewEvent(event: Int, selectedItem: String){
        when(event){
            OpenItem -> {
                openSet(selectedItem)
                printLog("OpenSetItem $selectedItem")
            }

            Edit -> {
                editSet(selectedItem)
                printLog("EditSet $selectedItem")
            }

            ChangeColor -> {
                setChangeColor(selectedItem)
                printLog("ChangeSetColor $selectedItem")
            }

            Delete -> {
                deleteSet(selectedItem)
                printLog("deleteSet $selectedItem")
            }

            MoveToAction -> {
                //moveCollectionToAction(selectedItem)
                printLog("MoveToAction $selectedItem")
            }
        }
    }

    private fun selectSetOnOpen(name: String){
        SetManagement.setSelectedSet(name)
        DataBaseServices.updateVar(V_SelectedSet, name)
        setActionBarTitle(name)
    }

    private fun refreshSet(collection: Boolean = false){
        setItemAdapter.changeList()
        if(collection){
            val fragment = getFragment()
            if(fragment is F_Collection){
                fragment.refreshCollection()
            }
        }
    }

    private fun openSet(selectedItem: String){
        //SetManagement.isCheckCollection = false //in the start of the fragment any action should not be active
        selectSetOnOpen(selectedItem)
        refreshSet(true)
        navController.popBackStack(R.id.f_Collection, false) //back to collectionFrag
        drawerLayout.closeDrawer(GravityCompat.START)

        //WordsManagement.tableType = NormalWords
        //popUpMenu.menu.findItem(R.id.SelectAll).isEnabled = false
    }

    private fun editSet(selectedItem: String){
        dialogEditItem = D_editItem(this){
            when {
                it.count() > MaxSetName -> {
                    Lib.showMessage(this, "${this.getString(R.string.Max_character_is)}$MaxSetName")
                }
                DataBaseServices.isSetNotExist(it) -> {
                    if(selectedItem == SetManagement.getSelectedSet()){
                        selectSetOnOpen(it)
                    }
                    DataBaseServices.updateSet(selectedItem, it)
                    refreshSet()
                    dialogEditItem.dismiss()
                }
                else -> {
                    Lib.showMessage(this, R.string.this_name_is_taken)
                }
            }
        }
        dialogEditItem.textHint = this.getString(R.string.set_name)
        dialogEditItem.maxLine = 1
        dialogEditItem.textInput = selectedItem
        dialogEditItem.maxChar = MaxSetName
        dialogEditItem.build()
        dialogEditItem.display()
    }

    private fun setChangeColor(selectedItem: String){
        val dialog = D_ask_color(this){ color ->
            DataBaseServices.updateSetColor(selectedItem, color)
            refreshSet(true)
        }
        dialog.theIntColor = SetManagement.getSet()!!.tagColor
        dialog.buildAndDisplay()
    }

    private fun deleteSet(selectedItem: String){
        val ask = D_ask(this, "ARE YOU SURE ?"){
            if(it){
                if(selectedItem == SetManagement.getSelectedSet()){
                    selectSetOnOpen(AllSet)
                }
                DataBaseServices.deleteSet(selectedItem)
                refreshSet(true)
            }
        }
        ask.build()
        ask.display()
    }

    private fun addSet(){
        val addSet = findViewById<TextView>(R.id.add_Set)

        addSet.setOnClickListener {
            dialogEditItem = D_editItem(this){
                when {
                    it.count() > MaxSetName -> {
                        Lib.showMessage(
                            this,
                            "${this.getString(R.string.Max_character_is)}$MaxSetName"
                        )
                    }
                    DataBaseServices.isSetNotExist(it) -> {
                        DataBaseServices.insertSet(it, Lib.pickRandomColor())
                        refreshSet()
                        dialogEditItem.dismiss()
                    }
                    else -> {
                        Lib.showMessage(this, R.string.this_name_is_taken)
                    }
                }
            }
            dialogEditItem.textHint = this.getString(R.string.set_name)
            dialogEditItem.maxLine = 1
            dialogEditItem.maxChar = MaxSetName
            dialogEditItem.buildAndDisplay()
        }

    }



    //endregion

    //region Notify Activity

    override fun onBackPressed() {
        if((getFragment() as MyFragment).onBackPress()) return
        super.onBackPressed()
    }

    override fun notifyActivity(event: Int, onProcess: Boolean) {
        when(event){
            OpenCollectionFrag -> openCollectionFrag()
            OpenTextFrag -> openTextFrag()
            OpenTextEditFrag -> openTextEditFrag()
            SaveChanges -> saveChanges()
            OpenTextDisplayFrag -> openTextDisplayFrag()
            OpenWordEdit -> openWordEditFrag()
            OpenSettings -> openSettingsFrag()
            OpenWordList -> openWordList()
            OpenTagFg -> openTagFg()
            OpenAllFoldersFrag -> openFolderFg()
            OpenCardsPractice -> openCardsPractice()
            OnProcess -> {
                Handler(this.mainLooper).post {
                    if (onProcess) {
                        progressProcess.visibility = View.VISIBLE
                    } else {
                        progressProcess.visibility = View.INVISIBLE
                    }
                }
            }
            RefreshData -> {
                setItemAdapter.changeList()
            }
        }
    }

    override fun navigateFragment(id: Int) {
        navigateToFg(id)
    }

    private fun saveChanges(){
        when(getFragment()){
            is F_EditText -> {
                onBackPressed()
            }
        }
    }

    private fun openCollectionFrag(){
        initActionBarLayout(R.layout.activity_action_bar)
        setActionBarTitle(SetManagement.getSelectedSet())
    }

    private fun openTextFrag(){
        val title = CollectionManagement.selectedCol.father + ": " + CollectionManagement.selectedCol.name
        initActionBarLayout(R.layout.action_text)
        setActionBarTitle(title)
    }

    private fun openTextEditFrag(){
        hideActionBar()
    }

    private fun openTextDisplayFrag(){
        hideActionBar()
    }

    private fun openSettingsFrag(){
        hideActionBar()
    }

    private fun openWordEditFrag(){
        hideActionBar()
    }

    private fun openWordList(){
        initActionBarLayout(R.layout.action_word_list)
    }

    private fun openTagFg(){
        initActionBarLayout(R.layout.action_tag)
    }

    private fun openFolderFg(){
        initActionBarLayout(R.layout.action_folder)
    }

    private fun openCardsPractice(){
        hideActionBar()
    }

    //region Common

    private fun initActionBarLayout(id: Int){

        val actionBar = findViewById<LinearLayout>(R.id.mainActionBar)
        actionBar.removeAllViews()
        layoutInflater.inflate(id, actionBar, true)
        initNavigationButton()
        actionBar.visibility = View.VISIBLE
    }

    private fun hideActionBar(){
        val actionBar = findViewById<LinearLayout>(R.id.mainActionBar)!!
        actionBar.visibility = View.GONE
    }

    private fun initNavigationButton(){
        val navigationButton : ImageView = findViewById(R.id.navigationButton)

        navigationButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setActionBarTitle(name: String){
        val setName : TextView? = findViewById(R.id.SetName)
        setName?.text = name
    }

    //endregion

    //endregion

    //region Other Permissions, onBackPress, getFrag

    private fun getFragment() : Fragment?{
        val navHostFragment : Fragment = supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment

        return navHostFragment.childFragmentManager.fragments[0]
    }

    private fun navigateToFg(id: Int){
        //val previousId = navController.previousBackStackEntry?.destination?.id
        val actualId = navController.currentBackStackEntry?.destination?.id

        if(actualId != id){
            navController.navigate(id)
        }
    }

    private fun navigateToWordList(){
        val id = R.id.f_WordsList


        val bundle = Bundle()
        bundle.putString(FgType, "Main")
        bundle.putString(PassedData, "")

        navController.navigate(id, bundle)

    }

    /*private fun navigateToWordList(tag: String = ""){
        val fragment = getFragment()
        val actualId = navController.currentBackStackEntry?.destination?.id
        val actualTag = if(fragment is F_WordsList) fragment.passedData else ""
        //val fgType = if(fragment is F_WordsList) fragment.fgType else ""
        val id = R.id.f_WordsList

        val isOtherTag = (actualTag != tag && actualId == id)
        if(actualId != id || isOtherTag){
            val bundle = Bundle()
            bundle.putString(SELECTED_TAG, tag)
            //bundle.putString(FgType, fgType)
            navController.navigate(id, bundle)
        }
    }*/

    override fun onPause() {
        when(val fragment = getFragment()){
            is F_WordEdit -> fragment.deaAudioMedia()
        }
        super.onPause()
    }


    //endregion
}