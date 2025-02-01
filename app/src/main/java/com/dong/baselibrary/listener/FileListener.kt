package com.dong.baselibrary.listener

import android.net.Uri

interface FileListener {
    fun onFileSelected(filePath: String){}
    fun onFileCancelled(){}
    fun onFileError(errorMessage: String){}
    fun onSaveSuccess(filePath: String){}
    fun onChooseSingleFile(filePath: String){}
    fun onSaveError(errorMessage: String){}
    fun onChooseMultipleFiles(filePaths: List<String>){}
}