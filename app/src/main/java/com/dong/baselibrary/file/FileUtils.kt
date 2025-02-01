@file:Suppress("DEPRECATION")

package com.dong.baselibrary.file

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dong.baselibrary.listener.FileListener
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, fileName: String): String? {
    val directory = context.filesDir
    val file = File(directory, "$fileName.png")

    return try {
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        file.absolutePath
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun Activity.saveImageToInternalStorage(bitmap: Bitmap, fileName: String): String? {
    val directory = this.filesDir
    val file = File(directory, "$fileName.png")

    return try {
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        file.absolutePath
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}


fun Activity.shareImages(imagePaths: MutableList<String>) {
    val imageUris = ArrayList<Uri>()

    for (path in imagePaths) {
        val file = File(path)
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        imageUris.add(uri)
    }
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND_MULTIPLE
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
        type = "image/*"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(shareIntent, "Share images via"))
}

fun shareImages(context: Context, imagePaths: MutableList<String>) {
    val imageUris = ArrayList<Uri>()

    for (path in imagePaths) {
        val file = File(path)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        imageUris.add(uri)
    }
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND_MULTIPLE
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
        type = "image/*"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share images via"))
}

fun Activity.bitmapFromUri(uri: Uri): Bitmap? {
    return try {
        val inputStream = this.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream).also {
            inputStream?.close()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun Bitmap.resizeWidth(newWidth: Int): Bitmap {
    val aspectRatio = this.height.toFloat() / this.width
    val targetHeight = (newWidth * aspectRatio).toInt()
    return Bitmap.createScaledBitmap(this, newWidth, targetHeight, true)
}

fun Bitmap.resizeHeight(newHeight: Int): Bitmap {
    val aspectRatio = this.width.toFloat() / this.height
    val targetWidth = (newHeight * aspectRatio).toInt()
    return Bitmap.createScaledBitmap(this, targetWidth, newHeight, true)
}

fun Bitmap.resize(newWidth: Int, newHeight: Int): Bitmap {
    return Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
}


fun Activity.openFromAssets(filePath: String, callback: ((Bitmap?) -> Unit)) {
    try {
        val inputStream = this.assets.open(filePath)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        callback.invoke(bitmap)
        inputStream.close()
    } catch (e: IOException) {
        callback.invoke(null)
        e.printStackTrace()
    }
}

fun Activity.openFromAssets(filePath: String): Bitmap? {
    try {
        val inputStream = this.assets.open(filePath)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        return bitmap
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}

fun copyQrToClipBoard(context: Context, value: String) {
    val clipboard =
        context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("Copy Value", value)
    clipboard.setPrimaryClip(clip)

}

fun Activity.copyQrToClipBoard(value: String) {
    val clipboard =
        getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("Copy Value", value)
    clipboard.setPrimaryClip(clip)

}


fun saveFileByBitmap(context: Context,bitmapCreate: Bitmap?,fileName: String,totalDirection: String, listener: FileListener? = null) {
    bitmapCreate?.let { bitmap ->
        val savedUri = saveImageToGallery(context, bitmap,fileName, totalDirection)
        if (savedUri == null) {
            listener?.onSaveSuccess("$savedUri")
        } else {
            listener?.onSaveError("Not found image file $savedUri")
        }
    }
}

fun Activity.saveFileByBitmap(bitmapCreate: Bitmap?,fileName: String,totalDirection: String, listener: FileListener? = null) {
    bitmapCreate?.let { bitmap ->
        val savedUri = saveImageToGallery(this, bitmap,fileName, totalDirection)
        if (savedUri == null) {
            listener?.onSaveSuccess("$savedUri")
        } else {
            listener?.onSaveError("Not found image file $savedUri")
        }
    }
}

fun Activity.copyToClipBoard(value: String) {
    val clipboard =
        getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("QR Code Text", value)
    clipboard.setPrimaryClip(clip)
}


fun Activity.saveFile(bitmapCreate: Bitmap?,fileName: String,totalDirection: String, callback: ((String) -> Unit)? = null) {
    bitmapCreate?.let { bitmap ->
        saveImageToGallery(this, bitmap, fileName,totalDirection) {
            callback?.invoke(it)
        }
    }
}


fun moveImageToGallery(activity: Activity, cacheFilePath: String,totalDirection: String): Uri? {
    val cacheFile = File(cacheFilePath)

    if (!cacheFile.exists()) {
        return null
    }

    val contentResolver = activity.contentResolver
    val uri: Uri?

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, cacheFile.name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                totalDirection
            )
        }

        uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                cacheFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

        }
    } else {

        val resultDir = File(totalDirection)
        if (!resultDir.exists()) {
            resultDir.mkdirs()
        }

        val newFile = File(resultDir, cacheFile.name)
        try {
            cacheFile.copyTo(newFile, overwrite = true)
            uri = Uri.fromFile(newFile)
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = uri
            activity.sendBroadcast(mediaScanIntent)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    return uri
}


fun saveImageToGallery(
    activity: Activity,
    bitmap: Bitmap,
    fileName: String,
    totalDirection: String,
    callback: ((String) -> Unit)? = null
): Uri? {
    val contentResolver = activity.contentResolver
    val uri: Uri?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                totalDirection
            )
        }
        uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        }
    } else {

        val fileDir = File(totalDirection)
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }
        val imageFile = File(fileDir, fileName)
        try {
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            uri = Uri.fromFile(imageFile)
            callback?.invoke(imageFile.absolutePath)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = uri
        activity.sendBroadcast(mediaScanIntent)
    }
    return uri
}


