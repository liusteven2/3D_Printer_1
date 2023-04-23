package com.example.a3d_printer_1

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.ContactsContract
import android.provider.OpenableColumns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
//import com.example.a3d_printer_1.databinding.ActivityMainBinding
//import com.example.a3d_printer_1.databinding.FragmentLibraryBinding
//sharedViewModel/Livedata begin imports
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.a3d_printer_1.model.PrintFileViewModel //was auto added
//sharedViewModel/Livedata end imports
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.BuildConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.tasks.await
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import java.text.DecimalFormat
import kotlin.collections.HashMap
import kotlin.math.log10
import kotlin.math.pow

class Library : Fragment() {

    private lateinit var uploadTask: UploadTask

//    //Uri for file in device
    private var fileUri: Uri? = null
    private var fileName: String? = null
    private var gcodeFile: gcodeFileClass? = null
    private var fileLengthReadable: String? = null
    private var fileNameNow: String? = null

    //creating recyclerview and receiving information from firebase
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userArrayList : ArrayList<gcodeFileClass>

    //for sending information to firebase database
    private lateinit var database : DatabaseReference
    private lateinit var databaseParsedLines : DatabaseReference
    private lateinit var storage : StorageReference

    //for fragment communication - sharedview/viewmodel
    private val sharedViewModel: PrintFileViewModel by activityViewModels()

    //temp start print variables
    private var temp_fileName: String? = null
    private var temp_fileUrl: String? = null

    //Adapter used to format interacive library
    private lateinit var adapter : MyAdapter

    //variable used for counting number of nodes
    private var i_fb_line: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //instantiate library tab view to access display tools
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        //instantiate upload button to XML button
        val selectbtn : Button = view.findViewById(R.id.selectBtn)

        //database file reference variables
        database = FirebaseDatabase.getInstance().getReference("Print Files") //used to display related file info to user
        databaseParsedLines = FirebaseDatabase.getInstance().getReference("Print Files Readable") //used to store parsed gcode files

