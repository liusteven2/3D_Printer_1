package com.example.a3d_printer_1

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.activityViewModels //was auto added - needed for sharedViewModel/LiveData
import com.example.a3d_printer_1.model.PrintFileViewModel //was auto added - needed for sharedViewModel/LiveData
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue


class Home : Fragment() {
    // The following variables are needed to communicate with
    private lateinit var database : DatabaseReference
    private lateinit var databasePC : DatabaseReference

    //for fragment communication - sharedview/viewmodel
    private val sharedViewModel: PrintFileViewModel by activityViewModels()

    private var bedTempDisplay :TextView? = null
    private var extTempDisplay :TextView? = null
    private var extPosDisplay :TextView? = null
    private var fanSpeedDisplay :TextView? = null
    private var nameOfFile : TextView? = null
    private var btn : Button? = null
    private var simpleChronometer : Chronometer? = null
    private var fileNumNodes : String? = null
    private var fileName : String? = null

    //checking database for prev initiated print
    private var prevfileName : String? = null
    private var prevfileNumLines : String? = null
    private var prevPrintCompleted : String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //instantiate view and variables to access display tools
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        nameOfFile = view.findViewById(R.id.nameOfFileToBePrinted)
        bedTempDisplay = view.findViewById(R.id.bedTempDisplay)
        extTempDisplay = view.findViewById(R.id.extTempDisplay)
        extPosDisplay = view.findViewById(R.id.extPosDisplay)
        fanSpeedDisplay = view.findViewById(R.id.fanSpeedDisplay)
        simpleChronometer = view.findViewById(R.id.simpleChronometer)

        //functions used for checking FB database and updating view
        getFormattingData()
        getPrintStatus()

