package com.dong.baselib.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.viewbinding.ViewBinding
import com.dong.baselib.R


abstract class BaseDialog<V : ViewBinding>(context: Context, var cancelAble: Boolean = false) :
    Dialog(context, R.style.BaseDialog) {
    private val TAG: String = BaseDialog::class.java.name
    lateinit var binding: V

    protected abstract fun setBinding(): V
    protected abstract fun initView()

    init {
        initialize()
    }

    private fun initialize() {

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setGravity(Gravity.CENTER)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    open fun showKeyboard(view: View?) {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    open fun showKeyboard() {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.root, InputMethodManager.SHOW_IMPLICIT)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setBinding()
        setContentView(binding.root)
        initView()
        setCancelable(cancelAble)
        this.setCanceledOnTouchOutside(false)
    }
}