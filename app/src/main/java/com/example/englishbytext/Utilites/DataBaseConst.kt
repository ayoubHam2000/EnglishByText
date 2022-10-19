package com.example.englishbytext

/*
ALTER TABLE words ADD COLUMN created_time Date;

delete from words where word in (
select word from words where word not in (
select word from relatedWord union
select related from relatedWord union
select word from wordsfolder union
select word from wordTags union
select word from wordsText)
);

 */

//sets(*setName, color)
//collections(*collectionName, order, *#setName)
//texts(id, textTitle, text, order, #collectionName, #setName, posX, posY)

//words(*word, isFavorite)
//definitions(*id, #word, definition)
//examples(*id, #word, example)
//images(*name, #word)
//audios(*name, #word)
//wordsText(textId, wordName, posStart, posEnd)
//relatedWord(word, related)

//tags(*tag)
//wordTags(*word, #*tag)
//infoVar(*varName, data)


//region tables Names
const val T_Sets = "sets"
const val T_collections = "collections"
const val T_texts = "texts"
const val T_words = "words"
const val T_definitions = "definitions"
const val T_examples_collection = "examples_collection"
const val T_examples = "examples"
const val T_details = "details"
const val T_images = "images"
const val T_audios = "audios"
const val T_wordTags = "wordTags"
const val T_tags = "tags"
const val T_infoVar = "infoVar"
const val T_wordsText = "wordsText"
const val T_relatedWord = "relatedWord"
const val T_folders = "Folders"
const val T_words_Folder = "WordsFolder"
//endregion

//region attributes Names
const val A_setName = "setName"
const val A_color = "color"
const val A_collectionName = "collectionName"
const val A_order = "orderIndex"
const val A_textID = "id"
const val A_text = "textName"
const val A_wordsPos = "wordsPos"
const val A_word = "word"
const val A_remember = "remember"
const val A_favorite = "favorite"
const val A_definition = "definition"
const val A_example = "example"
const val A_tag = "tag"
const val A_imageName = "imageName"
const val A_audioName = "audioName"
const val A_varId = "varId"
const val A_value = "value"
const val A_textTitle = "textTitle"
const val A_posX = "posX"
const val A_posY = "posY"
const val A_posStart = "posStart"
const val A_posEnd = "posEnd"
const val A_related = "related"
const val A_backgroundColor = "backgroundColor"
const val A_path = "path"
const val A_Name = "FolderName"
const val A_Type = "Type"
const val A_created_time = "created_time"
const val A_isKnown = "known"
const val A_example_col = "example_collection_name"
const val A_example_col_id = "example_collection_id"
//endregion

//region defTables

//region collection
//sets(*setName, color)
const val DT_set = "$T_Sets ($A_setName VARCHAR NOT NULL, $A_color INT," +
        " PRIMARY KEY($A_setName));"

//collections(*collectionName, order, *#setName)
const val DT_collections = "$T_collections ($A_collectionName VARCHAR NOT NULL, $A_order INT, $A_setName VARCHAR NOT NULL," +
        " PRIMARY KEY($A_collectionName, $A_setName)," +
        " FOREIGN KEY($A_setName) REFERENCES $T_Sets ($A_setName) ON DELETE CASCADE ON UPDATE CASCADE);"

//texts(id, textTitle, text, order, #collectionName, #setName, posX, posY)
const val DT_texts = "$T_texts ($A_textID INTEGER PRIMARY KEY, $A_textTitle VARCHAR, $A_text VARCHAR, $A_order INT," +
        " $A_collectionName VARCHAR NOT NULL, $A_setName VARCHAR NOT NULL, $A_posX INT DEFAULT 0, $A_posY INT DEFAULT 0," +
        " FOREIGN KEY($A_collectionName, $A_setName) REFERENCES $T_collections ($A_collectionName, $A_setName) ON DELETE CASCADE ON UPDATE CASCADE);"

//wordsText(textId, wordName, posStart, posEnd)
//delete on delete word
const val DT_wordText = "$T_wordsText ($A_textID INT, $A_word VARCHAR, $A_posStart INT, $A_posEnd INT, " +
        " FOREIGN KEY($A_textID) REFERENCES $T_texts ($A_textID) ON DELETE CASCADE ON UPDATE CASCADE);"
//endregion

//region table Words
//words(*word, isFavorite)
//create table temp_tab (word varchar not null,  favorite BIT DEFAULT 0, created_time BIGINT default 0,  known bit default 0, PRIMARY KEY(word))
//insert into temp_tab select word, favorite,created_time, known from words
//alter table temp_tab rename to words;

/*
create table example_tmp (word VARCHAR NOT NULL, example VARCHAR NOT NULL, example_collection_id INT NOT NULL, PRIMARY KEY(word, example, example_collection_id),
 FOREIGN KEY(word) REFERENCES words (word) ON DELETE CASCADE ON UPDATE CASCADE,
 FOREIGN KEY(example_collection_id) REFERENCES examples_collection (example_collection_id) ON DELETE CASCADE);
*/

