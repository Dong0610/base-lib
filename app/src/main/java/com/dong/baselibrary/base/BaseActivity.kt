package com.dong.baselibrary.base


import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable


@Suppress("DEPRECATION")
abstract class BaseActivity<VB : ViewBinding>(
    val bindingFactory: (LayoutInflater) -> VB,
    private var fullStatus: Boolean = false
) :
    AppCompatActivity() {
    val binding: VB by lazy { bindingFactory(layoutInflater) }
    private val statusBarHeight: Int
        @SuppressLint("DiscouragedApi", "InternalInsetResource")
        get() {
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        }


    abstract fun backPressed()
    abstract fun initialize()
    abstract fun VB.setData()

    abstract fun VB.onClick()
    enum class TypeGoSettings {
        NONE,
        CAMERA,
        STORAGE,
        NOTIFICATION,
        CONTACT,
        LOCATION
    }

    fun getMediaStoreUriFromFilePath(context: Context, filePath: String): Uri? {
        var uri: Uri? = null
        val projection =
            arrayOf(MediaStore.Images.Media._ID)
        val selection = MediaStore.Images.Media.DATA + " = ?"
        val selectionArgs = arrayOf(filePath)

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  
            projection,
            selection,
            selectionArgs,
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            uri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id.toLong()
            )
            cursor.close()
        }

        return uri
    }

    fun getUriFromFile(filePath: String): Uri? {
        val file = File(filePath)
        return if (file.exists()) {
            FileProvider.getUriForFile(
                this@BaseActivity,
                "${this@BaseActivity.packageName}.provider",
                file
            )
        } else {
            null
        }
    }

    fun getData(key: String?): Serializable? {
        return if (key != null && intent?.extras != null && intent.extras!!.containsKey(key)) {
            intent.extras!!.getSerializable(key)
        } else null
    }


    open fun showKeyboard(view: View?) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }


    open fun showKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.root, InputMethodManager.SHOW_IMPLICIT)
    }

    open fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0)
    }




    fun lightStatusBar(status: Boolean) {
        val insertController =
            WindowCompat.getInsetsController(window, binding.root)
        insertController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            show(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            isAppearanceLightStatusBars = status
        }
    }

    fun hideNavigation() {
        val windowInsetsController = if (Build.VERSION.SDK_INT >= 30) {
            ViewCompat.getWindowInsetsController(window.decorView)
        } else {
            WindowInsetsControllerCompat(window, binding.root)
        }

        windowInsetsController?.let {
            it.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            it.hide(WindowInsetsCompat.Type.navigationBars())

            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility == 0) {
                    Handler().postDelayed({
                        val controller = if (Build.VERSION.SDK_INT >= 30) {
                            ViewCompat.getWindowInsetsController(window.decorView)
                        } else {
                            WindowInsetsControllerCompat(window, binding.root)
                        }
                        controller?.hide(WindowInsetsCompat.Type.navigationBars())
                    }, 3000)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        lightStatusBar(true)
        val paddingTop = binding.root.paddingTop + statusBarHeight
        if (!fullStatus) {
            binding.root.setPadding(
                binding.root.paddingLeft,
                paddingTop,
                binding.root.paddingRight,
                binding.root.paddingBottom
            )
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressed()
            }
        })
        initialize()
        binding.setData()
        binding.onClick()

        hideNavigation()

    }


    private var lastHeight = 0
    private var isSetHeight = false

    fun getKeyboardHeight(onKeyboardHeightChanged: (Int) -> Unit) {
        val rootView = window.decorView.rootView
        val rect = Rect()

        rootView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            var currentHeight = 0

            override fun onGlobalLayout() {
                rootView.getWindowVisibleDisplayFrame(rect)
                val screenHeight = rootView.height
                val visibleHeight = rect.height()
                val keyboardHeight = screenHeight - visibleHeight

                if (keyboardHeight != currentHeight) {
                    if (!isSetHeight) {
                        lastHeight = keyboardHeight
                        isSetHeight = true
                    }
                    currentHeight = keyboardHeight
                    onKeyboardHeightChanged(keyboardHeight - lastHeight)
                }
            }
        })
    }

    fun addFragment(fragment: Fragment, id: Int = android.R.id.content) {
        hideKeyboard()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(id, fragment)
        transaction.commitAllowingStateLoss()
    }

    fun addFragment(
        fragment: Fragment,
        id: Int = android.R.id.content,
        addToBackStack: Boolean = false
    ) {
        hideKeyboard()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(id, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(fragment.javaClass.simpleName)
        }
        transaction.commitAllowingStateLoss()
    }


    fun moveImageToGallery(cacheFilePath: String): Uri? {
        val cacheFile = File(cacheFilePath)

        if (!cacheFile.exists()) {
            return null
        }

        val contentResolver = this@BaseActivity.contentResolver
        val uri: Uri?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, cacheFile.name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOWNLOADS}/Loan Calculator"
                )
            }

            uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    cacheFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        } else {
            val picturesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString()
            val qrCodesDir = File(picturesDir, "LoanCalculator")
            if (!qrCodesDir.exists()) {
                qrCodesDir.mkdirs()
            }
            val newFile = File(qrCodesDir, cacheFile.name)
            try {
                cacheFile.copyTo(newFile, overwrite = true)
                uri = Uri.fromFile(newFile)
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = uri
                this@BaseActivity.sendBroadcast(mediaScanIntent)
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        return uri
    }


    private fun saveImageToGallery(bitmap: Bitmap, fileName: String): Uri? {
        val contentResolver = contentResolver
        val uri: Uri?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOWNLOADS}/Loan Calculator"
                )
            }
            uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
            }
        } else {
            val picturesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString()
            val qrCodesDir = File(picturesDir, "LoanCalculator")
            if (!qrCodesDir.exists()) {
                qrCodesDir.mkdirs()
            }
            val imageFile = File(qrCodesDir, "$fileName.png")
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
            sendBroadcast(mediaScanIntent)
        }
        return uri
    }

}