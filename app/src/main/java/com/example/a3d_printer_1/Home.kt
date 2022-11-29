package com.example.a3d_printer_1

import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue


class Home : Fragment() {

    private lateinit var database : DatabaseReference
    private lateinit var databasePC : DatabaseReference

    private var bedTempDisplay :TextView? = null
    private var extTempDisplay :TextView? = null
    private var extPosDisplay :TextView? = null
    private var fanSpeedDisplay :TextView? = null
    private var simpleChronometer : Chronometer? = null
    private var fileUrl : String? = null
    private var name : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val nameOfFile : TextView = view.findViewById(R.id.nameOfFileToBePrinted)
        var args = this.arguments
        name = args?.get("fileName").toString()
        getUserData()
        bedTempDisplay = view.findViewById(R.id.bedTempDisplay)
        extTempDisplay = view.findViewById(R.id.extTempDisplay)
        extPosDisplay = view.findViewById(R.id.extPosDisplay)
        fanSpeedDisplay = view.findViewById(R.id.fanSpeedDisplay)
        simpleChronometer = view.findViewById(R.id.simpleChronometer)

        val btn : Button = view.findViewById(R.id.button2)
        if ((fileUrl != "null") && (name != "null")){
            nameOfFile.text = name.toString()
            btn.setOnClickListener{
//            xPosDelivered.setText(fuck.x_pos.toString())->
                fileUrl = args?.get("url").toString()
                database = FirebaseDatabase.getInstance().getReference("Start Print")
                val commencePrint = BeginPrint("true",fileUrl)
                database.child("Command Print").setValue(commencePrint).addOnSuccessListener {
                    Toast.makeText(activity, "Begin Print!", Toast.LENGTH_LONG).show();
                }.addOnFailureListener {
                    Toast.makeText(activity, "Begin Print Failed", Toast.LENGTH_SHORT).show();
                }
                btn.text = "Printing! Hold to cancel print."
                btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
            }
            btn.setOnLongClickListener{
                database = FirebaseDatabase.getInstance().getReference("Start Print")
                val commencePrint = BeginPrint("false",null)
                nameOfFile.text = "Empty"
                name = "null"
                fileUrl = "null"
                args = null
                database.child("Command Print").setValue(commencePrint).addOnSuccessListener {
                    Toast.makeText(activity, "Cancel Print!", Toast.LENGTH_LONG).show();
                }.addOnFailureListener {
                    Toast.makeText(activity, "Failed to Cancel Print", Toast.LENGTH_SHORT).show();
                }
                btn.text = "Start Print"
                btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
                true
            }
        } else {
            btn.setOnClickListener{
                Toast.makeText(activity, "Please select file from library!", Toast.LENGTH_SHORT).show();
            }
            nameOfFile.setText("Empty")
        }
        return view
    }

    private fun getUserData() {
        databasePC = FirebaseDatabase.getInstance().getReference().child("Printer Formatting").child("Format")
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pc = snapshot.getValue<PrinterControls>()
                bedTempDisplay?.setText(pc?.bed_temp.toString()+"\u00B0")
                extTempDisplay?.setText(pc?.ext_temp.toString()+"\u00B0")
                extPosDisplay?.setText(pc?.x_pos.toString()+"."+pc?.y_pos.toString()+"."+pc?.z_pos.toString())
                fanSpeedDisplay?.setText(pc?.fan_speed.toString())
                simpleChronometer?.start()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "DID NOT FIND DATA", Toast.LENGTH_SHORT).show();
            }
        }
        databasePC.addValueEventListener(postListener)
    }
}