/*
isKnown -> 000 three bits used
mastered is 100=4 or 110=6
visited is 010=2 or 110=6
archived is 001=1
mastered - visited - archived
*/
const val DT_words = "$T_words ($A_word varchar not null,  $A_favorite BIT DEFAULT 0," +
        " $A_created_time BIGINT default 0, $A_isKnown INT default 0," +
        " PRIMARY KEY($A_word));"

//definitions(#word, definition)
const val DT_definitions = "$T_definitions ($A_word VARCHAR NOT NULL, $A_definition VARCHAR NOT NULL," +
        " PRIMARY KEY($A_word, $A_definition)," +
        " FOREIGN KEY($A_word) REFERENCES $T_words ($A_word) ON DELETE CASCADE ON UPDATE CASCADE);"

//create table tmp_tab (example_collection_id INTEGER PRIMARY KEY AUTOINCREMENT, example_collection_name VARCHAR NOT NULL, UNIQUE(example_collection_name))
//examples_collection(#example_collection_id, example_collection_name)
const val DT_examples_collection = "$T_examples_collection ($A_example_col_id INTEGER PRIMARY KEY AUTOINCREMENT, $A_example_col VARCHAR NOT NULL," +
        "UNIQUE($A_example_col));"

//examples(#word, example, example_collection_id)
const val DT_examples = "$T_examples ($A_word VARCHAR NOT NULL, $A_example VARCHAR NOT NULL," +
        " $A_example_col_id INT NOT NULL," +
        " PRIMARY KEY($A_word, $A_example, $A_example_col_id)," +
        " FOREIGN KEY($A_word) REFERENCES $T_words ($A_word) ON DELETE CASCADE ON UPDATE CASCADE, " +
        " FOREIGN KEY($A_example_col_id) REFERENCES $T_examples_collection ($A_example_col_id) ON DELETE CASCADE);"

//images(*name, #word)
//on delete word delete images files
const val DT_images = "$T_images ($A_imageName VARCHAR NOT NULL, $A_word VARCHAR NOT NULL," +
        " PRIMARY KEY($A_imageName)," +
        " FOREIGN KEY($A_word) REFERENCES $T_words ($A_word) ON DELETE CASCADE ON UPDATE CASCADE);"

//audios(*name, #word)
//on delete word delete audios files
const val DT_audios = "$T_audios ($A_audioName VARCHAR NOT NULL, $A_word VARCHAR NOT NULL," +
        " PRIMARY KEY($A_audioName)," +
        " FOREIGN KEY($A_word) REFERENCES $T_words ($A_word) ON DELETE CASCADE ON UPDATE CASCADE);"

const val DT_related = "$T_relatedWord ($A_word VARCHAR, $A_related VARCHAR," +
        " PRIMARY KEY($A_word, $A_related)," +
        " FOREIGN KEY($A_word) REFERENCES $T_words ($A_word) ON DELETE CASCADE ON UPDATE CASCADE," +
        " FOREIGN KEY($A_related) REFERENCES $T_words ($A_word) ON DELETE CASCADE ON UPDATE CASCADE);"

//endregion

//region table tags
//tags(*tag)
const val DT_tags = "$T_tags ($A_tag, PRIMARY KEY($A_tag));"

//wordTags(*word, #*tag) //delete tag => delete (all Words, tag)
const val DT_wordTags = "$T_wordTags ($A_word VARCHAR NOT NULL, $A_tag VARCHAR NOT NULL," +
        " PRIMARY KEY($A_word, $A_tag)," +
        " FOREIGN KEY($A_word) REFERENCES $T_words ($A_word) ON DELETE CASCADE ON UPDATE CASCADE," +
        " FOREIGN KEY($A_tag) REFERENCES $T_tags ($A_tag) ON DELETE CASCADE ON UPDATE CASCADE);"
//endregion

//region table folder

const val DT_folders = "$T_folders ($A_path VARCHAR NOT NULL," +
        " PRIMARY KEY($A_path));"

const val DT_words_folder = "$T_words_Folder ($A_word VARCHAR NOT NULL, $A_path VARCHAR NOT NULL," +
        " PRIMARY KEY($A_word, $A_path)," +
        " FOREIGN KEY($A_word) REFERENCES $T_words ($A_word) ON DELETE CASCADE ON UPDATE CASCADE," +
        " FOREIGN KEY($A_path) REFERENCES $T_folders ($A_path) ON DELETE CASCADE ON UPDATE CASCADE);"

//endregion

//infoVar(*varName, data)
const val DT_infoVar = "$T_infoVar ($A_varId INT NOT NULL, $A_value VARCHAR NOT NULL," +
        " PRIMARY KEY($A_varId));"

//endregion

