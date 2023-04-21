package com.example.a3d_printer_1

import android.app.AlertDialog
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
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue


class Home : Fragment() {
    // The following variables are needed to communicate with
    private lateinit var database : DatabaseReference
    private lateinit var databaseSP : DatabaseReference
    private lateinit var databasePC : DatabaseReference
    private lateinit var databaseTESTING: DatabaseReference

    //for fragment communication - sharedview/viewmodel
    private val sharedViewModel: PrintFileViewModel by activityViewModels()

    private var bedTempDisplay :TextView? = null
    private var extTempDisplay :TextView? = null
    private var extPosDisplay :TextView? = null
    private var fanSpeedDisplay :TextView? = null
    private var nameOfFileTest : TextView? = null
    private var btn : Button? = null
    private var simpleChronometer : Chronometer? = null
    private var fileUrl : String? = null
    private var fileName : String? = null
    private var name : String? = null //remove/delete

    //checking database for prev intiated print
    private var prevfileName : String? = null
    private var prevfileNumLines : String? = null
    private var prevPrintStarted : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //TESTING-------------------------------------------------------------------------------------------------------------
//        databaseTESTING = FirebaseDatabase.getInstance().getReference().child("Start Print").child("Command Print")
//        val postListenerTESTING = object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                Toast.makeText(activity, "SHIT WAS UPDATED PLEASE REFRESH PAGE", Toast.LENGTH_SHORT).show();
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(activity, "DID NOT FIND DATA", Toast.LENGTH_SHORT).show();
//            }
//        }
//        databaseTESTING.addValueEventListener(postListenerTESTING)
//        //TESTING-------------------------------------------------------------------------------------------------------------
//        databaseSP = FirebaseDatabase.getInstance().getReference().child("Start Print").child("Command Print")
//        databaseSP.addValueEventListener( object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//                val commandPrint = snapshot.getValue<BeginPrint>()
//                prevPrintStarted = commandPrint!!.beginPrint.toString()
////                sharedViewModel.setFileName(commandPrint!!.fileName.toString())
////                sharedViewModel.setFileUrl(commandPrint!!.fileUrl.toString())
//
//                if (prevPrintStarted == "true") {
//                    prevfileName = commandPrint!!.fileName.toString()
//                    prevfileUrl = commandPrint!!.fileUrl.toString()
////                    sharedViewModel.setHasStartedPrint(true)
////                    sharedViewModel.setHasFile(true)
//                    sharedViewModel.setButtonText("Printing! Hold to cancel pint.")
//                    btn?.text = sharedViewModel.readButtonText()
//                    btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
//                    sharedViewModel.setHasStartedPrint(true)
//                    sharedViewModel.setFileName(prevfileName!!)
//                    sharedViewModel.setFileUrl(prevfileUrl!!)
//                    sharedViewModel.setHasFile(true)
//                    sharedViewModel.setHomeIsBusy(true)
//                    nameOfFileTest?.setText(sharedViewModel.readFileName())
//
////                    fragmentManager?.beginTransaction()?.replace(R.id.frame_layout,Home())?.commit()
//
//                }
//
//                if (prevPrintStarted == "completed") {//new beginprint variable to check if print is complete
////                        val notificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
////                        val CHANNEL_ID = "ChannelID"
////                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////                            val ChannelName = "MyChannel"
////                            val channel = NotificationChannel(CHANNEL_ID,ChannelName,NotificationManager.IMPORTANCE_DEFAULT)
////                            notificationManager.createNotificationChannel(channel)
////                        }
////                        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
////                            .setSmallIcon(R.drawable.notification_icon)
////                            .setContentTitle("My Notification")
////                            .setContentText("Check Printer for newly completed print")
////                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
////                        val notification = notificationBuilder.build()
////                        notificationManager.notify(1, notification)
//
//                    val builder = AlertDialog.Builder(requireActivity())
//                    with(builder) {
//                        setTitle("Print Completed!y")
//                        setPositiveButton("OK") { dialog, which ->
//                            android.widget.Toast.makeText(activity, "Print Completed", android.widget.Toast.LENGTH_SHORT);
//                        }
//                        show()
//                    }
//                }
//
//            }
//
//        override fun onCancelled(error: DatabaseError) {
//            ODO("Not yet implemented")
//        }})
//        //TESTING-------------------------------------------------------------------------------------------------------------
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val nameOfFile : TextView = view.findViewById(R.id.nameOfFileToBePrinted)
        //Testing SharedViewModel here
//        val nameOfFileTest : TextView = view.findViewById(R.id.nameOfFileToBePrintedTest)
        nameOfFileTest = view.findViewById(R.id.nameOfFileToBePrintedTest)
        var args = this.arguments //remove/delete
        name = args?.get("fileName").toString() //remove/delete
        getUserData()
        getUserData1()
//        Toast.makeText(activity, sharedViewModel.readHasFile().toString(), Toast.LENGTH_SHORT).show()
        if (prevPrintStarted == "true") {
            Toast.makeText(activity,"there is a prev file printing", Toast.LENGTH_SHORT).show()
        }
        bedTempDisplay = view.findViewById(R.id.bedTempDisplay)
        extTempDisplay = view.findViewById(R.id.extTempDisplay)
        extPosDisplay = view.findViewById(R.id.extPosDisplay)
        fanSpeedDisplay = view.findViewById(R.id.fanSpeedDisplay)
        simpleChronometer = view.findViewById(R.id.simpleChronometer)

//        val btn : Button = view.findViewById(R.id.button2)
        btn = view.findViewById(R.id.button2)
        btn?.setText(sharedViewModel.readButtonText())
        if (sharedViewModel.readButtonText() == "Printing! Hold to cancel pint.") {
            btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
        } else {
            btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP,30F)
        }

        // TODO start function here? to check if printCompleted is true, clear everything and if not allow for onLongClick to cancel

        if (sharedViewModel.readHasFile()){
            btn?.isEnabled = true
            btn?.isClickable = true
            //Previous if statement delete line above and replace with following for orig: if ((fileUrl != "null") && (name != "null")){
            nameOfFile.text = name.toString() //remove/delete
            //Testing SharedViewModel here
            nameOfFileTest?.setText(sharedViewModel.readFileName())
            btn?.setOnClickListener{
//                fileUrl = args?.get("url").toString() //remove/delete
                //Testing SharedViewModel here
                if (sharedViewModel.readHasFile()) {
                    fileUrl = sharedViewModel.readFileNumLines()
                    fileName = sharedViewModel.readFileName()
                    if (!sharedViewModel.readHasStartedPrint()) {
//                        database = FirebaseDatabase.getInstance().getReference("Start Print")
                        database = FirebaseDatabase.getInstance().getReference("Start Print 3").child("Command Print") //testing please delete
                        val commencePrint = BeginPrint("false","True", fileUrl,fileName)
                        database.setValue(commencePrint).addOnSuccessListener {
                            Toast.makeText(activity, "Begin Print!", Toast.LENGTH_LONG).show();
                        }.addOnFailureListener {
                            Toast.makeText(activity, "Begin Print Failed", Toast.LENGTH_SHORT).show();
                        }
                        sharedViewModel.setButtonText("Printing! Hold to cancel pint.")
                        btn?.text = sharedViewModel.readButtonText()
                        btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
                        sharedViewModel.setHasStartedPrint(true)
                    }
                } else {
                    Toast.makeText(activity, "Please select file from library!", Toast.LENGTH_SHORT).show();
                }
            }
            btn?.setOnLongClickListener{
                val commencePrint = BeginPrint("", "false","","")
                nameOfFile.text = "Select File"
                //Testing SharedViewModel here
                nameOfFileTest?.setText("Select File")
                fileUrl = "null" //possibly remove/delete
                sharedViewModel.setFileName("")
                sharedViewModel.setFileNumLines("")
                sharedViewModel.setHasFile(false)
                sharedViewModel.setHasStartedPrint(false)
                sharedViewModel.setHomeIsBusy(false)


                database.setValue(commencePrint).addOnSuccessListener {
                    Toast.makeText(activity, "Cancel Print!", Toast.LENGTH_LONG).show();
                }.addOnFailureListener {
                    Toast.makeText(activity, "Failed to Cancel Print", Toast.LENGTH_SHORT).show();
                }

                sharedViewModel.setButtonText("Start Print")
                btn?.text = sharedViewModel.readButtonText()
                btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
                true

////                        database = FirebaseDatabase.getInstance().getReference("Start Print")
//                database = FirebaseDatabase.getInstance().getReference("Start Print 3") //testing please delete
//                val commencePrint = BeginPrint("", "false","","")
//                nameOfFile.text = "Select File"
//                //Testing SharedViewModel here
//                nameOfFileTest?.setText("Select File")
//                name = "null" //remove/delete
//                fileUrl = "null"
//                args = null //remove/delete
//                sharedViewModel.setFileName("")
//                sharedViewModel.setFileNumLines("")
//                sharedViewModel.setHasFile(false)
//                sharedViewModel.setHasStartedPrint(false)
//                sharedViewModel.setHomeIsBusy(false)
////                btn.isEnabled = false
//
//
//
//                database.child("Command Print").setValue(commencePrint).addOnSuccessListener {
//                    Toast.makeText(activity, "Cancel Print!", Toast.LENGTH_LONG).show();
//                }.addOnFailureListener {
//                    Toast.makeText(activity, "Failed to Cancel Print", Toast.LENGTH_SHORT).show();
//                }
//
//                sharedViewModel.setButtonText("Start Print")
//                btn?.text = sharedViewModel.readButtonText()
//
//                btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
//                true
            }
        } else {
            btn?.setOnClickListener{
                Toast.makeText(activity, "Please select file from library!", Toast.LENGTH_SHORT).show();
            }
            nameOfFile.setText("Select File")
            //Testing SharedViewModel here
            nameOfFileTest?.setText("Select File")
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
//    data class BeginPrint(val Print_complete: String?=null, val beginPrint: String?=null, val fileNumLines: String?=null, val fileName: String?=null) //delete/remove
    private fun getUserData1() {
        database = FirebaseDatabase.getInstance().getReference("Start Print 3").child("Command Print") //testing please delete
        val postListenerSP = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val commandPrint = snapshot.getValue<BeginPrint>()
                prevPrintStarted = commandPrint!!.print_complete.toString()

                if (prevPrintStarted == "true") {
                    sharedViewModel.setFileName("")
                    sharedViewModel.setFileNumLines("")
                    sharedViewModel.setHasFile(false)
                    sharedViewModel.setHasStartedPrint(false)
                    sharedViewModel.setHomeIsBusy(false)
                    sharedViewModel.setButtonText("Start Print")
                    btn?.text = sharedViewModel.readButtonText()
                    btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
                    true
                    val builder = AlertDialog.Builder(requireActivity())
                    with(builder) {
                        setTitle("Print Completed!")
                        setPositiveButton("OK") { dialog, which ->
                            val commencePrint = BeginPrint("", "false","","")
                            database.setValue(commencePrint)
                            android.widget.Toast.makeText(activity, "Print Completed", android.widget.Toast.LENGTH_SHORT);
                        }
                        show()
                    }
                } else if (prevPrintStarted == "false") {

                        prevfileName = commandPrint!!.fileName.toString()
                        prevfileNumLines = commandPrint!!.fileNumLines.toString()
//                    sharedViewModel.setHasStartedPrint(true)
//                    sharedViewModel.setHasFile(true)
                        sharedViewModel.setButtonText("Printing! Hold to cancel pint.")
//                        btn?.text = sharedViewModel.readButtonText()
//                        btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
                        sharedViewModel.setHasStartedPrint(true)
                        sharedViewModel.setFileName(prevfileName!!)
                        sharedViewModel.setFileNumLines(prevfileNumLines!!)
                        sharedViewModel.setHasFile(true)
                        sharedViewModel.setHomeIsBusy(true)
                        nameOfFileTest?.setText(sharedViewModel.readFileName())

//                        fragmentManager?.beginTransaction()?.replace(R.id.frame_layout,Home())?.commit()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "DID NOT FIND DATA", Toast.LENGTH_SHORT).show();
            }
        }
//        database = FirebaseDatabase.getInstance().getReference("Start Print 3").child("Command Print") //testing please delete
//        val postListenerSP = object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//                val commandPrint = snapshot.getValue<BeginPrint>()
//                prevPrintStarted = commandPrint!!.print_complete.toString()
//
//                if (prevPrintStarted == "true") {
//                    sharedViewModel.setFileName("")
//                    sharedViewModel.setFileNumLines("")
//                    sharedViewModel.setHasFile(false)
//                    sharedViewModel.setHasStartedPrint(false)
//                    sharedViewModel.setHomeIsBusy(false)
//                    sharedViewModel.setButtonText("Start Print")
//                    btn?.text = sharedViewModel.readButtonText()
//                    btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
//                    true
//                    val commencePrint = BeginPrint("", "false","","")
////                    database.setValue(commencePrint)
////                    val builder = AlertDialog.Builder(requireActivity())
////                    with(builder) {
////                        setTitle("Print Completed!")
////                        setPositiveButton("OK") { dialog, which ->
////                            val commencePrint = BeginPrint("", "false","","")
////                            database.setValue(commencePrint)
////                            android.widget.Toast.makeText(activity, "Print Completed", android.widget.Toast.LENGTH_SHORT);
////                        }
////                        show()
////                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(activity, "DID NOT FIND DATA", Toast.LENGTH_SHORT).show();
//            }
//        }
    database.addValueEventListener(postListenerSP)

//        if (!sharedViewModel.readHasStartedPrint() && !sharedViewModel.readHomeIsBusy()) {
////            databaseSP = FirebaseDatabase.getInstance().getReference().child("Start Print").child("Command Print")
//            database = FirebaseDatabase.getInstance().getReference("Start Print 3").child("Command Print") //testing please delete
//            val postListenerSP = object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//
//                    val commandPrint = snapshot.getValue<BeginPrint>()
//                    prevPrintStarted = commandPrint!!.beginPrint.toString()
////                sharedViewModel.setFileName(commandPrint!!.fileName.toString())
////                sharedViewModel.setFileUrl(commandPrint!!.fileUrl.toString())
//
////                    if (prevPrintStarted == "true") {
////                        prevfileName = commandPrint!!.fileName.toString()
////                        prevfileUrl = commandPrint!!.fileUrl.toString()
//////                    sharedViewModel.setHasStartedPrint(true)
//////                    sharedViewModel.setHasFile(true)
////                        sharedViewModel.setButtonText("Printing! Hold to cancel pint.")
////                        btn?.text = sharedViewModel.readButtonText()
////                        btn?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
////                        sharedViewModel.setHasStartedPrint(true)
////                        sharedViewModel.setFileName(prevfileName!!)
////                        sharedViewModel.setFileUrl(prevfileUrl!!)
////                        sharedViewModel.setHasFile(true)
////                        sharedViewModel.setHomeIsBusy(true)
////                        nameOfFileTest?.setText(sharedViewModel.readFileName())
////
////                        fragmentManager?.beginTransaction()?.replace(R.id.frame_layout,Home())?.commit()
////
////                    }
//
//                    if (prevPrintStarted == "completed") {//new beginprint variable to check if print is complete
////                        val notificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
////                        val CHANNEL_ID = "ChannelID"
////                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////                            val ChannelName = "MyChannel"
////                            val channel = NotificationChannel(CHANNEL_ID,ChannelName,NotificationManager.IMPORTANCE_DEFAULT)
////                            notificationManager.createNotificationChannel(channel)
////                        }
////                        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
////                            .setSmallIcon(R.drawable.notification_icon)
////                            .setContentTitle("My Notification")
////                            .setContentText("Check Printer for newly completed print")
////                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
////                        val notification = notificationBuilder.build()
////                        notificationManager.notify(1, notification)
//
//                        val builder = AlertDialog.Builder(requireActivity())
//                        with(builder) {
//                            setTitle("Print Completed!")
//                            setPositiveButton("OK") { dialog, which ->
//                                android.widget.Toast.makeText(activity, "Print Completed", android.widget.Toast.LENGTH_SHORT);
//                            }
//                            show()
//                        }
//                    }
//
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Toast.makeText(activity, "DID NOT FIND DATA", Toast.LENGTH_SHORT).show();
//                }
//            }
//            databaseSP.addValueEventListener(postListenerSP)
//        }
    }
}

