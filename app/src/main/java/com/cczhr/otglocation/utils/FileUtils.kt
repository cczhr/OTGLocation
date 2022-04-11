package com.cczhr.otglocation.utils

import android.util.Log
import java.io.File
import java.io.FileFilter

/**
 * @author cczhr
 * @description
 * @since 2021/6/11 16:31
 */
class FileUtils {
    companion object{
        fun findFile(dir: File,fileName:String):String {

            val fileTreeWalk = dir.walk()
            fileTreeWalk.iterator().forEach {
                if (it.name == fileName && it.isFile) {
                    return it.absolutePath
                }

            }
            return ""
        }
        /**
         * Delete the all in directory.
         *
         * @param dir The directory.
         * @return `true`: success<br></br>`false`: fail
         */
        fun deleteAllInDir(dir: File?): Boolean {
            return deleteFilesInDirWithFilter(dir, FileFilter { true })
        }

        /**
         * Delete all files that satisfy the filter in directory.
         *
         * @param dir    The directory.
         * @param filter The filter.
         * @return `true`: success<br></br>`false`: fail
         */
        fun deleteFilesInDirWithFilter(dir: File?, filter: FileFilter?): Boolean {
            if (dir == null || filter == null) return false
            // dir doesn't exist then return true
            if (!dir.exists()) return true
            // dir isn't a directory then return false
            if (!dir.isDirectory) return false
            val files = dir.listFiles()
            if (files != null && files.size != 0) {
                for (file in files) {
                    if (filter.accept(file)) {
                        if (file.isFile) {
                            if (!file.delete()) return false
                        } else if (file.isDirectory) {
                            if (!deleteDir(file)) return false
                        }
                    }
                }
            }
            return true
        }

        /**
         * Delete the directory.
         *
         * @param dir The directory.
         * @return `true`: success<br></br>`false`: fail
         */
        private fun deleteDir(dir: File?): Boolean {
            if (dir == null) return false
            // dir doesn't exist then return true
            if (!dir.exists()) return true
            // dir isn't a directory then return false
            if (!dir.isDirectory) return false
            val files = dir.listFiles()
            if (files != null && files.isNotEmpty()) {
                for (file in files) {
                    if (file.isFile) {
                        if (!file.delete()) return false
                    } else if (file.isDirectory) {
                        if (!deleteDir(file)) return false
                    }
                }
            }
            return dir.delete()
        }


    }
}