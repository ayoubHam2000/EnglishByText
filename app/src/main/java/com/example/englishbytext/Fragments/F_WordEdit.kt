package com.example.englishbytext.Fragments

import android.app.Activity
import android.content.ClipboardManager
import android.content.ContentUris
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishbytext.Adapters.*
import com.example.englishbytext.Classes.Objects.D_Ask_Bottom
import com.example.englishbytext.Classes.Objects.D_ask
import com.example.englishbytext.Classes.Objects.D_lastImages
import com.example.englishbytext.Classes.Objects.D_recordDialog
import com.example.englishbytext.Classes.schemas.StringId
import com.example.englishbytext.Dialogs.D_editItem
import com.example.englishbytext.Objects.DataBaseServices
import com.example.englishbytext.Objects.Lib
import com.example.englishbytext.Objects.MainSetting
import com.example.englishbytext.Objects.WordsManagement
import com.example.englishbytext.R
import com.example.englishbytext.Utilites.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class F_WordEdit : MyFragment() {

    //region init

    //====================================
    //++++++++++++++++++++++  Vars
    //====================================
    private var wordName = ""

    //====================================
    //++++++++++++++++++++++  Views
    //====================================
    private lateinit var definitionAdapter : A_def_exp
    private lateinit var exampleAdapter : A_def_exp
    private lateinit var imagesAdapter : A_imageMedia
    private lateinit var audiosAdapter : A_audioMedia
    private lateinit var relatedAdapter : A_RelatedWordItem
    private lateinit var tagsAdapter : A_TagsWordsItem
    private var popPupList: ListPopupWindow? = null
    private var audioRecorder : D_recordDialog? = null

    private lateinit var parentLayout : RelativeLayout
    private lateinit var wordNameTextView : TextView
    private lateinit var favoriteBtn : ImageView
    private lateinit var masteredWordBtn : ImageView
    private lateinit var defRV : RecyclerView
    private lateinit var expRV : RecyclerView
    private lateinit var imgRV : RecyclerView
    private lateinit var audRV : RecyclerView
    private lateinit var relRV : RecyclerView
    private lateinit var tagRV : RecyclerView
    private lateinit var addDef : ImageView
    private lateinit var addExp : ImageView
    private lateinit var examplesCollection : TextView
    private lateinit var addImg : ImageView
    private lateinit var addImageFromGallery : ImageView
    private lateinit var addAud : ImageView
    private lateinit var addRel : ImageView
    private lateinit var addTag : ImageView
    private lateinit var sayIt : ImageView
    private lateinit var copyWord : ImageView
    private lateinit var deleteWord : ImageView

    //====================================
    //++++++++++++++++++++++  Init
    //====================================
    override fun getMainLayout(): Int {
        return R.layout.f_word_edit
    }

    override fun getNotifyListenerId(): Int {
        return OpenWordEdit
    }

    override fun initVar(view: View) {
        parentLayout = view.findViewById(R.id.parentLayout)
        wordNameTextView = view.findViewById(R.id.wordName)
        favoriteBtn = view.findViewById(R.id.favoriteBtn)
        defRV = view.findViewById(R.id.defRV)
        expRV = view.findViewById(R.id.expRV)
        imgRV = view.findViewById(R.id.imgRv)
        audRV = view.findViewById(R.id.audRV)
        relRV = view.findViewById(R.id.relatedRV)
        tagRV = view.findViewById(R.id.TagsRV)

        addDef = view.findViewById(R.id.addDef)
        addExp = view.findViewById(R.id.addExample)
        examplesCollection = view.findViewById(R.id.exampleCollectionSelect)
        addImg = view.findViewById(R.id.addImage)
        addImageFromGallery = view.findViewById(R.id.addImageFromGallery)
        addAud = view.findViewById(R.id.addAudio)
        addRel = view.findViewById(R.id.addRelated)
        addTag = view.findViewById(R.id.addTags)
        sayIt = view.findViewById(R.id.sayIt)
        copyWord = view.findViewById(R.id.copyWord)
        deleteWord = view.findViewById(R.id.deleteWord)
        masteredWordBtn = view.findViewById(R.id.masteredBtn)
    }

    override fun initFun() {
        wordNameTextView.text = wordName
        parentLayout.setBackgroundColor(gContext.getColor(R.color.transparent2))

        setFavorite()
        setMasterIcon()
        initDefinitionRv()
        initExampleRv()
        initImagesRV()
        initAudioRV()
        initRelatedRv()
        initTagsRv()
        initExampleCollection()
        addDef.setOnClickListener { addDef() }
        addExp.setOnClickListener { addExp() }
        addImg.setOnClickListener { addImage() }
        addImageFromGallery.setOnClickListener { addImageFromGallery() }
        addAud.setOnClickListener { addAudio() }
        addRel.setOnClickListener { addRelated() }
        addTag.setOnClickListener { addTag() }
        sayIt.setOnClickListener { sayItClick() }
        copyWord.setOnClickListener { copyWordClick() }
        deleteWord.setOnClickListener { deleteWordClick() }

        addDef.setOnLongClickListener {
            longAddDef()
            true
        }
        addExp.setOnLongClickListener {
            longAddExp()
            true
        }

        favoriteBtn.setOnClickListener { setWordFavorite() }
        masteredWordBtn.setOnClickListener { setMasteredWordBtn() }
    }

    //endregion

    //region views

    private fun setFavorite(){
        val isFavorite = DataBaseServices.getWordFavorite(wordName)
        val iconId = if(isFavorite) R.drawable.ic_favorite_active else R.drawable.ic_favorite
        favoriteBtn.setBackgroundResource(iconId)
    }

    private fun setMasterIcon(){
        val isKnown = DataBaseServices.getWordIsKnown(wordName)
        if(isKnown)
            Lib.changeBackgroundTint(gContext.getColor(R.color.master_word_active), masteredWordBtn)
        else
            Lib.changeBackgroundTint(gContext.getColor(R.color.master_word), masteredWordBtn)
    }

    private fun setWordFavorite(){
        val isFavorite = DataBaseServices.getWordFavorite(wordName)
        DataBaseServices.updateWordFavorite(wordName, !isFavorite)
        val iconId = if(!isFavorite) R.drawable.ic_favorite_active else R.drawable.ic_favorite
        favoriteBtn.setBackgroundResource(iconId)
    }

    private fun setMasteredWordBtn(){
        DataBaseServices.updateIsWordKnown(arrayListOf(wordName))
        setMasterIcon()
    }

    private fun sayItClick(){
        var textToSpeech: TextToSpeech? = null

        textToSpeech = TextToSpeech(gContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                textToSpeech?.speak(wordName, TextToSpeech.QUEUE_ADD, null, "SAY_IT")
            }
        }

    }

    private fun copyWordClick(){
        Lib.copyContent(gContext, "WordName", wordName)
        Lib.showMessage(gContext, "${wordName}${gContext.getString(R.string.is_copied)}")
    }

    private fun deleteWordClick(){
        val mainPath = gContext.getExternalFilesDir("/")!!.absolutePath
        DataBaseServices.deleteWords(mainPath, arrayListOf(wordName))
        navController.popBackStack()
        Lib.showMessage(gContext, R.string.word_deleted)
        for(item in WordsManagement.wordList){
            if(item.name == wordName){
                WordsManagement.wordList.remove(item)
                break
            }
        }
    }

    //endregion

    //region definition, examples
    //region definition RV

    private fun initDefinitionRv(){
        definitionAdapter = A_def_exp(gContext, wordName, OpenDefinition)


        val layout = LinearLayoutManager(context)
        definitionAdapter.changeList()
        defRV.layoutManager = layout
        defRV.adapter = definitionAdapter

        val itemTouchHelper = ItemTouchHelper(getItemTouchDeleter(OpenDefinition))
        itemTouchHelper.attachToRecyclerView(defRV)
    }

    private fun insertDef(new: String){
        DataBaseServices.insertDefinition(wordName, new)
        definitionAdapter.changeList()
    }

    private fun longAddDef(){
        val clipboard = gContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        val text = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(gContext)?.toString()
        if(text != null){
            insertDef("${definitionAdapter.list.count() + 1}. $text")
            Lib.shortMessage(gContext, "Def Added")
        }
    }

    private fun addDef(){
        var defDialog : D_editItem? = null
        defDialog = D_editItem(gContext){
            insertDef(it)
            defDialog!!.dismiss()
        }
        defDialog.textHint = gContext.getString(R.string.add_definition)
        defDialog.buildAndDisplay()
    }

    //endregion

    //region example RV

    private fun initExampleCollection(){
        val list = DataBaseServices.getExpCollection()
        list.add(0, StringId(0, "All"))
        val index = list.indexOf(list.find { it.id == MainSetting.selectedExamplesCollection })
        examplesCollection.text = list[index].value

        popPupList = ListPopupWindow(gContext)
        val adapter = ArrayAdapter(gContext, R.layout.support_simple_spinner_dropdown_item, list)
        popPupList?.anchorView = examplesCollection
        popPupList?.setAdapter(adapter)
        popPupList?.setOnItemClickListener { _, _, i, _ ->
            exampleAdapter.selectedCollection = list[i].id
            examplesCollection.text = list[i].value
            exampleAdapter.changeList()
            popPupList?.dismiss()
        }
        examplesCollection.setOnClickListener {
            popPupList?.show()
        }
    }

    private fun initExampleRv(){
        exampleAdapter = A_def_exp(gContext, wordName, OpenExample)
        exampleAdapter.selectedCollection = MainSetting.selectedExamplesCollection

        val layout = LinearLayoutManager(context)
        exampleAdapter.changeList()
        expRV.layoutManager = layout
        expRV.adapter = exampleAdapter

        val itemTouchHelper = ItemTouchHelper(getItemTouchDeleter(OpenExample))
        itemTouchHelper.attachToRecyclerView(expRV)
    }

    private fun insertExp(new: String){
        DataBaseServices.insertExamples(wordName, new, exampleAdapter.selectedCollection)
        exampleAdapter.changeList()
    }

    private fun longAddExp(){
        val clipboard = gContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        val text = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(gContext)?.toString()
        if(text != null){
            insertExp("${exampleAdapter.list.count()+1}. $text")
            Lib.shortMessage(gContext, "Exp Added")
        }
    }

    private fun addExp(){
        var exampleDialog : D_editItem? = null
        exampleDialog = D_editItem(gContext){
            insertExp(it)
            exampleDialog!!.dismiss()
        }
        exampleDialog.textHint = gContext.getString(R.string.add_example)
        exampleDialog.buildAndDisplay()
    }

    //endregion

    //region delete item

    private fun getItemTouchDeleter(type: Int) : ItemTouchHelper.SimpleCallback{
        return object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.ANIMATION_TYPE_DRAG) {
            override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when(type){
                    OpenDefinition -> {
                        val id = definitionAdapter.list[viewHolder.absoluteAdapterPosition].id
                        println("Remove Definition N ${viewHolder.absoluteAdapterPosition} | id -> $id")
                        removeDefinition(id)
                        //definitionAdapter.changeList()
                    }
                    OpenExample -> {
                        val id = exampleAdapter.list[viewHolder.absoluteAdapterPosition].id
                        println("Remove Example N ${viewHolder.absoluteAdapterPosition} | id -> $id")
                        removeExample(id)
                        //exampleAdapter.changeList()
                    }
                }

            }
        }
    }

    private fun removeExample(id: Int){
        val dialog = D_Ask_Bottom(){
            if(it){
                DataBaseServices.deleteExample(id)
            }
            exampleAdapter.changeList()
        }
        dialog.show(parentFragmentManager, "ASK_DELETE")
    }

    private fun removeDefinition(id: Int){
        val dialog = D_Ask_Bottom(){
            if(it){
                DataBaseServices.deleteDefinition(id)
            }
            definitionAdapter.changeList()
        }
        dialog.show(parentFragmentManager, "ASK_DELETE")
    }

    //endregion
    //endregion

    //region images
    private fun addImage(){
        println(">>>ADD IMAGE")
        getLastImages()
    }

    private fun addImageFromGallery(){
        println(">>>ADD IMAGE")
        startAddImage()
    }

    private fun getLastImages(){
        if(Lib.isStoragePermissionGranted(gContext, this)){
            var i = 0
            val res = ArrayList<Uri>()
            val imageColumns = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
            val imageOrderBy = MediaStore.Images.Media._ID + " DESC"
            val imageCursor: Cursor? = gContext.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy)
            val col = MediaStore.Images.Media._ID
            if(imageCursor != null && imageCursor.moveToFirst()){
                do {
                    val fullPath = imageCursor.getLong(imageCursor.getColumnIndex(col))
                    val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fullPath)
                    res.add(contentUri)
                    i++
                } while (imageCursor.moveToNext() && i < 200)
            }
            imageCursor?.close()
            var d : D_lastImages? = null
            d = D_lastImages(gContext, res){
                launchImageCrop(it)
                d?.dismiss()
                imagesAdapter.changeList()
            }
            d.buildAndDisplay()
        }
    }

    private fun initImagesRV(){
        imagesAdapter = A_imageMedia(gContext, wordName){ event ->
            recyclerViewImageEvent(event)
        }

        val layout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        imagesAdapter.changeList()
        imgRV.layoutManager = layout
        imgRV.adapter = imagesAdapter
    }

    private fun recyclerViewImageEvent(event: Int){
        when(event){
            OnSelectMode -> selectImageMode()
        }
    }

    private fun selectImageMode(){
        addImg.setBackgroundResource(R.drawable.ic_delete_24)
        addImageFromGallery.visibility = View.GONE
        addImg.setOnClickListener {
            val ask = D_ask(gContext, "ARE YOU SURE ?"){
                if(it){
                    imagesAdapter.deleteItems()
                    deaSelectImageMode()
                }
            }
            ask.buildAndDisplay()
        }
    }

    private fun activeAddImage(){
        addImageFromGallery.visibility = View.VISIBLE
        addImg.setBackgroundResource(R.drawable.ic__add)
        addImg.setOnClickListener {
            addImage()
        }
    }

    //endregion

    //region Audio

    private fun addAudio(){
        if(Lib.isAudioPermissionGranted(gContext, this)){
            println(">>> Add audio")
            audioRecorder = D_recordDialog(gContext){
                val path = it
                DataBaseServices.insertAudio(wordName, path)
                audiosAdapter.changeList()
            }
            audioRecorder?.buildAndDisplay()
        }
    }

    private fun initAudioRV(){
        audiosAdapter = A_audioMedia(gContext, wordName){ event ->
            recyclerViewAudioEvent(event)
        }

        val layout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        audiosAdapter.changeList()
        audRV.layoutManager = layout
        audRV.adapter = audiosAdapter
    }

    private fun recyclerViewAudioEvent(event: Int){
        when(event){
            OnSelectMode -> selectAudioMode()
        }
    }

    private fun selectAudioMode(){
        addAud.setBackgroundResource(R.drawable.ic_delete_24)
        addAud.setOnClickListener {
            val ask = D_ask(gContext, "ARE YOU SURE ?"){
                if(it){
                    audiosAdapter.deleteItems()
                    deaSelectAudioMode()
                }
            }
            ask.buildAndDisplay()
        }
    }

    private fun activeAddAudio(){
        addAud.setBackgroundResource(R.drawable.ic__add)
        addAud.setOnClickListener {
            addAudio()
        }
    }

    //endregion

    //region related

    private fun addRelated(){
        var dialog : D_editItem? = null
        dialog = D_editItem(gContext){ newItem->
            when{
                wordName == newItem -> {
                    dialog!!.inputName.error = "The Same Word"
                }
                /*DataBaseServices.isWordNotExist(newItem) ->{
                    dialog!!.inputName.error = "Word Not Exist"
                }*/
                !DataBaseServices.isRelatedNotExist(wordName, newItem) ->{
                    dialog!!.inputName.error = "Already Exist In The List"
                }
                else->{
                    DataBaseServices.insertWord(newItem)
                    DataBaseServices.insertRelated(wordName, newItem)
                    relatedAdapter.changeList()
                    definitionAdapter.changeList()
                    exampleAdapter.changeList()
                    imagesAdapter.changeList()
                    audiosAdapter.changeList()
                    tagsAdapter.changeList()
                    dialog!!.dismiss()
                }
            }
        }
        dialog.textHint = "Related"
        dialog.listSuggestion = DataBaseServices.getRelatedWordSuggestion(wordName)
        dialog.buildAndDisplay()
    }

    private fun initRelatedRv(){
        val layout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        relatedAdapter = A_RelatedWordItem(gContext, wordName){ event, item ->
            relatedRvEvent(event, item)
        }
        relatedAdapter.changeList()
        relRV.adapter = relatedAdapter
        relRV.layoutManager = layout
    }

    private fun relatedRvEvent(event: Int, item: String){
        when(event){
            OnSelectMode -> activeDeleteRelated()
            OpenItem -> openRelatedItem(item)
        }
    }

    private fun openRelatedItem(item: String){
        val bundle = Bundle()
        bundle.putString("WORD_NAME", item)
        navController.navigate(R.id.action_f_WordEdit_self, bundle)
    }

    private fun activeDeleteRelated(){
        addRel.setBackgroundResource(R.drawable.ic_delete_24)
        addRel.setOnClickListener {
            val ask = D_ask(gContext, "ARE YOU SURE ?"){
                if(it){
                    DataBaseServices.deleteRelated(relatedAdapter.getSelectedIds())
                    deaSelectRelated()
                    definitionAdapter.changeList()
                    exampleAdapter.changeList()
                    imagesAdapter.changeList()
                    audiosAdapter.changeList()
                    tagsAdapter.changeList()
                }
            }
            ask.buildAndDisplay()
        }
    }

    private fun activeAddRelated(){
        addRel.setBackgroundResource(R.drawable.ic__add)
        addRel.setOnClickListener {
            addRelated()
        }
    }

    //endregion

    //region tags

    private fun addTag(){
        var dialog : D_editItem? = null
        dialog = D_editItem(gContext){ newItem->
            when{
                DataBaseServices.isWordTagNotExist(wordName, newItem) ->{
                    DataBaseServices.insertWordTag(wordName, newItem)
                    tagsAdapter.changeList()
                    dialog!!.dismiss()
                }
                else->{
                    dialog!!.inputName.error = "Tag Already Exist In the List"
                }
            }
        }
        dialog.textHint = "Tag"
        dialog.maxChar = MaxTagChars
        dialog.listSuggestion = DataBaseServices.getTagSuggestionList(wordName)
        dialog.buildAndDisplay()
    }

    private fun initTagsRv(){
        val layout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        tagsAdapter = A_TagsWordsItem(gContext, wordName){ event, item->
            when(event){
                OpenItem -> openTagItem(item)
                OnSelectMode -> activeDeleteTags()
            }
        }

        tagsAdapter.changeList()
        tagRV.adapter = tagsAdapter
        tagRV.layoutManager = layout
    }

    private fun openTagItem(tag: String){
        val bundle = Bundle()

        bundle.putString(FgType, "Tags")
        bundle.putString(PassedData, tag)
        navController.navigate(R.id.f_WordsList, bundle)
    }

    private fun activeDeleteTags(){
        addTag.setBackgroundResource(R.drawable.ic_delete_24)
        addTag.setOnClickListener {
            val ask = D_ask(gContext, "ARE YOU SURE ?"){
                if(it){
                    DataBaseServices.deleteWordTags(tagsAdapter.getSelectedIds())
                    deaSelectTag()
                }
            }
            ask.buildAndDisplay()
        }
    }

    private fun activeAddTag(){
        addTag.setBackgroundResource(R.drawable.ic__add)
        addTag.setOnClickListener {
            addTag()
        }
    }

    //endregion

    //region media

    private fun startAddImage(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeType = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        intent.flags = Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun launchImageCrop(uri: Uri){
        CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(gContext, this)
    }

    private fun getBitmapAsByteArray(bitmap: Bitmap) : ByteArray{
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        return outputStream.toByteArray()
    }

    private fun saveImageToDataBase(uri: Uri){
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss.SSS", Locale.US).format(Date())
        val name = "Image_$timeStamp.jpeg"
        val path = gContext.getExternalFilesDir("/$IMAGE_FOLDER")!!.absolutePath

        File(path, name).writeText("")
        val f = FileOutputStream("$path/$name")
        val bitmap = BitmapFactory.decodeFile(uri.encodedPath)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, f)
        DataBaseServices.insertImage(wordName, name)

        //println("-->(${wordName}), ($name)")
        imagesAdapter.changeList()
    }

    //endregion

    //region Methods

    private fun deaSelectImageMode() : Boolean{
        activeAddImage()
        return imagesAdapter.disableSelectImage()
    }

    private fun deaSelectAudioMode() : Boolean{
        activeAddAudio()
        return audiosAdapter.disableSelectAudio()
    }

    private fun deaSelectRelated() : Boolean{
        activeAddRelated()
        return relatedAdapter.deaSelect()
    }

    private fun deaSelectTag() : Boolean{
        activeAddTag()
        return tagsAdapter.deaSelect()
    }

    fun deaAudioMedia(){
        audioRecorder?.exitMedia()
        audiosAdapter.exitMedia()
    }

    //endregion

    //region override

    override fun onBackPress(): Boolean {
        if (deaSelectImageMode()) return true
        if (deaSelectAudioMode()) return true
        if (deaSelectRelated()) return true
        if (deaSelectTag()) return true
        if (popPupList != null && popPupList!!.isShowing) return true
        deaAudioMedia()
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            GALLERY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let {
                        launchImageCrop(it)
                    }
                } else {
                    println(">>>couldn't select image from the gallery")
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    result.uri?.let {
                        saveImageToDataBase(it)
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    println(">>>CropError : ${result.error}")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if(bundle != null){
            wordName = bundle.getString("WORD_NAME")!!
            WordsManagement.selectedWordName = wordName
        }
    }

    //endregion


}