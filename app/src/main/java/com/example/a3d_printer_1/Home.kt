package com.example.a3d_printer_1

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*


class Home : Fragment() {

    private lateinit var database : DatabaseReference

//    private lateinit var pControlsDelivered : PrinterControls

    private var xPosDeliveredFragTemporary : String? = null
    private var bedTemp : String? = null

    private var list = mutableListOf<PrinterControls>()

    private var fuck = PrinterControls()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        val btn : Button = view.findViewById(R.id.button)
//        btn.setOnClickListener{
//            val editText : EditText = view.findViewById(R.id.ediText)
//            val input = editText.text.toString()
//            val bundle = Bundle()
//            bundle.putString("data",input)
//            val fragment = Formatting()
//            fragment.arguments = bundle
//            fragmentManager?.beginTransaction()?.replace(R.id.frame_layout,fragment)?.commit()
//        }
        val view = inflater.inflate(R.layout.fragment_home, container, false)
//        val xPosDelivered : TextView = view.findViewById(R.id.xPosDelivered)
        val nameOfFile : TextView = view.findViewById(R.id.nameOfFileToBePrinted)
        val args = this.arguments
//        val inputData = args?.get("data")
        val name = args?.get("fileName")
//        xPosDelivered.text = inputData.toString()
        nameOfFile.text = name.toString()
        getUserData()
        val bedTempDisplay : TextView = view.findViewById(R.id.bedTempDisplay)
        bedTempDisplay.text = bedTemp
//        xPosDeliveredFrag.text = xPosDeliveredFragTemporary
//        for (position in list) {
//            xPosDeliveredFrag.text = position.x_pos
//
//        }
//        xPosDelivered.text = fuck.x_pos
        val btn : Button = view.findViewById(R.id.button2)
        btn.setOnClickListener{
//            xPosDelivered.setText(fuck.x_pos.toString())->
            val fileUrl = args?.get("url").toString()
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
            database.child("Command Print").setValue(commencePrint).addOnSuccessListener {
                Toast.makeText(activity, "Cancel Print!", Toast.LENGTH_LONG).show();
            }.addOnFailureListener {
                Toast.makeText(activity, "Failed to Cancel Print", Toast.LENGTH_SHORT).show();
            }
            btn.text = "Start Print"
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
            true
        }
        return view
    }

    private fun getUserData() {
        database = FirebaseDatabase.getInstance().getReference("Printer Formatting")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children){
                    val pFormat = userSnapshot.key
//                    val etTemp = userSnapshot.child("ext_temp").value.toString()
                    bedTemp = userSnapshot.child("ext_temp").value.toString()
                    val bdTemp = userSnapshot.child("bed_temp").value.toString()
                    val fnSpeed = userSnapshot.child("fan_speed").value.toString()
                    val xpos = userSnapshot.child("x_pos").value.toString()
                    val ypos = userSnapshot.child("y_pos").value.toString()
                    val zpos = userSnapshot.child("z_pos").value.toString()
                    fuck = PrinterControls(bedTemp,bdTemp,fnSpeed,xpos,ypos,zpos)
//                    list.add(pCtrl)
                    Toast.makeText(activity, "FOUND EXT POSITION: $xpos:$ypos:$zpos", Toast.LENGTH_SHORT).show();


                }
//                if (snapshot.exists()){
////                    for (userSnapshot in snapshot.children){
////                        val pFormat = userSnapshot.getValue(PrinterControls::class.java)
//////                        val view = inflater.inflate(R.layout.fragment_home, container, false)
//////                        val textView : TextView = view.findViewById(R.id.textView)
//////                        val args = this.arguments
//////                        val inputData = args?.get("data")
//////                        textView.text = inputData.toString()
////                        pControls_delivered.bed_temp = pFormat.bed_temp.toString()
////
////                    }
////                    val pFormatBedTemp = snapshot.child("Format").child("bed_temp").value.toString()
////                    val pFormatExtTemp = snapshot.child("Format").child("ext_temp").value.toString()
////                    val pFormatFanSpeed = snapshot.child("Format").child("fan_speed").value.toString()
////                    val pFormatXPos = snapshot.child("Format").child("x_pos").value.toString()
////                    val pFormatYPos = snapshot.child("Format").child("y_pos").value.toString()
////                    val pFormatZPos = snapshot.child("Format").child("z_pos").value.toString()
//                    val pControlsDelivered = snapshot.child("Format").getValue(PrinterControls::class.java)
////                    pControlsDelivered(pFormat_bed)
////                    val pFormatting = PrinterControls(pFormatBedTemp,pFormatExtTemp,pFormatFanSpeed,pFormatXPos,pFormatYPos,pFormatZPos)
////                    pControlsDelivered = pFormatting
//                    if (pControlsDelivered != null) {
//                        xPosDeliveredFragTemporary = pControlsDelivered.x_pos.toString()
//                    }
////                    xPosDelivered.text
//                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}


////COMMENTED OUT TO TEST VIDEO, ORIGINAL LINES 9-60===========================================
//// TODO: Rename parameter arguments, choose names that match
//// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"
//
///**
// * A simple [Fragment] subclass.
// * Use the [Home.newInstance] factory method to
// * create an instance of this fragment.
// */
//class Home : Fragment() {
//    // TODO: Rename and change types of parameters
//    private var param1: String? = null
//    private var param2: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_home, container, false)
//    }
//
//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment Home.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            Home().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
//}
////COMMENTED OUT TO TEST VIDEO, ORIGINAL LINES 9-60===========================================