//val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//val channelId = "my_channel_id"
//val channelName = "My Channel"
//val importance = NotificationManager.IMPORTANCE_DEFAULT
//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//    val channel = NotificationChannel(channelId, channelName, importance)
//    notificationManager.createNotificationChannel(channel)
//}
//val notificationBuilder = NotificationCompat.Builder(this, channelId)
//    .setContentTitle("Data updated")
//    .setContentText("New value: $data")
//    .setSmallIcon(R.drawable.ic_notification)
//    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//val notification = notificationBuilder.build()
//notificationManager.notify(1, notification)

//myRef.addValueEventListener(object : ValueEventListener {
//    override fun onDataChange(snapshot: DataSnapshot) {
//        val data = snapshot.getValue(String::class.java)
//        if (data != null) {
//            // Show notification
//            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            val channelId = "my_channel_id"
//            val channelName = "My Channel"
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val channel = NotificationChannel(channelId, channelName, importance)
//                notificationManager.createNotificationChannel(channel)
//            }
//            val notificationBuilder = NotificationCompat.Builder(this@MainActivity, channelId)
//                .setContentTitle("Data updated")
//                .setContentText("New value: $data")
//                .setSmallIcon(R.drawable.ic_notification)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            val notification = notificationBuilder.build()
//            notificationManager.notify(1, notification)
//        }
//    }
//
//    override fun onCancelled(error: DatabaseError) {
//        Log.w(TAG, "Failed to read value.", error.toException())
//    }
//})
//Again, note that data is the updated value of the data in Firebase Realtime Database. You will need to replace this with the appropriate value for your app.
//




