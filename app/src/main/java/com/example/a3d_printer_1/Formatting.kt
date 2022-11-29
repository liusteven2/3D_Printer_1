package com.example.a3d_printer_1

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.text.InputFilter
import android.text.Spanned
import androidx.core.widget.addTextChangedListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.w3c.dom.Text


//class Formatting : Fragment() {
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_formatting, container, false)
//        val textView : TextView = view.findViewById(R.id.textView)
//        val args = this.arguments
//        val inputData = args?.get("data")
//        textView.text = inputData.toString()
//        return view
//    }
//}


class Formatting : Fragment() {

    //for sending information to firebase database
    private lateinit var database : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_formatting, container, false)
//        val textView : TextView = view.findViewById(R.id.textView)
//        val args = this.arguments
//        val inputData = args?.get("data")
//        textView.text = inputData.toString()


        var formatExtTemp : String? = null
        var formatBedTemp : String? = null
        var formatFanSpeed : String? = null
        var formatXPos : String? = null
        var formatYPos : String? = null
        var formatZpos : String? = null

        val extTemp : EditText = view.findViewById(R.id.numPickerExtTemp)
        extTemp.filters = arrayOf<InputFilter>(MinMaxFilter(1,300))
//        val a : Int = view.findViewById(R.id.numPickerExtTemp).text.toString().toInt()
//        extTemp.text.isNotEmpty().apply {
//            if(a > 100){
//                formatExtTemp="100"
////            extTemp.setText("100")
//            } else {
//                formatExtTemp=extTemp.text.toString()
//            }
//        }
////        if(extTemp.text.toString().toInt()!=null && extTemp.text.toString().toInt() > 100){
//            formatExtTemp="100"
////            extTemp.setText("100")
//        } else {
//            formatExtTemp=extTemp.text.toString()
//        }
//        val extNumber = extTemp.text.toString().toIntOrNull()
//        if (extNumber != null) {
//            if (extTemp.text.toString().toInt() < 0) {
//                extTemp.setText("0")
//                formatExtTemp = extTemp.text.toString()
//            } else if (extTemp.text.toString().toInt() > 100) {
//                extTemp.setText("100")
//                formatExtTemp = extTemp.text.toString()
//            } else {
//                formatExtTemp = extTemp.text.toString()}
//        }
//        val extTemp : NumberPicker = view.findViewById(R.id.numPickerExtTemp)
//        extTemp.minValue = 0
//        extTemp.maxValue = 100

        val bedTemp : EditText = view.findViewById(R.id.numPickerBedTemp)
        bedTemp.filters = arrayOf<InputFilter>(MinMaxFilter(1,100))
//        val bedNumber = bedTemp.text.toString().toIntOrNull()
//        if (extNumber != null) {
//            if (bedNumber < 0) {
//                extTemp.setText("0")
//            } else if (bedNumber > 100) {
//                extTemp.setText("100")
//                formatBedTemp = bedTemp.text.toString()
//            } else {
//                formatBedTemp = bedTemp.text.toString()}
//        }
//        bedTemp.minValue = 0
//        bedTemp.maxValue = 100

//        val fanSpeed : NumberPicker = view.findViewById(R.id.numPickerFanSpeed)
//        fanSpeed.minValue = 0
//        fanSpeed.maxValue = 100
////        val fanSpeed : EditText = view.findViewById(R.id.numPickerFanSpeed)
////        fanSpeed.filters = arrayOf<InputFilter>(MinMaxFilter(1,100))
//        val fanSpeed : Button = view.findViewById(R.id.textView_FanSpeed)
//        fanSpeed.setOnClickListener{
//            if (fanSpeed.text.toString() == "HIGH") {
//                fanSpeed.setText("LOW")
//            } else if (fanSpeed.text.toString() == "LOW") {
//                fanSpeed.setText("OFF")
//            } else {
//                fanSpeed.setText("HIGH")
//            }
//        }

        val xVal : EditText = view.findViewById(R.id.numPickerX)
        xVal.filters = arrayOf<InputFilter>(MinMaxFilter(1,300))
//        xVal.minValue = 0
//        xVal.maxValue = 100

        val yVal : EditText = view.findViewById(R.id.numPickerY)
        yVal.filters = arrayOf<InputFilter>(MinMaxFilter(1,100))
//        yVal.minValue = 0
//        yVal.maxValue = 100

        val zVal : EditText = view.findViewById(R.id.numPickerZ)
        zVal.filters = arrayOf<InputFilter>(MinMaxFilter(1,100))
//        zVal.minValue = 0
//        zVal.maxValue = 100

//        var formatExtTemp : String? = null
//        var formatBedTemp : String? = null
//        var formatFanSpeed : String? = null
//        var formatXPos : String? = null
//        var formatYPos : String? = null
//        var formatZpos : String? = null

//        extTemp.setOnValueChangedListener { numberPicker, i, i2 ->  formatExtTemp = numberPicker.value.toString()}
//        bedTemp.setOnValueChangedListener { numberPicker, i, i2 ->  formatBedTemp = numberPicker.value.toString()}
//        fanSpeed.setOnValueChangedListener { numberPicker, i, i2 ->  formatFanSpeed = numberPicker.value.toString()}
//        xVal.setOnValueChangedListener { numberPicker, i, i2 ->  formatXPos = numberPicker.value.toString()}
//        yVal.setOnValueChangedListener { numberPicker, i, i2 ->  formatYPos = numberPicker.value.toString()}
//        zVal.setOnValueChangedListener { numberPicker, i, i2 ->  formatZpos = numberPicker.value.toString()}

        val fanSpeed : Button = view.findViewById(R.id.textView_FanSpeed)
        fanSpeed.setOnClickListener {
            if (fanSpeed.text.toString() == "HIGH")
                fanSpeed.text = "LOW"
            else if (fanSpeed.text.toString() == "LOW")
                fanSpeed.text = "OFF"
            else
                fanSpeed.text = "HIGH"
        }

        val btn : Button = view.findViewById(R.id.buttonApply)
        btn.setOnClickListener{
            database = FirebaseDatabase.getInstance().getReference("Printer Formatting")
//            val newFormat = PrinterControls(extTemp.text.toString(), bedTemp.text.toString(), fanSpeed.text.toString(), xVal.text.toString(), yVal.text.toString(), zVal.text.toString())
            val newFormat = PrinterControls(extTemp.text.toString(), bedTemp.text.toString(), "HIGH", xVal.text.toString(), yVal.text.toString(), zVal.text.toString())
            database.child("Format").setValue(newFormat).addOnSuccessListener {
                Toast.makeText(activity, "Successfully Saved" + extTemp.text.toString(), Toast.LENGTH_SHORT).show();
            }.addOnFailureListener {
                Toast.makeText(activity, "Failed Saved", Toast.LENGTH_SHORT).show();
            }
        }
        return view
    }

//    Custom class to define min and max for the edit text
    inner class MinMaxFilter() : InputFilter {
        private var intMin: Int = 0
        private var intMax: Int = 0

        // Initialized
        constructor(minValue: Int, maxValue: Int) : this() {
            this.intMin = minValue
            this.intMax = maxValue
        }

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dStart: Int, dEnd: Int): CharSequence? {
            try {
                val input = Integer.parseInt(dest.toString() + source.toString())
                if (isInRange(intMin, intMax, input)) {
                    return null
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return ""
        }

        // Check if input c is in between min a and max b and
        // returns corresponding boolean
        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }

}