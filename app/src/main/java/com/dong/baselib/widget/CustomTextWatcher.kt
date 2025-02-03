package  com.dong.baselib.widget

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.math.BigDecimal

class CustomTextWatcher(
    private val field: EditText,
    private val decimalSeparator: Char = '.',
    private val thousandSeparator: Char = ',',
    private var callback: (String) -> Unit
) : TextWatcher {
    private var prevString = ""
    private var curString = ""

    val limit = BigDecimal(1.0E16)
    private fun formatWithCommas(input: String): String {
        return input.reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()
    }

    override fun afterTextChanged(p0: Editable?) {
        field.removeTextChangedListener(this)

        try {
            val givenString: String = p0.toString()
            if (givenString.startsWith(',') || givenString.startsWith('.')) {
                callback.invoke("null")
                field.setText(prevString)
            } else {
                curString = givenString
                val initialCurPos: Int = field.selectionEnd ?: 0

                var isEditing = false
                if (initialCurPos != givenString.length) {
                    isEditing = true
                }
                var firstStr = givenString
                var secondStr = ""
                val indexOfDecimalPoint = givenString.indexOf(decimalSeparator)
                if (indexOfDecimalPoint != -1) {
                    firstStr = givenString.substring(0, indexOfDecimalPoint)
                    secondStr = givenString.substring(indexOfDecimalPoint + 1, givenString.length)
                    secondStr = secondStr.filter { it.isDigit() }
                }
                if (firstStr.contains(thousandSeparator)) {
                    firstStr = firstStr.replace(thousandSeparator.toString(), "")
                }

                if(firstStr.length>=17){
                    callback("Na")
                   firstStr = firstStr.substring(0,17)
                }
                val longVal = BigDecimal(firstStr)
                if(secondStr.length>4){
                    secondStr = secondStr.substring(0, 4)
                }
                val formattedString = formatWithCommas(longVal.toString())
                val resultantStr = if (indexOfDecimalPoint != -1) {
                    "$formattedString$decimalSeparator$secondStr"
                } else {
                    formattedString
                }

                field.setText(resultantStr)
                callback.invoke(resultantStr)
                var finalCurPos = field.text.length
                if (isEditing) {
                    finalCurPos = if (
                        curString.length > prevString.length &&
                        firstStr.length != 1 && firstStr.length % 3 == 1 &&
                        initialCurPos != indexOfDecimalPoint
                    ) {
                        initialCurPos + 1
                    } else {
                        initialCurPos
                    }
                }
                field.setSelection(finalCurPos)
                prevString = curString
            }

        } catch (nfe: NumberFormatException) {
            nfe.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        field.addTextChangedListener(this)
    }

    private fun restorePreviousState() {
        field.setText(prevString)
        field.setSelection(prevString.length)
    }


    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        
    }
}


