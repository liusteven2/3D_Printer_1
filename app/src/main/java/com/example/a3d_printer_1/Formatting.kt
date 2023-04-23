package com.example.a3d_printer_1

import android.annotation.SuppressLint
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
import androidx.fragment.app.activityViewModels
import com.example.a3d_printer_1.model.PrintFileViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.w3c.dom.Text


class Formatting : Fragment() {

    //for sending information to firebase database
    private lateinit var database : DatabaseReference

    //for fragment communication - sharedview/viewmodel
    private val sharedViewModel: PrintFileViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // instantiate variables and interactive display tools
        val view = inflater.inflate(R.layout.fragment_formatting, container, false)
        val bedTemp : EditText = view.findViewById(R.id.numPickerBedTemp)
        bedTemp.filters = arrayOf<InputFilter>(MinMaxFilter(0,80))
        val extTemp : EditText = view.findViewById(R.id.numPickerExtTemp)
        extTemp.filters = arrayOf<InputFilter>(MinMaxFilter(0,220))
        val xVal : EditText = view.findViewById(R.id.numPickerX)
        xVal.filters = arrayOf<InputFilter>(MinMaxFilter(0,220))
        val yVal : EditText = view.findViewById(R.id.numPickerY)
        yVal.filters = arrayOf<InputFilter>(MinMaxFilter(0,220))
        val zVal : EditText = view.findViewById(R.id.numPickerZ)
        zVal.filters = arrayOf<InputFilter>(MinMaxFilter(0,250))

        //three state button
        val fanSpeed : Button = view.findViewById(R.id.textView_FanSpeed)
        fanSpeed.setOnClickListener {
            if (fanSpeed.text.toString() == "HIGH")
                fanSpeed.text = "LOW"
            else if (fanSpeed.text.toString() == "LOW")
                fanSpeed.text = "OFF"
            else
                fanSpeed.text = "HIGH"
        }

        //setting up apply button to submit user input values to FB database
        val btn : Button = view.findViewById(R.id.buttonApply)
        btn.setOnClickListener{

            //if print has already started, limit which fields will be submitted
            if(sharedViewModel.readHasStartedPrint()) {

                //setup database reference, printercontrols object with user input, and send to FB database
                database = FirebaseDatabase.getInstance().getReference("Printer Formatting")
                val newFormat = PrinterControls(extTemp.text.toString(), bedTemp.text.toString(), fanSpeed.text.toString(), "", "", "")
                database.child("Format").setValue(newFormat).addOnSuccessListener {
                    Toast.makeText(activity, "Successfully Saved", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(activity, "Failed Saved", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(activity, "Note: Can only change bed and extruder temperatures and fan speed while printing!", Toast.LENGTH_SHORT).show()
            } else {

                //setup database reference, printercontrols object with user input, and send to FB database
                database = FirebaseDatabase.getInstance().getReference("Printer Formatting")
                val newFormat = PrinterControls(extTemp.text.toString(), bedTemp.text.toString(), fanSpeed.text.toString(), xVal.text.toString(), yVal.text.toString(), zVal.text.toString())
                database.child("Format").setValue(newFormat).addOnSuccessListener {
                    Toast.makeText(activity, "Successfully Saved", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(activity, "Failed Saved", Toast.LENGTH_SHORT).show()
                }
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

        //filter function to only allow values within a range
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