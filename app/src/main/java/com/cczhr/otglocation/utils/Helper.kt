package com.cczhr.otglocation.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import com.amap.api.mapcore.util.it
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*


fun InputStream.readString(): String {
    val baos = ByteArrayOutputStream()
    this.copyTo(baos)
    return baos.toString()
}

fun Context.runMainThread(run: () -> Unit) {
    ContextCompat.getMainExecutor(this).execute {
        run()
    }
}


fun Call<ResponseBody>.downloadFile(
    filename: String,
    downloadPath: String

) = flow {
    val response = this@downloadFile.execute()
    if (response.isSuccessful) {
        File(downloadPath).apply {
            if (!exists()) {
                mkdirs()
            } else {
                //删除已存在的文件
                this.listFiles()?.forEach {
                    it.delete()
                }
            }
        }
        var fos: FileOutputStream? = null
        val buffer = ByteArray(4096)
        var len = 0
        var sum: Long = 0
        val off = 0
        try {
            val filepath = downloadPath + File.separator + filename
            val total = response.body()!!.contentLength()
            fos = FileOutputStream(File(filepath))
            val inputStream = response.body()!!.byteStream()
            while (inputStream.read(buffer).apply { len = this } > 0) {
                fos.write(buffer, off, len)
                sum += len.toLong()
                val progress = (sum * 1.0f / total * 100).toInt()
                emit(progress)
            }
            if (sum == 0L)
                emit(-1)

        } catch (e: Exception) {
            e.printStackTrace()
            emit(-1)

        } finally {
            fos?.flush()
            fos?.close()
        }
    } else {
        emit(-1)
    }


}.flowOn(Dispatchers.IO)


fun InputStream.saveFilesDir(filePath: String, fileName: String): String {
    File(filePath).let {
        if (!it.exists())
            it.mkdirs()
    }
    val savePath = "$filePath/$fileName"
    val file = File(filePath, fileName)
    val os: FileOutputStream?
    var bis: BufferedInputStream? = null
    var bos: BufferedOutputStream? = null
    try {
        os = FileOutputStream(file)
        bis = BufferedInputStream(this)
        bos = BufferedOutputStream(os)
        var length: Int
        while (-1 != bis.read().also { length = it }) {
            bos.write(length)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            bos!!.close()
            bis!!.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return savePath
}

