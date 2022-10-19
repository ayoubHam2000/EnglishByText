package com.example.englishbytext.Objects

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.util.Base64
import com.example.englishbytext.*
import com.example.englishbytext.Classes.schemas.*
import com.example.englishbytext.Classes.schemas.Collections
import com.example.englishbytext.Objects.DataBaseServices.toBase64
import com.example.englishbytext.Utilites.*
import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


object DataBaseServices {

    //region section 0 (vars, init, load)
    //region global vars

    private lateinit var dataBase : SQLiteDatabase
    private lateinit var tables : Array<String>
    var mainPath : String? = null

    //endregion

    //region init
    fun initDataBase(context: Context){
        dataBase = context.openOrCreateDatabase("ENGLISH_BY_TEXT", Context.MODE_PRIVATE, null)
        mainPath = context.getExternalFilesDir("/")?.absolutePath

        dataBase.execSQL("PRAGMA foreign_keys = ON;")
        //db.setForeignKeyConstraintsEnabled(true);
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_set")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_collections")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_texts")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_words")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_examples_collection")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_definitions")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_examples")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_images")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_audios")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_tags")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_wordTags")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_infoVar")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_wordText")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_related")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_folders")
        dataBase.execSQL("CREATE TABLE IF NOT EXISTS $DT_words_folder")

        tables = arrayOf(T_Sets, T_collections, T_texts, T_words, T_examples_collection, T_definitions, T_examples, T_images, T_audios,
            T_wordsText, T_relatedWord, T_tags, T_wordTags, T_infoVar, T_folders, T_words_Folder)
        initInsert()
    }

    private fun initInsert(){
        val q1 = "INSERT OR IGNORE INTO $T_Sets($A_setName) VALUES('${AllSet.toBase64()}');"
        val q2 = "INSERT OR IGNORE INTO $T_examples_collection VALUES('1', '${Default.toBase64()}')"

        dataBase.execSQL(q1)
        dataBase.execSQL(q2)
    }

    //endregion

    //endregion

    //region Tables

    //region sets
    fun insertSet(name: String, color: Int){
        dataBase.execSQL("INSERT INTO $T_Sets ($A_setName, $A_color) values ('${name.toBase64()}'  , $color)")
    }

    fun getSets() : ArrayList<Sets>{
        val result = ArrayList<Sets>()

        val sCursor = dataBase.rawQuery("SELECT * FROM $T_Sets", null)
        if(sCursor.moveToFirst()){
            do{
                val name = sCursor.getString(0).fromBase64ToString()
                val tagColor = sCursor.getInt(1)
                result.add(Sets(name, tagColor))
            }while (sCursor.moveToNext())
        }
        sCursor.close()
        return result
    }

    fun getSetColor(name: String) : Int{
        val default =  Color.rgb(0, 0, 0)
        val setName = name.toBase64()
        val sCursor = dataBase.rawQuery(
            "SELECT $A_color FROM $T_Sets WHERE $A_setName = '$setName'",
            null
        )
        val color = if(sCursor.moveToFirst()) sCursor.getInt(0) else default
        sCursor.close()
        return color
    }

    fun isSetNotExist(name: String) : Boolean{
        val q = "SELECT $A_setName FROM $T_Sets WHERE $A_setName = '${name.toBase64()}'"
        return tableCountByQuery(q) == 0
    }

    fun updateSet(name: String, newName: String){
        dataBase.execSQL("UPDATE $T_Sets SET $A_setName = '${newName.toBase64()}' WHERE $A_setName = '${name.toBase64()}'")
        dataBase.execSQL("UPDATE $T_collections SET $A_setName = '${newName.toBase64()}' WHERE $A_setName = '${name.toBase64()}'")
    }

    fun updateSetColor(name: String, newColor: Int){
        dataBase.execSQL("UPDATE $T_Sets SET $A_color = $newColor WHERE $A_setName = '${name.toBase64()}'")
    }

    fun deleteSet(n: String){
        val name = n.toBase64()

        transaction {
            dataBase.execSQL("DELETE FROM $T_Sets WHERE $A_setName = '$name'")
        }
    }

    //endregion

    //region collections

    fun insertCollection(n: String){
        val name = n.toBase64()
        val order = CollectionManagement.collections.count()
        val setName = SetManagement.getSelectedSet().toBase64()

        dataBase.execSQL("INSERT INTO $T_collections VALUES ('$name', $order, '$setName')")
    }

    fun getCollections(setName: String) : ArrayList<Collections>{
        val result = ArrayList<Collections>()
        val setNameF = setName.toBase64()
        val q = if(setName == AllSet){
            "SELECT * FROM $T_collections ORDER BY $A_order"
        }else{
            "SELECT * FROM $T_collections WHERE $A_setName = '$setNameF' ORDER BY $A_order"
        }

        val sCursor = dataBase.rawQuery(q, null)
        if(sCursor.moveToFirst()){
            do{
                val name = sCursor.getString(0).fromBase64ToString()
                val father = sCursor.getString(2).fromBase64ToString()
                result.add(Collections(name, father))
            }while (sCursor.moveToNext())
        }
        sCursor.close()
        return result
    }

    fun getCollection(n: String, s: String) : Collections?{
        val colName = n.toBase64()
        val setName = s.toBase64()
        val q = "SELECT * FROM $T_collections WHERE $A_setName = '$setName' AND $A_collectionName = '$colName'"
        val cursor = dataBase.rawQuery(q, null)
        var res : Collections? = null
        if(cursor.moveToFirst()){
            val name = cursor.getString(0).fromBase64ToString()
            val set = cursor.getString(2).fromBase64ToString()
            res = Collections(name, set)
        }
        cursor.close()
        return res
    }

    fun isCollectionNotExist(s: String, c: String) : Boolean{
        val setName = s.toBase64()
        val collection = c.toBase64()
        val q = "SELECT $A_collectionName FROM $T_collections " +
                "WHERE $A_setName = '$setName' AND $A_collectionName = '$collection'"
        return tableCountByQuery(q) == 0
    }

    fun updateCollection(c: Collections, newName: String){
        val colName = c.name.toBase64()
        val colFather = c.father.toBase64()
        val newNameB = newName.toBase64()
        dataBase.execSQL(
            "UPDATE $T_collections SET $A_collectionName = '$newNameB' " +
                    "WHERE $A_collectionName = '$colName' AND $A_setName = '$colFather'"
        )
    }

    fun deleteCollection(c: Collections){
        val col = c.name.toBase64()
        val set = c.father.toBase64()
        dataBase.execSQL("DELETE FROM $T_collections WHERE $A_collectionName = '$col' AND $A_setName = '$set';")
    }

    //endregion

    //region texts

    fun insertText(n: String){
        //texts(textTitle, text, order, #collectionName, #setName, posX, posY)
        val tile = n.toBase64()
        val order = TextManagement.texts.count()
        val colName = CollectionManagement.selectedCol.name.toBase64()
        val setName = CollectionManagement.selectedCol.father.toBase64()
        val q = "INSERT INTO $T_texts($A_textTitle, $A_text, $A_order, $A_collectionName, $A_setName, $A_posX, $A_posY) " +
                "VALUES ('$tile', '', $order, '$colName', '$setName', 0, 0)"

        dataBase.execSQL(q)
    }

    fun insertTextPos(id: Int, pos: WordPosItem){
        val word = pos.word.toBase64()
        val s = pos.s
        val e = pos.e
        dataBase.execSQL("INSERT INTO $T_wordsText VALUES($id, '$word', $s, $e)")
    }

    fun findAndInsertTextWordPos(mainText : String, start : Int, end : Int) : Int{
        val allWords = getListString("SELECT $A_word FROM $T_words")
        val text = mainText.substring(start, end)
        val id = TextManagement.selectedItem
        var i = 0
        transaction {
            for(item in allWords){
                val r = "(\\W|^)$item(\\W|$)".toRegex().findAll(text)
                for(match in r){
                    var s = match.range.first + start
                    var e = match.range.last + start
                    val nonChar1 = Regex("^\\W.*").matches(match.value)
                    val nonChar2 = Regex(".*\\W$").matches(match.value)
                    if(nonChar1)  s++
                    if(!nonChar2) e++
                    val w = WordPosItem(s, e, mainText.substring(s, e))
                    insertTextPos(id, w)
                    i++
                    //println("-->('${w.word}', ${w.s}, ${w.e}) ->(${match.value}, $nonChar1, $nonChar2)")
                }
            }
        }
        return i
    }

    fun getTexts() : ArrayList<Texts>{
        //texts(textTitle, text, wordsPos, order, #collectionName, #setName)
        val colName = CollectionManagement.selectedCol.name.toBase64()
        val setName = CollectionManagement.selectedCol.father.toBase64()
        val res = ArrayList<Texts>()
        val q = "SELECT rowId, $A_textTitle, $A_text FROM $T_texts WHERE $A_setName = '$setName' AND $A_collectionName = '$colName'"
        val cursor = dataBase.rawQuery(q, null)
        if(cursor.moveToFirst()){
            do{
                val id = cursor.getInt(0)
                val title = cursor.getString(1).fromBase64ToString()
                val text = cursor.getString(2).fromBase64ToString()
                res.add(Texts(id, title, text))
            }while(cursor.moveToNext())
        }
        cursor.close()
        return res
    }

    fun getTextCount(c: String, f: String) : Int{
        val colName = c.toBase64()
        val setName = f.toBase64()
        val q = "SELECT * FROM $T_texts WHERE $A_setName = '$setName' AND $A_collectionName = '$colName'"
        return tableCountByQuery(q)
    }

    fun getTextPosX(id: Int) : Int{
        return getInt("SELECT $A_posX FROM $T_texts WHERE rowId = $id")!!
    }

    fun getTextPosY(id: Int) : Int{
        return getInt("SELECT $A_posY FROM $T_texts WHERE rowId = $id")!!
    }

    fun getTextWordsPos(id: Int) : ArrayList<WordPosItem>{
        val res = ArrayList<WordPosItem>()
        val q = "SELECT $A_word, $A_posStart, $A_posEnd FROM $T_wordsText WHERE $A_textID = $id"
        iterationCursor(q){
            val word = it.getString(0).fromBase64ToString()
            val s = it.getInt(1)
            val e = it.getInt(2)
            res.add(WordPosItem(s, e, word))
        }
        return res
    }

    fun getTextWordsCount() : HashMap<Int, Int>{
        val res = HashMap<Int, Int>()
        val q = "SELECT id, COUNT(*) FROM $T_wordsText group by $A_textID"

        val cursor = dataBase.rawQuery(q, null)
        if(cursor.moveToFirst()){
            do{
                val id = cursor.getInt(0)
                val count = cursor.getInt(1)
                res[id] = count
            }while (cursor.moveToNext())
        }
        cursor.close()
        return res
    }

    fun updateTextTitle(id: Int, t: String){
        val title = t.toBase64()
        val q = "UPDATE $T_texts SET $A_textTitle = '$title' WHERE rowId = $id"
        dataBase.execSQL(q)
    }

    fun updateTextText(id: Int, t: String){
        val text = t.toBase64()

        val q = "UPDATE $T_texts SET $A_text = '$text' WHERE rowId = $id"
        dataBase.execSQL(q)
    }

    fun updateTextWordsPos(id: Int, words: ArrayList<WordPosItem>){
        //wordsText(textId, wordName, posStart, posEnd)


        transaction {
            dataBase.execSQL("DELETE FROM $T_wordsText WHERE $A_textID = $id")
            words.forEach { item->
                val q = "INSERT INTO $T_wordsText ($A_textID, $A_word, $A_posStart, $A_posEnd ) VALUES (?, ?, ?, ?)"
                val statement = dataBase.compileStatement(q)
                statement.bindLong(1, id.toLong())
                statement.bindString(2, item.word.toBase64())
                statement.bindLong(3, item.s.toLong())
                statement.bindLong(4, item.e.toLong())

                statement.executeInsert()
                statement.close()
            }
        }
    }

    fun deleteTexts(list: ArrayList<Int>){
        val res = ArrayList<String>()
        val l = TextManagement.texts
        for(pos in list){ res.add(l[pos].id.toString()) }

        val theList = res.toListSql()
        transaction {
            dataBase.execSQL("DELETE FROM $T_texts WHERE rowId IN $theList")
            dataBase.execSQL("DELETE FROM $T_wordsText WHERE $A_textID IN $theList")
        }
    }

    fun deleteTextWordPos(id: Int, wordPos: ArrayList<WordPosItem>){
        transaction {
            wordPos.forEach {
                val s = it.s
                val e = it.e
                val q = "DELETE FROM $T_wordsText WHERE $A_textID = $id AND $A_posStart = $s AND $A_posEnd = $e"
                dataBase.execSQL(q)
            }
        }
    }

    fun deleteTextWords(id : Int, words : ArrayList<String>){
        transaction {
            words.forEach{
                val q = "DELETE FROM $T_wordsText WHERE $A_textID = $id AND $A_word = '${it.toBase64()}'"
                dataBase.execSQL(q)
            }
        }
    }


    fun updateTextPosX(id: Int, newValue: Int){
        dataBase.execSQL("UPDATE $T_texts SET $A_posX = $newValue WHERE rowId = $id")
    }

    fun updateTextPosY(id: Int, newValue: Int){
        dataBase.execSQL("UPDATE $T_texts SET $A_posY = $newValue WHERE rowId = $id")
    }

    //endregion

    //region Vars



    fun getVar(varId: Int, defaultValue: String) : String{
        val isExist = "SELECT $A_varId FROM $T_infoVar WHERE $A_varId = $varId"
        if(tableCountByQuery(isExist) == 0){
            dataBase.execSQL("INSERT INTO $T_infoVar VALUES($varId, '${defaultValue.toBase64()}')")
        }else{
            val cursor = dataBase.rawQuery(
                "SELECT $A_value FROM $T_infoVar WHERE $A_varId = $varId",
                null
            )
            if(cursor.moveToFirst()){
                return cursor.getString(0).fromBase64ToString()
            }
            cursor.close()
        }
        return defaultValue
    }

    fun updateVar(varId: Int, newValue: String){
        dataBase.execSQL(
            "UPDATE $T_infoVar SET $A_value = '${newValue.toBase64()}' " +
                    "WHERE $A_varId = '$varId'"
        )
    }

    //endregion

    //region words, def, exp, img, audio, related

    fun insertWord(n: String){
        val name = n.toBase64()

        val currentTime = System.currentTimeMillis()
        dataBase.execSQL("INSERT OR IGNORE INTO $T_words($A_word, $A_favorite, $A_created_time) VALUES('$name', '0', $currentTime)")
    }

    fun insertWordsFromDefinedText(list : ArrayList<WordFile>){
        transaction {
            //number of examples of an existing word should not excited EXAMPLES_MAX
            val collectionExp = if (MainSetting.selectedExamplesCollection.get() > 0) MainSetting.selectedExamplesCollection.get() else DEFAULT_EXAMPLE_COLLECTION
            val wordExpNbrQuery = "select $A_word, COUNT(*) from $T_examples where $A_example_col_id = '$collectionExp' group by $A_word;"
            val wordExpNbrMap = getWordsDefExpNbr(wordExpNbrQuery)

            for(item in list){
                val examplesNbr = wordExpNbrMap[item.word] ?: 0
                insertWord(item.word)

                for(def in item.definitions){
                    insertDefinition(item.word, def)
                }
                if (examplesNbr < EXAMPLES_MAX) {
                    for(exp in item.examples){
                        insertExamples(item.word, exp, collectionExp)
                    }
                }
            }
            when(FileManagement.fgType){
                "Tags" ->{
                    val tag = FileManagement.passedData.toBase64()
                    for(item in list){
                        val word = item.word.toBase64()
                        val q = "INSERT OR IGNORE INTO $T_wordTags VALUES('$word', '$tag')"
                        dataBase.execSQL(q)
                    }
                }
                "Folders" ->{
                    val p = FileManagement.passedData.toBase64()
                    for(item in list){
                        val n = item.word.toBase64()
                        val q = "Insert OR IGNORE into $T_words_Folder($A_word, $A_path) values('$n', '$p')"
                        dataBase.execSQL(q)
                    }
                }
            }
        }
    }

    fun isWordNotExist(n: String) : Boolean{
        val name = n.toBase64()
        val q = "SELECT $A_word FROM $T_words WHERE $A_word = '$name'"
        return tableCountByQuery(q) == 0
    }

    fun getWords(query: String) : ArrayList<Word>{
        val res = ArrayList<Word>()

        val cursor = dataBase.rawQuery(query, null)
        if(cursor.moveToFirst()){
            do{
                val name = cursor.getString(0).fromBase64ToString()
                val isFavorite = cursor.getInt(1) == 1
                val isKnown = cursor.getInt(2)
                res.add(Word(name, isFavorite, isKnown))
            }while (cursor.moveToNext())
        }
        cursor.close()
        return res
    }

    fun getWordsHasImages() : HashMap<String, Boolean> {
        val q = "SELECT Distinct $A_word FROM $T_images"
        return getWordsHasMedia(q)
    }

    fun getWordsHasAudios() : HashMap<String, Boolean> {
        val q = "SELECT Distinct $A_word FROM $T_audios"
        return getWordsHasMedia(q)
    }

    fun getWordsHasTag() : HashMap<String, Boolean>{
        val q = "SELECT Distinct $A_word FROM $T_wordTags"
        return getWordsHasMedia(q)
    }

    fun getWordsHasFolder() : HashMap<String, Boolean>{
        val q = "SELECT Distinct $A_word FROM $T_words_Folder"
        return getWordsHasMedia(q)
    }

    fun getWordsHasText() : HashMap<String, Boolean>{
        val q = "SELECT Distinct $A_word FROM $T_wordsText"
        return getWordsHasMedia(q)
    }

    fun getWordsHasRelated() : HashMap<String, Boolean>{
        val q = "select $A_word from $T_relatedWord union select $A_related from $T_relatedWord"
        return getWordsHasMedia(q)
    }

    fun getWordsHasDefinition() : HashMap<String, Boolean>{
        val q = "select distinct $A_word from $T_definitions"
        return getWordsHasMedia(q)
    }

    fun getWordsHasExample() : HashMap<String, Boolean>{
        val q = "select distinct $A_word from $T_examples"
        return getWordsHasMedia(q)
    }

    private fun getWordsHasMedia(q : String) : HashMap<String, Boolean>{
        val res = HashMap<String, Boolean>()
        val cursor = dataBase.rawQuery(q, null)
        if(cursor.moveToFirst()){
            do{
                val name = cursor.getString(0).fromBase64ToString()
                res[name] = true
            }while (cursor.moveToNext())
        }
        cursor.close()
        return res
    }

    private fun getWordsDefExpNbr(q : String) : HashMap<String, Int>{
        val res = HashMap<String, Int>()
        val cursor = dataBase.rawQuery(q, null)
        if(cursor.moveToFirst()){
            do{
                val name = cursor.getString(0).fromBase64ToString()
                val nbr = cursor.getInt(1)
                res[name] = nbr
            }while (cursor.moveToNext())
        }
        cursor.close()
        return res
    }

    fun deleteWords(path: String, w: ArrayList<String>){
        val words = w.toBase64()
        val images = getListString("SELECT $A_imageName FROM $T_images WHERE $A_word IN $words")
        val audios = getListString("SELECT $A_audioName FROM $T_audios WHERE $A_word IN $words")
        val files = ArrayList<String>()
        for(item in images) files.add("$path/$IMAGE_FOLDER/$item")
        for(item in audios) files.add("$path/$AUDIO_FOLDER/$item")

        println(">>>$files")
        transaction {
            dataBase.execSQL("DELETE FROM $T_words WHERE $A_word IN $words")
            dataBase.execSQL("DELETE FROM $T_wordsText WHERE $A_word IN $words")
            MediaManagement.deleteFiles(files)
        }
    }

    //region getMedia
    fun getWordFavorite(n: String) : Boolean{
        val name = n.toBase64()
        val q = "SELECT $A_favorite FROM $T_words WHERE $A_word = '$name'"
        val cursor = dataBase.rawQuery(q, null)
        val isFavorite = if(cursor.moveToFirst()) cursor.getInt(0) == 1 else false
        cursor.close()
        return isFavorite
    }

    fun getWordIsKnown(n: String) : Int{
        val name = n.toBase64()
        val q = "SELECT $A_isKnown FROM $T_words WHERE $A_word = '$name'"
        val cursor = dataBase.rawQuery(q, null)
        val isKnown = if(cursor.moveToFirst()) cursor.getInt(0) else 0
        cursor.close()
        return isKnown
    }

    fun getWordExamples(n: String, collectionId : Int) : ArrayList<WordInfoId>{
        val name = n.toBase64()

        val q1 = "Select $A_related FROM $T_relatedWord WHERE $A_word = '$name'"
        val q2 = "Select $A_word FROM $T_relatedWord WHERE $A_related = '$name' UNION SELECT '$name'"
        //val q1 = "SELECT $A_related FROM $T_relatedWord WHERE $A_word = '$name' UNION SELECT '$name'"
        var q = "SELECT rowId, $A_example, $A_word FROM $T_examples WHERE $A_word IN ($q1 UNION $q2)"
        if (collectionId > 0)
            q += " and $A_example_col_id = '$collectionId';"
        println(">>> $q")
        return getListWordInfoId(q)
    }

    fun getWordDefinitions(n: String) : ArrayList<WordInfoId>{
        val name = n.toBase64()
        //val q = "SELECT rowId, $A_definition FROM $T_definitions WHERE $A_word = '$name'"
        val q1 = "Select $A_related FROM $T_relatedWord WHERE $A_word = '$name'"
        val q2 = "Select $A_word FROM $T_relatedWord WHERE $A_related = '$name' UNION SELECT '$name'"
        //val q1 = "SELECT $A_related FROM $T_relatedWord WHERE $A_word = '$name' UNION SELECT '$name'"
        val q = "SELECT rowId, $A_definition, $A_word FROM $T_definitions WHERE $A_word IN ($q1 UNION $q2)"

        return getListWordInfoId(q)
    }

    fun getWordImages(n: String) : ArrayList<WordInfoId>{
        val name = n.toBase64()

        val q1 = "Select $A_related FROM $T_relatedWord WHERE $A_word = '$name'"
        val q2 = "Select $A_word FROM $T_relatedWord WHERE $A_related = '$name' UNION SELECT '$name'"
        val q = "SELECT rowId, $A_imageName, $A_word FROM $T_images WHERE $A_word IN ($q1 UNION $q2)"

        return getListWordInfoId(q)
    }

    fun getWordAudios(n: String) : ArrayList<WordInfoId>{
        val name = n.toBase64()

        val q1 = "Select $A_related FROM $T_relatedWord WHERE $A_word = '$name'"
        val q2 = "Select $A_word FROM $T_relatedWord WHERE $A_related = '$name' UNION SELECT '$name'"
        val q = "SELECT rowId, $A_audioName, $A_word FROM $T_audios WHERE $A_word IN ($q1 UNION $q2)"

        return getListWordInfoId(q)
    }

    fun getRelatedWord(w: String) : ArrayList<StringId> {
        val word = w.toBase64()
        val q1 = "Select rowId, $A_related FROM $T_relatedWord WHERE $A_word = '$word'"
        val q2 = "Select rowId, $A_word FROM $T_relatedWord WHERE $A_related = '$word'"
        return getListStringId("$q1 UNION $q2")
    }

    fun getRelatedWordSuggestion(w: String) : ArrayList<String>{
        val word = w.toBase64()
        val q1 = "Select $A_related FROM $T_relatedWord WHERE $A_word = '$word'"
        val q2 = "Select $A_word FROM $T_relatedWord WHERE $A_related = '$word'"
        val q3 = "SELECT $A_word FROM $T_words WHERE $A_word NOT IN ($q1 UNION $q2) AND $A_word <> '$word'"
        return getListString(q3)
    }

    fun isRelatedNotExist(w: String, r: String) : Boolean{
        val word = w.toBase64()
        val related = r.toBase64()
        val q = "SELECT * FROM $T_relatedWord WHERE $A_word = '$word' AND $A_related = '$related'"
        return tableCountByQuery(q) == 0
    }

    //endregion

    //region insertMedia

    fun insertExamples(w: String, e: String, selectedCollection: Int){
        val word = w.toBase64()
        val example = e.toBase64()
        dataBase.execSQL("INSERT OR IGNORE INTO $T_examples ($A_word, $A_example, $A_example_col_id) VALUES ('$word', '$example', '$selectedCollection')")
    }

    fun insertDefinition(w: String, d: String){
        val word = w.toBase64()
        val definition = d.toBase64()

        dataBase.execSQL("INSERT OR IGNORE INTO $T_definitions ($A_word, $A_definition) VALUES ('$word', '$definition')")
    }

    fun insertImage(w: String, im: String){
        val word = w.toBase64()
        val statement = dataBase.compileStatement("INSERT INTO $T_images ($A_word, $A_imageName) VALUES (?, ?)")

        statement.bindString(1, word)
        statement.bindString(2, im.toBase64())
        statement.executeInsert()
        statement.close()
    }

    fun insertAudio(w: String, a: String){
        val word = w.toBase64()
        val audio = a.toBase64()

        dataBase.execSQL("INSERT INTO $T_audios ($A_word, $A_audioName) VALUES ('$word', '$audio')")
    }

    fun insertRelated(w: String, r: String){
        val word = w.toBase64()
        val related = r.toBase64()
        val q = "INSERT INTO $T_relatedWord VALUES('$word', '$related')"
        dataBase.execSQL(q)
    }

    //endregion

    //region updateMedia

    fun updateWordExample(id: Int, c: String){
        val newContent = c.toBase64()

        dataBase.execSQL("UPDATE $T_examples SET $A_example = '$newContent' WHERE rowId = $id")
    }

    fun updateWordDefinition(id: Int, c: String){
        val newContent = c.toBase64()

        dataBase.execSQL("UPDATE $T_definitions SET $A_definition = '$newContent' WHERE rowId = $id")
    }

    fun updateWordFavorite(n: String, v: Boolean){
        val name = n.toBase64()
        val value = v.toInt()
        dataBase.execSQL("UPDATE $T_words SET $A_favorite = $value WHERE $A_word = '$name'")
    }

    fun updateIsWordKnown(l : ArrayList<String>){
        val list = l.toBase64()
        val q = "SELECT $A_word FROM $T_words WHERE $A_word IN $list AND $A_isKnown = 4"
        val alreadyKnown = getListString(q).toBase64()

        //println("UPDATE $T_words SET $A_isKnown = 1 WHERE $A_word IN $list")
        //println("UPDATE $T_words SET $A_isKnown = 0 WHERE $A_word IN $alreadyKnown")
        dataBase.execSQL("UPDATE $T_words SET $A_isKnown = 4 WHERE $A_word IN $list")
        dataBase.execSQL("UPDATE $T_words SET $A_isKnown = 0 WHERE $A_word IN $alreadyKnown")
    }

    fun updateSetVisited(w: String){
        val word = w.toBase64()
        val q = "Update $T_words set $A_isKnown = 2 where $A_word = '$word' and $A_isKnown = 0"
        dataBase.execSQL(q)
    }

    fun updateSetArchived(w : String){
        val word = w.toBase64()
        val q = "Update $T_words set $A_isKnown = 1 where $A_word = '$word'"
        dataBase.execSQL(q)
    }

    fun updateIsFavoriteWords(w: ArrayList<String>){
        val list = w.toBase64()
        val q = "SELECT $A_word FROM $T_words WHERE $A_word IN $list AND $A_favorite = 1"
        val alreadyFavorite = getListString(q).toBase64()
        dataBase.execSQL("UPDATE $T_words SET $A_favorite = 1 WHERE $A_word IN $list")
        dataBase.execSQL("UPDATE $T_words SET $A_favorite = 0 WHERE $A_word IN $alreadyFavorite")

    }

    //endregion

    //region deleteMedia

    fun deleteExample(id: Int){
        dataBase.execSQL("DELETE FROM $T_examples WHERE rowId = $id")
    }

    fun deleteDefinition(id: Int){
        dataBase.execSQL("DELETE FROM $T_definitions WHERE rowId = $id")
    }

    fun deleteImages(list: ArrayList<Int>){
        val res = ArrayList<String>()
        val l = MediaManagement.images
        for(pos in list){ res.add(l[pos].id.toString()) }

        val theList = res.toListSql()
        dataBase.execSQL("DELETE FROM $T_images WHERE rowId IN $theList")
    }

    fun deleteAudios(list: ArrayList<Int>){
        val res = ArrayList<String>()
        val l = MediaManagement.audios
        for(pos in list){ res.add(l[pos].id.toString()) }

        val theList = res.toListSql()
        dataBase.execSQL("DELETE FROM $T_audios WHERE rowId IN $theList")
    }

    fun deleteRelated(list: ArrayList<Int>){
        val l = list.intToListSql()
        dataBase.execSQL("DELETE FROM $T_relatedWord WHERE rowId IN $l")
    }

    //endregion

    //endregion

    //region tags

    fun getTags() : ArrayList<StringId>{
        val q = "SELECT rowId, $A_tag FROM $T_tags"
        return getListStringId(q)
    }

    fun getExpCollection() : ArrayList<StringId>{
        val q = "SELECT $A_example_col_id, $A_example_col FROM $T_examples_collection"
        return getListStringId(q)
    }

    fun getTagSuggestionList(w: String) : ArrayList<String>{
        val word = w.toBase64()
        val q = "SELECT $A_tag FROM $T_tags WHERE $A_tag NOT IN (SELECT $A_tag FROM $T_wordTags WHERE $A_word = '$word')"
        return getListString(q)
    }

    fun getTagsCount() : HashMap<String, Int>{
        val tags = HashMap<String, Int>()
        val q = "SELECT tag, count(*) FROM $T_wordTags GROUP BY tag;"
        iterationCursor(q){
            val tag = it.getString(0).fromBase64ToString()
            val count = it.getInt(1)
            tags[tag] = count
        }
        return tags
    }

    fun getExpCollectionCount() : HashMap<Int, Int>{
        val res = HashMap<Int, Int>()
        val q = "SELECT $A_example_col_id, count(*) FROM $T_examples GROUP BY $A_example_col_id;"
        iterationCursor(q){
            val tag = it.getInt(0)
            val count = it.getInt(1)
            res[tag] = count
        }
        return res
    }

    fun getWordTags(w: String) : ArrayList<WordInfoId>{
        val word = w.toBase64()

        val q1 = "Select $A_related FROM $T_relatedWord WHERE $A_word = '$word'"
        val q2 = "Select $A_word FROM $T_relatedWord WHERE $A_related = '$word' UNION SELECT '$word'"
        val q = "SELECT rowId, $A_tag, $A_word FROM $T_wordTags WHERE $A_word IN ($q1 UNION $q2)"
        return getListWordInfoId(q)
    }

    fun insertTag(t: String){
        val tag = t.toBase64()
        val q = "INSERT OR IGNORE INTO $T_tags VALUES('$tag')"
        dataBase.execSQL(q)
    }

    fun insertExampleCollection(t: String){
        val name = t.toBase64()
        val q = "INSERT OR IGNORE INTO $T_examples_collection($A_example_col) VALUES('$name')"
        dataBase.execSQL(q)
    }

    fun insertWordTag(w: String, t: String){
        val word = w.toBase64()
        val tag = t.toBase64()
        val q = "INSERT OR IGNORE INTO $T_wordTags VALUES('$word', '$tag')"
        insertTag(t)
        dataBase.execSQL(q)
    }

    fun isTagNotExist(t: String) : Boolean{
        val tag = t.toBase64()
        val q = "SELECT * FROM $T_tags WHERE $A_tag = '$tag';"
        return tableCountByQuery(q) == 0
    }

    fun isExampleCollectionExist(t: String) : Boolean{
        val tag = t.toBase64()
        val q = "SELECT * FROM $T_examples_collection WHERE $A_example_col = '$tag';"
        return tableCountByQuery(q) == 0
    }

    fun isWordTagNotExist(w: String, t: String) : Boolean{
        val word = w.toBase64()
        val tag = t.toBase64()
        val q = "SELECT * FROM $T_wordTags WHERE $A_word = '$word' AND $A_tag = '$tag';"
        return tableCountByQuery(q) == 0
    }

    fun updateTags(id: Int, t: String){
        val tag = t.toBase64()
        val q = "UPDATE $T_tags SET $A_tag = '$tag' WHERE rowId = $id"
        dataBase.execSQL(q)
    }

    fun updateExampleCollection(id: Int, t: String){
        val name = t.toBase64()
        val q = "UPDATE $T_examples_collection SET $A_example_col = '$name' WHERE $A_example_col_id = $id"
        dataBase.execSQL(q)
    }

    fun deleteTags(list: ArrayList<Int>){
        val l = list.intToListSql()
        dataBase.execSQL("DELETE FROM $T_tags WHERE rowId IN $l")
    }

    fun deleteExamplesCollection(list: ArrayList<Int>){
        //DEFAULT_EXAMPLE_COLLECTION should not be deleted from T_examples_collection table
        transaction {
            if (DEFAULT_EXAMPLE_COLLECTION in list){
                list.remove(DEFAULT_EXAMPLE_COLLECTION)
                dataBase.execSQL("DELETE FROM $T_examples WHERE $A_example_col_id = '$DEFAULT_EXAMPLE_COLLECTION'")
            }
            val l = list.intToListSql()
            dataBase.execSQL("DELETE FROM $T_examples_collection WHERE $A_example_col_id IN $l")
        }
    }

    fun deleteWordTags(list: ArrayList<Int>){
        val l = list.intToListSql()
        dataBase.execSQL("DELETE FROM $T_wordTags WHERE rowId IN $l")
    }

    fun deleteWordsFromTag(tag : String, list: ArrayList<String>){
        val l = list.toBase64()
        val t = tag.toBase64()
        dataBase.execSQL("DELETE FROM $T_wordTags WHERE $A_tag = '$t' AND $A_word IN $l")
    }

    //endregion

    //region Folders and WordsFolder
    //path always start with './'

    //region insert

    fun isFolderExist(path: String) : Boolean{
        val p = path.toBase64()
        return tableCountByQuery("Select * From $T_folders Where $A_path = '$p'")!=0
    }

    fun isFolderNameValid(name : String) : Boolean{
        if(!name.contains("/")){
            return true
        }
        return false
    }

    fun isWordExistInFolder(path: String, name : String) : Boolean{
        val p = path.toBase64()
        val n = name.toBase64()
        return tableCountByQuery("Select * From $T_words_Folder Where $A_path = '$p' And $A_word = '$n'")!=0
    }

    fun insertFolder(path : String, name : String){
        //warning : path can't be empty
        val p = ("$path/$name").toBase64()

        val q = "Insert into $T_folders values('$p')"
        dataBase.execSQL(q)
    }

    fun insertWordInFolder(path : String, name : String){
        //warning : path can't be empty
        val p = path.toBase64()
        val n = name.toBase64()

        val q = "Insert OR IGNORE into $T_words_Folder($A_word, $A_path) values('$n', '$p')"
        dataBase.execSQL(q)
    }


    //endregion

    //region get

    private fun String.isParentFolderOf(path : String) : Boolean{
        /*
        paths
        ./A/B/1
        ./A/B/2
        ./A/B/3
        ./A/B/F/4
        ./A not count
        ./A/B not count

        item = ./A/B
        */
        val splitPath = path.split("/")
        val splitItem = this.split("/")
        if(splitPath.count() > splitItem.count()){
            for(i in 0 until splitItem.count()){
                if(splitItem[i] != splitPath[i])
                    return false
            }
        }else{
            return false
        }
        return true
    }

    fun getListOfFolders(path : String) : ArrayList<String>{
        val result = ArrayList<String>()

        val q = "Select * From $T_folders"
        val allFolders = getListString(q)

        for(folder in allFolders){
            if(path.isParentFolderOf(folder)){
                if(folder.split("/").count() == path.split("/").count() + 1){
                    result.add(folder)
                }
            }
        }
        return result
    }

    //endregion

    //region update

    fun updateFolderName(path: String, newPath : String){
        val p = path.toBase64()
        val nP = newPath.toBase64()

        val allFoldersQuery = "Select $A_path From $T_folders"
        val allFolders = getListString(allFoldersQuery)
        transaction {
            for(folder in allFolders){
                if(path.isParentFolderOf(folder)){
                    val theNewPath = newPath + folder.substring(path.count(), folder.count())
                    val q = "update $T_folders set $A_path = '${theNewPath.toBase64()}' where $A_path = '${folder.toBase64()}'"
                    dataBase.execSQL(q)
                }
            }
            val q = "update $T_folders set $A_path = '$nP' where $A_path = '$p'"
            dataBase.execSQL(q)
        }
    }

    fun getFoldersWordsNumber() : HashMap<String, Int>{
        val res = HashMap<String, Int>()
        val q = "Select $A_path, count(*) from $T_words_Folder group by $A_path"
        val cursor = dataBase.rawQuery(q, null)
        if(cursor.moveToFirst()){
            do{
                val path = cursor.getString(0).fromBase64ToString()
                val wordsNbr = cursor.getInt(1)
                res[path] = wordsNbr
            }while (cursor.moveToNext())
        }
        cursor.close()
        return res
    }

    //endregion

    //region delete

    fun deleteFolders(list : ArrayList<String>){
        val deleteList = ArrayList<String>()

        val allFoldersQuery = "Select * from $T_folders"
        val allFolders = getListString(allFoldersQuery)

        for(folder in allFolders){
            for(deleteItem in list){
                if(deleteItem.isParentFolderOf(folder) || folder == deleteItem){
                    deleteList.add(folder)
                }
            }
        }

        transaction {
            for(item in deleteList){
                val p = item.toBase64()
                val q = "delete from $T_folders where $A_path = '$p'"
                dataBase.execSQL(q)
            }
        }

    }

    fun deleteWordsFromFolder(path : String, list : ArrayList<String>){
        val p = path.toBase64()

        transaction {
            for(item in list){
                val n = item.toBase64()
                val q = "delete from $T_words_Folder where $A_path = '$p' and $A_word = '$n'"
                dataBase.execSQL(q)
            }
        }
    }

    //endregion

    //region actions

    fun copyWordsToFolder(path : String, listOfWords : ArrayList<String>){
        val p = path.toBase64()

        transaction {
            for(item in listOfWords){
                val word = item.toBase64()
                val q = "INSERT OR IGNORE INTO $T_words_Folder($A_word, $A_path) VALUES('$word', '$p')"
                dataBase.execSQL(q)
            }
        }

    }

    //endregion

    //endregion

    //endregion

    //region General
    private fun getInt(q: String) : Int?{
        val cursor = dataBase.rawQuery(q, null)
        val res = if(cursor.moveToFirst()) cursor.getInt(0) else null
        cursor.close()
        return res
    }

    private fun getListStringId(query: String) : ArrayList<StringId>{
        val result = ArrayList<StringId>()

        val sCursor = dataBase.rawQuery(query, null)
        if(sCursor.moveToFirst()){
            do{
                val id = sCursor.getInt(0)
                val n = sCursor.getString(1).fromBase64ToString()
                result.add(StringId(id, n))
            }while (sCursor.moveToNext())
        }
        sCursor.close()
        return result
    }

    private fun getListWordInfoId(query: String) : ArrayList<WordInfoId>{
        val result = ArrayList<WordInfoId>()

        val sCursor = dataBase.rawQuery(query, null)
        if(sCursor.moveToFirst()){
            do{
                val id = sCursor.getInt(0)
                val n = sCursor.getString(1).fromBase64ToString()
                val w = sCursor.getString(2).fromBase64ToString()
                result.add(WordInfoId(id, n, w))
            }while (sCursor.moveToNext())
        }
        sCursor.close()
        return result
    }

    fun getListString(query: String) : ArrayList<String>{
        val result = ArrayList<String>()

        val sCursor = dataBase.rawQuery(query, null)
        if(sCursor.moveToFirst()){
            do{
                result.add(sCursor.getString(0).fromBase64ToString())
            }while (sCursor.moveToNext())
        }
        sCursor.close()
        return result
    }

    fun getListInt(query: String) : ArrayList<Int>{
        val result = ArrayList<Int>()

        val sCursor = dataBase.rawQuery(query, null)
        if(sCursor.moveToFirst()){
            do{
                result.add(sCursor.getInt(0))
            }while (sCursor.moveToNext())
        }
        sCursor.close()
        return result
    }
    //endregion

    //region section of utilities

    private fun fromListStringToString(
        l: ArrayList<String>,
        format: Boolean = false,
        toBase64: Boolean = true
    ) : String{
        return if(format){
            val newList = ArrayList<String>(l.count())
            for(item in l){
                if(toBase64){
                    newList.add("'${item.toBase64()}'")
                }else{
                    newList.add("'$item'")
                }
            }
            newList.toString().replace("[", "(").replace("]", ")")
        }else{
            l.toString().replace("[", "(").replace("]", ")")
        }
    }

    private fun ArrayList<String>.toBase64() : String{
        return fromListStringToString(this, format = true, toBase64 = true)
    }

    private fun ArrayList<String>.toListSql() : String{
        return fromListStringToString(this, false)
    }

    private fun ArrayList<Int>.intToListSql() : String{
        return this.toString().replace("^\\[".toRegex(), "(")
                .replace("]$".toRegex(), ")")
    }

    fun tableCountByQuery(sql: String) : Int{
        val cursor = dataBase.rawQuery(sql, null)
        val count = cursor.count
        cursor.close()
        return count
    }

    private fun transaction(event: () -> Unit){
        dataBase.beginTransaction()
        try{

            event()

            dataBase.setTransactionSuccessful()
        }catch (e: Exception){
            println(">>> ${e.printStackTrace()}")
        }finally {
            dataBase.endTransaction()
        }
    }

    fun String.toBase64() : String{
        val bytes = this.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT).replace("[\\x0a]".toRegex(), "")
    }

    fun String.fromBase64ToString() : String{
        return try{
            val originByte = Base64.decode(this, Base64.DEFAULT)
            return String(originByte)
        }catch (e : Exception){
            "???"
        }
    }

    private fun Boolean.toInt() : Int{
        if(this) return 1
        return 0
    }

    private fun iterationCursor(q: String, event: (Cursor) -> Unit){
        val cursor = dataBase.rawQuery(q, null)
        if(cursor.moveToFirst()){
            do{
                event(cursor)
            }while (cursor.moveToNext())
        }
        cursor.close()
    }

    //endregion

    //region load-save
    //region save Data

    fun saveData(context: Context, uri: Uri){
        try {
            val masterPath = context.getExternalFilesDir("/")!!.absolutePath
            val path = "$masterPath/$FILES_FOLDER"

            File(path).mkdir()
            for(item in tables){
                println(">>> Try to Save $item")
                saveTable(path, item)
            }

            ZipManager.zipFolder(context, context.getExternalFilesDir("/")!!, uri)
            deleteRecursive(File("$masterPath/$FILES_FOLDER"))
        }catch (e : Exception){
            Handler(context.mainLooper).post {
                println(e.printStackTrace())
                Lib.showMessage(context, "Error Save Failed")
            }
        }
    }

    private fun saveTable(path: String, table: String){
        val file = File(path, "$table.txt")
        file.writeText("")
        val q = "SELECT * FROM $table"
        val cursor = dataBase.rawQuery(q, null)
        val col = cursor.columnCount
        if(cursor.moveToFirst()){
            do{
                val res = StringBuilder()
                for(i in 0 until col){
                    res.append(cursor.getString(i))
                    res.append("||")
                }
                file.appendText(res.toString().replace("\\|\\|$".toRegex(), "\n"))
            }while (cursor.moveToNext())
        }
        cursor.close()
    }

    //endregion

    //region load

    fun loadData(context: Context, uri: Uri){
        try{
            val masterPath = context.getExternalFilesDir("/")!!.absolutePath
            val path = "$masterPath/$FILES_FOLDER"
            val stream = FileInputStream(context.contentResolver.openFileDescriptor(uri, "r")!!.fileDescriptor)


            ZipManager.unzip(stream, File(masterPath))

            if(checkFolders(masterPath)){
                transaction {
                    for(item in tables)
                        loadTable(path, item)
                }
                deleteRecursive(File("$masterPath/$FILES_FOLDER"))
                deleteNotRegisterMedia(masterPath)
                Handler(context.mainLooper).post {
                    Lib.showMessage(context, "Load Done")
                }
            }else{
                println(">>>Folders Not Valid")
                Handler(context.mainLooper).post {
                    Lib.showMessage(context, "Load failed Something Went Wrong")
                }
            }

        }catch (e : Exception){
            Handler(context.mainLooper).post {
                println(e.printStackTrace())
                Lib.showMessage(context, "Error Something Went Wrong")
            }
        }
    }

    private fun checkFolders(masterPath : String) : Boolean{
        val listFiles = File(masterPath).listFiles()
        val listOrigin = arrayOf(AUDIO_FOLDER, IMAGE_FOLDER, FILES_FOLDER)
        var fileExist = false
        if(listFiles != null){
            //check is file folder exist
            for(item in listFiles){
                if(item.name == FILES_FOLDER){
                    fileExist = true
                    break
                }
            }
            //delete all not target folders
            for(item in listFiles)
                if(item.name !in listOrigin)
                    deleteRecursive(item)
            return fileExist
        }
        return false
    }

    private fun deleteNotRegisterMedia(path : String){
        val d = getListString("SELECT $A_imageName FROM $T_images")
        val f = File("$path/$IMAGE_FOLDER").listFiles()
        if(f != null){
            for(item in f){
                if(item.name !in d)
                    deleteRecursive(item)
            }
        }

        val da = getListString("SELECT $A_audioName FROM $T_audios")
        val fa = File("$path/$AUDIO_FOLDER").listFiles()
        if(fa != null){
            for(item in fa){
                if(item.name !in da)
                    deleteRecursive(item)
            }
        }
    }

    private fun loadTable(path: String, table: String){
        val f = File(path, "$table.txt")
        val colNumber = tableCountByQuery("pragma table_info($table);")
        if(f.exists()){

            dataBase.execSQL("DELETE FROM $table")
            f.forEachLine {
                //val data = ArrayList(it.split("||"))
                val data = it.split("||")
                //println("--> $table || ${data.size}. $data")
                if(data.size == colNumber){
                    val res = StringBuilder()
                    for(item in data)
                    {
                        res.append("'$item',")
                    }
                    val value = res.toString().replace(",$".toRegex(), "")
                    println(">>> ${"INSERT INTO $table VALUES($value)"}")
                    dataBase.execSQL("INSERT INTO $table VALUES($value)")
                }
            }
        }else{
            println(">>>${table}.txt Not Exist")
        }
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory){
            val l = fileOrDirectory.listFiles()
            if(l != null){
                for (child in l)
                    deleteRecursive(child)
            }
        }
        fileOrDirectory.delete()
    }
    //endregion
    //endregion
}