@Suppress("DEPRECATION")
fun Activity.saveImageToDownloads(
    bitmap: Bitmap,
    fileName: String,
    totalDirection: String,
    callback: ((String) -> Unit)? = null
) {
    val contentResolver = this.contentResolver
    var uri: Uri?

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                totalDirection
            )
        }
        uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        }
    } else {

        val photoBlenderDir = File(totalDirection)
        if (!photoBlenderDir.exists()) {
            photoBlenderDir.mkdirs()
        }
        val imageFile = File(photoBlenderDir, fileName)
        try {
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            uri = Uri.fromFile(imageFile)
        } catch (e: IOException) {
            e.printStackTrace()
            uri = null
        }
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = uri
        this.sendBroadcast(mediaScanIntent)
    }
    val imagePath = getRealPathFromUri(this, uri)
    callback?.invoke(imagePath ?: "")
}

fun getRealPathFromUri(activity: Activity, uri: Uri?): String? {
    if (uri == null) return null
    return if (uri.scheme == "content") {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.MediaColumns.DATA)
            cursor = activity.contentResolver.query(uri, projection, null, null, null)
            cursor?.let {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    return it.getString(columnIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        null
    } else {
        uri.path
    }
}


fun shareFileWithPath(activity: Activity, path: String) {
    val file = File(path)

    if (file.exists()) {
        val fileUri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.provider",
            file
        )
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "image/png"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        activity.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
    }
}

fun moveImageToGallery(context: Context, cacheFilePath: String,totalDirection:String): Uri? {
    val cacheFile = File(cacheFilePath)

    if (!cacheFile.exists()) {
        return null
    }

    val contentResolver = context.contentResolver
    val uri: Uri?

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, cacheFile.name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, totalDirection)
        }

        uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                cacheFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

        }
    } else {

        val fileDir = File(totalDirection)
        if (!fileDir.exists()) {
            fileDir.mkdirs()
        }

        val newFile = File(fileDir, cacheFile.name)
        try {
            cacheFile.copyTo(newFile, overwrite = true)
            uri = Uri.fromFile(newFile)

            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = uri
            context.sendBroadcast(mediaScanIntent)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    return uri
}


fun saveImageToGallery(context: Context, bitmap: Bitmap, fileName: String,totalDirection:String): Uri? {
    val contentResolver = context.contentResolver
    val uri: Uri?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, totalDirection)
        }
        uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        }
    } else {

        val qrCodesDir = File(totalDirection)
        if (!qrCodesDir.exists()) {
            qrCodesDir.mkdirs()
        }
        val imageFile = File(qrCodesDir, fileName)
        try {
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            uri = Uri.fromFile(imageFile)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = uri
        context.sendBroadcast(mediaScanIntent)
    }
    return uri
}

fun loadImageFromAssets(context: Context, fileName: String): Drawable? {
    return try {
        val inputStream = context.assets.open(fileName)
        Drawable.createFromStream(inputStream, null)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun loadBitmapFromAssets(context: Context, fileName: String): Bitmap? {
    return try {
        val inputStream = context.assets.open(fileName)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}


fun saveBitmapToCache(activity: Activity, bitmap: Bitmap): File {
    val cacheDir = activity.cacheDir
    val file = File(cacheDir, "${System.currentTimeMillis()}resized_image.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, out)
    }
    return file
}

fun saveDrawableToCache(context: Context, drawableId: Int): String? {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    val bitmap = (drawable as BitmapDrawable).bitmap
    val cacheDir = context.cacheDir
    val file = File(cacheDir, "${System.currentTimeMillis()}drawable_image.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, out)
    }
    return file.absolutePath
}

@SuppressLint("UseCompatLoadingForDrawables")
fun getIconFromData(activity: Activity, icon: Any): Drawable? {
    return when (icon) {
        is Int -> activity.getDrawable(icon)
        is Bitmap -> BitmapDrawable(activity.resources, icon)
        is String -> {
            val file = File(icon)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(icon)
                BitmapDrawable(activity.resources, bitmap)
            } else {
                loadImageFromAssets(activity, icon)
            }
        }

        else -> null
    }
}

@Throws(IOException::class)
fun getBitmapFromAssets(context: Context, fileName: String?): Bitmap {
    val assetManager = context.assets

    val istr = assetManager.open(fileName!!)
    val bitmap = BitmapFactory.decodeStream(istr)
    istr.close()

    return bitmap
}


fun shareFileWithPath(context: Context, path: String) {
    val file = File(path)

    if (file.exists()) {
        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "image/png"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Phot"))
    }
}

fun shareFileBitmap(context: Context,fileName: String, bitmapCreate: Bitmap?) {
    bitmapCreate?.let { bitmap ->
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, fileName)
        try {
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, fileUri)
                type = "image/png"
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Photo"))
            Handler(Looper.getMainLooper()).postDelayed({
                if (file.exists()) {
                    file.delete()
                }
            }, 5000)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}


