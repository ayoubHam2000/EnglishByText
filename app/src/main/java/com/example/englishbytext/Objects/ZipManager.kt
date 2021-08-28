package com.example.englishbytext.Objects

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.example.englishbytext.Utilites.FILE_CODE
import java.io.*
import java.lang.String.format
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipManager {

    /**
     * Zips a Folder to "[Folder].zip"
     * @param toZipFolder Folder to be zipped
     * @return the resulting ZipFile
     */
    fun zipFolder(context : Context, toZipFolder: File, uri : Uri): Uri? {
        val zipFile = FileOutputStream(context.contentResolver.openFileDescriptor(uri, "w")!!.fileDescriptor)
        return try {
            val out = ZipOutputStream(zipFile)
            zipFile.write(FILE_CODE.toByteArray())
            zipSubFolder(out, toZipFolder, toZipFolder.path.length)
            out.close()
            uri
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }


    private fun zipSubFolder(out: ZipOutputStream, folder: File, basePathLength: Int) {
        val BUFFER = 2048
        val fileList: Array<File> = folder.listFiles()!!
        var origin: BufferedInputStream? = null
        for (file in fileList) {
            if (file.isDirectory) {
                zipSubFolder(out, file, basePathLength)
            } else {
                val data = ByteArray(BUFFER)
                val unmodifiedFilePath: String = file.path
                val relativePath = unmodifiedFilePath.substring(basePathLength + 1)
                val fi = FileInputStream(unmodifiedFilePath)
                origin = BufferedInputStream(fi, BUFFER)
                val entry = ZipEntry(relativePath)
                entry.time = file.lastModified() // to keep modification time after unzipping
                out.putNextEntry(entry)
                var count: Int
                while (origin.read(data, 0, BUFFER).also { count = it } != -1) {
                    out.write(data, 0, count)
                }
                origin.close()
                out.closeEntry()
            }
        }
    }


    fun unzip(zipFile: FileInputStream, targetDirectory: File?) {
        val codeBuffer = ByteArray(FILE_CODE.length)
        zipFile.read(codeBuffer)
        if(String(codeBuffer) == FILE_CODE){
            val zis = ZipInputStream(BufferedInputStream(zipFile))
            zis.use {
                var ze: ZipEntry?
                var count: Int
                val buffer = ByteArray(8192)
                while (zis.nextEntry.also { ze = it } != null) {
                    val file = File(targetDirectory, ze!!.name)
                    val dir = if (ze!!.isDirectory) file else file.parentFile
                    if (!dir.isDirectory && !dir.mkdirs()) throw FileNotFoundException(
                            "Failed to ensure directory: " +
                                    dir.absolutePath
                    )
                    if (ze!!.isDirectory) continue
                    val fout = FileOutputStream(file)
                    fout.use {
                        while (zis.read(buffer).also { count = it } != -1) fout.write(buffer, 0, count)
                    }
                }
            }
        }
    }

}