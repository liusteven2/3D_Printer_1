package com.example.a3d_printer_1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
        val textView : TextView = view.findViewById(R.id.textView)
        val args = this.arguments
        val inputData = args?.get("data")
        textView.text = inputData.toString()

        val extTemp : NumberPicker = view.findViewById(R.id.numPickerExtTemp)
        extTemp.minValue = 0
        extTemp.maxValue = 100

        val bedTemp : NumberPicker = view.findViewById(R.id.numPickerBedTemp)
        bedTemp.minValue = 0
        bedTemp.maxValue = 100

        val fanSpeed : NumberPicker = view.findViewById(R.id.numPickerFanSpeed)
        fanSpeed.minValue = 0
        fanSpeed.maxValue = 100

        val xVal : NumberPicker = view.findViewById(R.id.numPickerX)
        xVal.minValue = 0
        xVal.maxValue = 100

        val yVal : NumberPicker = view.findViewById(R.id.numPickerY)
        yVal.minValue = 0
        yVal.maxValue = 100

        val zVal : NumberPicker = view.findViewById(R.id.numPickerZ)
        zVal.minValue = 0
        zVal.maxValue = 100

        var formatExtTemp = 0
        var formatBedTemp = 0
        var formatFanSpeed = 0
        var formatXPos = 0
        var formatYPos = 0
        var formatZpos = 0

        extTemp.setOnValueChangedListener { numberPicker, i, i2 ->  formatExtTemp = numberPicker.value}
        bedTemp.setOnValueChangedListener { numberPicker, i, i2 ->  formatBedTemp = numberPicker.value}
        fanSpeed.setOnValueChangedListener { numberPicker, i, i2 ->  formatFanSpeed = numberPicker.value}
        xVal.setOnValueChangedListener { numberPicker, i, i2 ->  formatXPos = numberPicker.value}
        yVal.setOnValueChangedListener { numberPicker, i, i2 ->  formatYPos = numberPicker.value}
        zVal.setOnValueChangedListener { numberPicker, i, i2 ->  formatZpos = numberPicker.value}

        val btn : Button = view.findViewById(R.id.buttonApply)
        btn.setOnClickListener{
            database = FirebaseDatabase.getInstance().getReference("Printer Formatting")
            val newFormat = PrinterControls(formatExtTemp, formatBedTemp, formatFanSpeed, formatXPos, formatYPos, formatZpos)
            database.child("Format").setValue(newFormat).addOnSuccessListener {
                Toast.makeText(activity, "Successfully Saved", Toast.LENGTH_SHORT).show();
            }.addOnFailureListener {
                Toast.makeText(activity, "Failed Saved", Toast.LENGTH_SHORT).show();
            }
        }
        return view
    }

}