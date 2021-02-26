package com.cczhr.otglocation.utlis

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.*



fun InputStream.readString(): String {
    val baos = ByteArrayOutputStream()
    this.copyTo(baos)
    return baos.toString()
}

fun Context.runMainThread(run:()->Unit){
    ContextCompat.getMainExecutor(this).execute {
        run()
    }
}
fun InputStream.saveFilesDir(filePath:String, fileName:String):String  {
    File(filePath).let {
        if(!it.exists())
            it.mkdirs()
    }
    val savePath="$filePath/$fileName"
    val file = File(filePath, fileName)
    val os: FileOutputStream?
    var bis:BufferedInputStream?=null
    var bos:BufferedOutputStream?=null
    try  {
        os = FileOutputStream( file )
        bis = BufferedInputStream(this)
        bos = BufferedOutputStream(os)
        var length: Int
        while (-1 != bis.read().also { length = it }) {
            bos.write(length)
        }
    }catch (e:Exception){
        e.printStackTrace()
    }finally {
        try {
            bos!!.close()
            bis!!.close()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    return savePath
}