        //initialize text on button
        btn = view.findViewById(R.id.button2)
        btn?.setText(sharedViewModel.readButtonText())
        if (sharedViewModel.readButtonText() == "Printing! Hold to cancel pint.") {
            btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
        } else {
            btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP,30F)
        }

        //if file has been selected allow user to print
        if (sharedViewModel.readHasFile()){

            //update name to file selected and communicate print command to FB
            nameOfFile?.setText(sharedViewModel.readFileName())
            btn?.setOnClickListener{

                //set local variables to shared variable values
                fileNumNodes = sharedViewModel.readFileNumLines()
                fileName = sharedViewModel.readFileName()

                //if print has not started allow, send print command to FB
                if (!sharedViewModel.readHasStartedPrint()) {

                    //initialize database reference variable and update FB command print node
                    database = FirebaseDatabase.getInstance().getReference("Start Print").child("Command Print")
                    val commencePrint = BeginPrint("false","true", fileNumNodes,fileName)
                    database.setValue(commencePrint).addOnSuccessListener {
                        Toast.makeText(activity, "Begin Print!", Toast.LENGTH_LONG).show()
                    }.addOnFailureListener {
                        Toast.makeText(activity, "Begin Print Failed", Toast.LENGTH_SHORT).show()
                    }

                    //update display and shared variables
                    sharedViewModel.setButtonText("Printing! Hold to cancel pint.")
                    btn?.text = sharedViewModel.readButtonText()
                    btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
                    sharedViewModel.setHasStartedPrint(true)
                } else {
                    Toast.makeText(activity, "Print in progress!", Toast.LENGTH_SHORT).show()
                }
            }

            //long click for canceling a print in progress
            btn?.setOnLongClickListener{

                //updating local variables and display as well as shared variables
                nameOfFile?.setText("Select File")
                fileNumNodes = "null"
                sharedViewModel.setFileName("")
                sharedViewModel.setFileNumLines("")
                sharedViewModel.setHasFile(false)
                sharedViewModel.setHasStartedPrint(false)
                sharedViewModel.setHomeIsBusy(false)
                sharedViewModel.setButtonText("Start Print")
                btn?.text = sharedViewModel.readButtonText()
                btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)

                //update FB database command print node
                val commencePrint = BeginPrint("", "false","","")
                database.setValue(commencePrint).addOnSuccessListener {
                    Toast.makeText(activity, "Cancel Print!", Toast.LENGTH_LONG).show()
                }.addOnFailureListener {
                    Toast.makeText(activity, "Failed to Cancel Print", Toast.LENGTH_SHORT).show()
                }
                true
            }
        } else {
            btn?.setOnClickListener{
                Toast.makeText(activity, "Please select file from library!", Toast.LENGTH_SHORT).show()
            }
            nameOfFile?.setText("Select File")
        }
        return view
    }

    //function used for retrieving realtime printer formatting
    private fun getFormattingData() {

        //set FB database reference variable and listen for updates
        databasePC = FirebaseDatabase.getInstance().reference.child("Printer Formatting Test").child("Format")
        val postListener = object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {

                //update display tools with received FB updated values
                val pc = snapshot.getValue<PrinterControls>()
                bedTempDisplay?.setText(pc?.bed_temp.toString()+"\u00B0")
                extTempDisplay?.setText(pc?.ext_temp.toString()+"\u00B0")
                extPosDisplay?.setText(pc?.x_pos.toString()+"."+pc?.y_pos.toString()+"."+pc?.z_pos.toString())
                fanSpeedDisplay?.setText(pc?.fan_speed.toString())
                simpleChronometer?.start()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "DID NOT FIND DATA", Toast.LENGTH_SHORT).show()
            }
        }
        databasePC.addValueEventListener(postListener)
    }

    //function used for retrieving realtime print status
    private fun getPrintStatus() {

        //set FB database reference variable and listen for updates
        database = FirebaseDatabase.getInstance().getReference("Start Print").child("Command Print")
        val postListenerSP = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                //get print status, if complete, notify user and reset internal and FB variables/nodes
                val commandPrint = snapshot.getValue<BeginPrint>()
                prevPrintCompleted = commandPrint!!.print_complete.toString()

                if (prevPrintCompleted == "true") {

                    //resetting variables and updating visual tools
                    sharedViewModel.setFileName("")
                    sharedViewModel.setFileNumLines("")
                    sharedViewModel.setHasFile(false)
                    sharedViewModel.setHasStartedPrint(false)
                    sharedViewModel.setHomeIsBusy(false)
                    sharedViewModel.setButtonText("Start Print")
                    btn?.text = sharedViewModel.readButtonText()
                    btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
                    true

                    //reset FB database values
                    val commencePrint = BeginPrint("", "false","","")
                    database.setValue(commencePrint)

                    //display dialog to notify user print completion
                    if (isAdded) {
                        val builder = AlertDialog.Builder(requireContext())
                        with(builder) {
                            setTitle("Print Completed!")
                            setPositiveButton("OK") { dialog, which ->

                                //refresh tab to ensure variable and visuals are up to date
                                fragmentManager?.beginTransaction()?.replace(R.id.frame_layout,Home())?.commit()
                            }
                            show()
                        }

                    }
                }

                //if app has been restarted update visuals and notify print progress to user
                else if ((prevPrintCompleted == "false") and !(sharedViewModel.readHasStartedPrint())) {

                        //update variables and visual tools to display current print
                        prevfileName = commandPrint.fileName.toString()
                        prevfileNumLines = commandPrint.fileNumLines.toString()
                        sharedViewModel.setButtonText("Printing! Hold to cancel pint.")
                        btn?.text = sharedViewModel.readButtonText()
                        btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
                        sharedViewModel.setHasStartedPrint(true)
                        sharedViewModel.setFileName(prevfileName!!)
                        sharedViewModel.setFileNumLines(prevfileNumLines!!)
                        sharedViewModel.setHasFile(true)
                        sharedViewModel.setHomeIsBusy(true)
                        nameOfFile?.setText(sharedViewModel.readFileName())

                        //notify the user that a print is still in progress if restarting app
                        if (isAdded) {
                            val builder = AlertDialog.Builder(requireContext())
                            with(builder) {
                                setTitle("Print in Progress...")
                                setPositiveButton("OK") { dialog, which ->

                                    //refresh tab to ensure variable and visuals are up to date
                                    fragmentManager?.beginTransaction()?.replace(R.id.frame_layout,Home())?.commit()
                                }
                                show()
                            }

                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "DID NOT FIND DATA", Toast.LENGTH_SHORT).show()
            }
        }
    database.addValueEventListener(postListenerSP)
    }
}