        //retrieve file from internal storage
        val getFile = registerForActivityResult(ActivityResultContracts.GetContent(),
            ActivityResultCallback {

                //used for pulling file info, deconstructed in "Upload()" function
                if (it != null) {
                    fileUri = it

                    //get internal file name
                    val cursor = requireContext().contentResolver.query(it!!, null, null, null, null)
                    val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor?.moveToFirst()
                    val internalFileName = cursor?.getString(nameIndex!!)
                    cursor?.close()

                    if ((it != Uri.EMPTY) && internalFileName!!.endsWith(".gcode")) {
                        Toast.makeText(activity, it.path?.toString(), Toast.LENGTH_LONG)
                        fileUri = it!!

                        //initializing visuals
                        val builder = AlertDialog.Builder(requireActivity())
                        val editText_view =
                            inflater.inflate(R.layout.edit_text_layout, container, false)
                        val editText: EditText = editText_view.findViewById(R.id.et_editText)

                        //pop up dialog for file name & FB parsed upload
                        with(builder) {
                            setTitle("Enter name of file!")
                            setPositiveButton("OK") { dialog, which ->
                                uploadFile() //function to set up relative file info

                                //file transfer loading visual
                                val progressDialog = ProgressDialog(activity)
                                progressDialog.setMessage("Uploading File...")
                                progressDialog.setCancelable(false)
                                progressDialog.show()

                                //if user does not input name, default to internal file name
                                if (editText.text.toString() == "") {
                                    fileName = internalFileName.substring(0, internalFileName.indexOf(".gcode"))
                                } else {
                                    fileName = editText.text.toString()
                                }

                                //open selected file, parse, and send to FB
                                val inputStream = it?.let { it1 -> activity?.contentResolver?.openInputStream(it1) }
                                val reader: BufferedReader? = BufferedReader(InputStreamReader(inputStream))
                                var line: String? = reader?.readLine()
                                var i_line: Int = 1
                                val stringBuilder = StringBuilder()
                                i_fb_line = 1

                                //if not at the end of file
                                while (line != null) {

                                    //parse every 100 gcode lines into a single FB database node
                                    if (i_line % 100 < 100) {

                                        //only read pertinent file lines
                                        if ((line.firstOrNull() == 'G') or (line.firstOrNull() == 'M')) {
                                            i_line = i_line?.inc()

                                            //ignore comments after line
                                            if (line.indexOf(';') > 0) {
                                                line = line.substring(0, line.indexOf(';'))
                                            }

                                            stringBuilder.append(line) //appending parsed gcode lines for upload
                                            stringBuilder.append("/") //use '/' as delimiter
                                        }
                                    }

                                    //if 100 lines parsed, send to FB into single node
                                    if (i_line % 100 == 0) {
                                        if ((line.firstOrNull() == 'G') or (line.firstOrNull() == 'M')) {
                                            databaseParsedLines.child(fileName!!).child(i_fb_line.toString()).setValue(stringBuilder.toString())
                                            i_fb_line = i_fb_line.inc() //count number of nodes
                                            stringBuilder.clear() //clear parsed gcode string
                                        }
                                    }

                                    line = reader?.readLine() //iterate to next line

                                    //if at the end of file perform final FB upload
                                    if (line == null) {
                                        databaseParsedLines.child(fileName!!).child(i_fb_line.toString()).setValue(stringBuilder.toString()).addOnSuccessListener {
                                            if (progressDialog.isShowing) progressDialog.dismiss()
                                            Toast.makeText(activity, "File Upload Success!", Toast.LENGTH_SHORT).show()
                                        }.addOnFailureListener{
                                            if (progressDialog.isShowing) progressDialog.dismiss()
                                            Toast.makeText(activity, "File Upload Failed!", Toast.LENGTH_SHORT).show()
                                        }
                                        stringBuilder.clear()
                                    }
                                }
                            }
                            setNegativeButton("Cancel") { dialog, which ->
                                Toast.makeText(activity, "File Upload Canceled", Toast.LENGTH_SHORT)
                                    .show();
                            }
                            setView(editText_view)
                            show()
                        }
                    } else {
                        Toast.makeText(activity, "Cancelled: not a gcode file", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(activity, "File Upload Cancelled", Toast.LENGTH_LONG).show()
                }
            })

        //pop up dialog confirming desire to upload new file
        selectbtn.setOnClickListener{
            val builder1 = AlertDialog.Builder(requireActivity())
            val tet_textView =inflater.inflate(R.layout.textview_layout, container, false)
            val textView: TextView = tet_textView.findViewById(R.id.et_textView)

            with(builder1) {
                setPositiveButton("Continue") { dialog, which ->
                    getFile.launch("application/octet-stream")
                }
                setNegativeButton("Cancel") { dialog, which ->
                    Toast.makeText(activity, "Canceled File Upload", Toast.LENGTH_SHORT).show();
                }
                setView(tet_textView)
                show()
            }
        }
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //for receiving firebase data into recyclerview
        val layoutManager = LinearLayoutManager(context)

        //interactive display window to visualize library
        userRecyclerView = view.findViewById(R.id.recycler_view)
        userRecyclerView.layoutManager = layoutManager
        userRecyclerView.setHasFixedSize(true)
        userArrayList = arrayListOf<gcodeFileClass>()

        //filter in gcode files and related info from FB database
        adapter = MyAdapter(userArrayList)
        getUserData()
    }


    //function used for updating interactive library handling user interactions with library select file to print or hold for delete
    private fun getUserData() {
        database = FirebaseDatabase.getInstance().getReference("Print Files")
        database.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                //if FB was updated add item to interactive library
                if (snapshot.exists()){
                    for (userSnapshot in snapshot.children){
                        val gcodefile = userSnapshot.getValue(gcodeFileClass::class.java)
                        userArrayList.add(gcodefile!!) //note double exclamation points will throw an exception on null value
                    }
                    userRecyclerView.adapter = adapter //update view

                    adapter.setOnClickListener(object : MyAdapter.onItemClickListener{

                        //short click on item sends info to home tab to initiate print
                        override fun onItemClick(position: Int) {

                            //only allow when printer has not already started printing
                            if (!sharedViewModel.readHasStartedPrint()) {
                                Toast.makeText(activity, "Clicked on File: " + userArrayList[position].name, Toast.LENGTH_SHORT).show();
                                temp_fileName = userArrayList[position].name
                                temp_fileUrl = userArrayList[position].numLines

                                //sharedViewModel - liveData objects necessary for communication between tabs
                                sharedViewModel.setFileName(temp_fileName!!)
                                sharedViewModel.setFileNumLines(temp_fileUrl!!)
                                sharedViewModel.setHasFile(true)
                                sharedViewModel.setHomeIsBusy(true)
                            } else {
                                Toast.makeText(activity, "Print in progress! Please cancel current print to select a new print file.", Toast.LENGTH_LONG).show();
                            }

                        }

                        //long click for deleting files
                        override fun onLongItemClick(position: Int) {
                            temp_fileName = userArrayList[position].name
                            if (isAdded) {

                                //pop up dialog to confirm delete
                                val builder = AlertDialog.Builder(requireContext())
                                with(builder) {
                                    setTitle("Are you sure you want to delete: $temp_fileName?")
                                    setPositiveButton("OK") { dialog, which ->

                                        //removing file and related info from FB database
                                        database.child(temp_fileName.toString()).removeValue().addOnSuccessListener {

                                            //notify user and refresh tab to ensure variable and visuals are up to date
                                            Toast.makeText(activity, "Successfully Removed!", Toast.LENGTH_LONG).show();
                                            fragmentManager?.beginTransaction()?.replace(R.id.frame_layout,Library())?.commit()
                                        }
                                        databaseParsedLines.child(temp_fileName.toString()).removeValue()
                                        storage = FirebaseStorage.getInstance().getReference("New Print Files/"+temp_fileName)
                                    }
                                    setNegativeButton("Cancel") { dialog, which ->
                                        Toast.makeText(activity, "Canceled!", Toast.LENGTH_SHORT).show();
                                    }
                                    show()
                                }

                            }
                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "Data retrieval error", Toast.LENGTH_SHORT).show()
            }

        })

    }


    //function used to pull related file info
    private fun uploadFile() {

        //setting and formatting upload timestamp
        val formatter = SimpleDateFormat("MM/dd/yyyy, HH:mm:ss", Locale.getDefault())
        val now = Date()
        fileNameNow = formatter.format(now)

        //used only for easily pulling size info, no time to re-implement and test
        storage = FirebaseStorage.getInstance().getReference("New Print Files/"+fileName)
        uploadTask = storage.putFile(fileUri!!)
        var pfd = requireActivity().contentResolver.openFileDescriptor(fileUri!!, "r")
        var fileLength: Long? = pfd!!.getStatSize()
        fileLengthReadable = fileLength?.readableFormat()

        //if file successfully uploaded to FB storage
        uploadTask.addOnSuccessListener{

            //create gcodeFileClass object to send to FB database
            gcodeFile = gcodeFileClass(fileName, i_fb_line.toString(), fileNameNow.toString(), fileLengthReadable.toString())
            database.child(fileName!!).setValue(gcodeFile).addOnSuccessListener {

                //refresh tab to ensure variable and visuals are up to date
                fragmentManager?.beginTransaction()?.replace(R.id.frame_layout,Library())?.commit()
            }.addOnFailureListener {
                Toast.makeText(activity, "Failed Saved to Database", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //helper function to handle file size formatting
    fun Long.readableFormat(): String {
        if (this <= 0 ) return "0"
        val units = arrayOf("iB", "kiB", "MiB", "GiB", "TiB")
        val digitGroups = (log10(this.toDouble()) / log10(1024.00)).toInt()
        return DecimalFormat("#,##0.#").format(this / 1024.00.pow(digitGroups.toDouble())).toString() + " " + units[digitGroups]
    }}

