package com.readystatesoftware.sqliteasset

import android.util.Log

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayList
import java.util.Scanner
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

internal object Utils {

    private val TAG = SQLiteAssetHelper::class.java.simpleName

    fun splitSqlScript(script: String, delim: Char): List<String> {
        val statements = ArrayList<String>()
        var sb = StringBuilder()
        var inLiteral = false
        val content = script.toCharArray()
        for (i in 0 until script.length) {
            if (content[i] == '"') {
                inLiteral = !inLiteral
            }
            if (content[i] == delim && !inLiteral) {
                if (sb.length > 0) {
                    statements.add(sb.toString().trim { it <= ' ' })
                    sb = StringBuilder()
                }
            } else {
                sb.append(content[i])
            }
        }
        if (sb.length > 0) {
            statements.add(sb.toString().trim { it <= ' ' })
        }
        return statements
    }

    @Throws(IOException::class)
    fun writeExtractedFileToDisk(`in`: InputStream, outs: OutputStream) {
        val buffer = ByteArray(1024)
        var length: Int = 0
        //while ((length = `in`.read(buffer)) > 0) {
        while ({ length = `in`.read(buffer); length }() > 0) {
            outs.write(buffer, 0, length)
        }
        outs.flush()
        outs.close()
        `in`.close()
    }

    @Throws(IOException::class)
    fun getFileFromZip(zipFileStream: InputStream): ZipInputStream? {
        val zis = ZipInputStream(zipFileStream)
        var ze: ZipEntry? = null
        //while ((ze = zis.nextEntry() != null) {
        while ({ ze = zis.nextEntry; ze }() != null) {
            Log.w(TAG, "extracting file: '" + ze?.name + "'...")
            return zis
        }
        return null
    }

    fun convertStreamToString(`is`: InputStream): String {
        return Scanner(`is`).useDelimiter("\\A").next()
    }

}
