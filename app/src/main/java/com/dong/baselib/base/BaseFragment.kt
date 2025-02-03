@file:Suppress("DEPRECATION")

package com.dong.baselib.base

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.io.File
import java.io.IOException
import java.io.Serializable

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    protected lateinit var binding: VB
    val indexPage = 0;
    abstract fun setBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        saveInstanceState: Bundle?,
    ): VB



    fun lightStatusBar(status: Boolean) {
        val insertController =
            WindowCompat.getInsetsController(requireActivity().window, binding.root)
        insertController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            show(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            isAppearanceLightStatusBars = status
        }
    }


    fun newInstance(data: HashMap<String, Serializable>?): BaseFragment<VB> {
        val fragment = this
        val args = Bundle()
        if (data != null) {
            for ((key, value) in data) {
                args.putSerializable(key, value)
            }
        }
        fragment.setArguments(args)
        return fragment
    }

    fun getData(key: String?): Serializable? {
        val args = arguments
        return if (args != null && args.containsKey(key)) {
            args.getSerializable(key)
        } else null
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = setBinding(inflater, container, savedInstanceState)

        return binding.root
    }

    val statusBarHeight: Int
        @SuppressLint("DiscouragedApi", "InternalInsetResource")
        get() {
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
        binding.initView()
        binding.onClick()
    }

    open fun getData() {
    }


    abstract fun VB.initView()
    abstract fun VB.onClick()



    fun notFullView() {
        val paddingTop = binding.root.paddingTop + statusBarHeight
        binding.root.setPadding(
            binding.root.paddingLeft,
            paddingTop,
            binding.root.paddingRight,
            binding.root.paddingBottom
        )

    }

    open fun showKeyboard(view: View?) {
        val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }


    open fun showKeyboard() {
        val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.root, InputMethodManager.SHOW_IMPLICIT)
    }

    open fun hideKeyboard() {
        val imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().window.decorView.rootView.windowToken, 0)
    }

    fun addFragment(fragment: Fragment) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(id, fragment)
        transaction?.commitAllowingStateLoss()
    }

    fun addFragment(
        fragment: Fragment,
        id: Int = android.R.id.content,
        addToBackStack: Boolean = false
    ) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.add(id, fragment)
        if (addToBackStack) {
            transaction?.addToBackStack(fragment.javaClass.simpleName)
        }
        transaction?.commitAllowingStateLoss()
    }

    fun replaceFullViewFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(id, fragment)
        if (addToBackStack) {
            transaction?.addToBackStack(fragment.javaClass.simpleName)
        }
        transaction?.commitAllowingStateLoss()
    }


    fun replaceFragment(
        fragment: Fragment,
        id: Int = android.R.id.content,
        addToBackStack: Boolean = true
    ) {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(id, fragment)
        if (addToBackStack) {
            transaction?.addToBackStack(fragment.javaClass.simpleName)
        }
        transaction?.commitAllowingStateLoss()
    }

    open fun closeFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().remove(fragment)
            .commitNowAllowingStateLoss()
    }


    fun moveImageToGallery(cacheFilePath: String): Uri? {
        val cacheFile = File(cacheFilePath)

        if (!cacheFile.exists()) {
            return null
        }

        val contentResolver = requireActivity().contentResolver
        val uri: Uri?

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, cacheFile.name)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/QR Codes")
            }

            uri = contentResolver.insert(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
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
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            val qrCodesDir = File(picturesDir, "QR Codes")
            if (!qrCodesDir.exists()) {
                qrCodesDir.mkdirs()
            }

            val newFile = File(qrCodesDir, cacheFile.name)
            try {
                cacheFile.copyTo(newFile, overwrite = true)
                uri = Uri.fromFile(newFile)
               
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = uri
                requireActivity().sendBroadcast(mediaScanIntent)
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        return uri
    }


    private fun saveImageToGallery(bitmap: Bitmap, fileName: String): Uri? {
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/QR Codes")
        }
        val contentResolver = requireContext().contentResolver
        val uri = contentResolver.insert(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        }
        return uri
    }

    fun reconvertUnicodeToEmoji(unicodeStr: String?): String {
        val regex = Regex("""\\u([0-9A-Fa-f]{4})""")
        if (unicodeStr == null) return ""
        else {
            return regex.replace(unicodeStr) { matchResult ->
                val codePoint =
                    matchResult.groupValues[1].toInt(16) 
                String(Character.toChars(codePoint)) 
            }
        }
    }

    fun convertEmojisToUnicode(input: String?): String {
        val unicodeString = StringBuilder()
        if (input != null) {
            for (char in input) {
                val codePoint = char.code
                unicodeString.append(String.format("\\u%04X", codePoint))
            }
            return unicodeString.toString()
        } else {
            return ""
        }
    }


}
