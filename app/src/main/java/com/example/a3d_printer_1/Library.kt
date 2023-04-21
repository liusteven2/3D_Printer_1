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
import com.example.a3d_printer_1.databinding.ActivityMainBinding
import com.example.a3d_printer_1.databinding.FragmentLibraryBinding
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
    private var fileUrl1: String? = null
    private var fileUrl: String? = null
    private var gcodeFile: gcodeFileClass? = null
    private var fileLengthReadable: String? = null
    private var fileNameNow: String? = null
    private var fileUrlDatabase: String? = null

    //creating recyclerview and receiving information from firebase
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userArrayList : ArrayList<gcodeFileClass>

    //for sending information to firebase database
    private lateinit var database : DatabaseReference
    private lateinit var databaseGF : DatabaseReference
    private lateinit var databasePC : DatabaseReference
    private lateinit var databaseParsedLines : DatabaseReference
    private lateinit var storage : StorageReference

    //for fragment communication - sharedview/viewmodel
    private val sharedViewModel: PrintFileViewModel by activityViewModels()

    //checking for url successfully upload
    private var isUrlUploaded: Boolean? = false

    //temp start print variables
    private var temp_fileName: String? = null
    private var temp_fileUrl: String? = null

    //testing begin for swipetodelte
    private lateinit var adapter : MyAdapter

    private var i_fb_line: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)
        val selectbtn : Button = view.findViewById(R.id.selectBtn)
//        database = FirebaseDatabase.getInstance().getReference("Print Files")
        database = FirebaseDatabase.getInstance().getReference("Print Files 3") //please delete
//        databaseParsedLines = FirebaseDatabase.getInstance().getReference("Print Files Readable")
        databaseParsedLines = FirebaseDatabase.getInstance().getReference("Print Files Readable 3") //please delete
//        databaseParsedLines = FirebaseDatabase.getInstance().getReference("TestingSHIT")

        val getFile = registerForActivityResult(ActivityResultContracts.GetContent(),
            ActivityResultCallback {
//                //testing
                val builder_PF_Readable = AlertDialog.Builder(requireActivity())
                val editText_view =
                    inflater.inflate(R.layout.edit_text_layout, container, false)
                val editText: EditText = editText_view.findViewById(R.id.et_editText)

                with(builder_PF_Readable) {
                    setTitle("Enter name of file!")
                    setPositiveButton("OK") { dialog, which ->
                        uploadFile()
                        val progressDialog = ProgressDialog(activity)
                        progressDialog.setMessage("Uploading File...")
                        progressDialog.setCancelable(false)
                        progressDialog.show()
                        fileName = editText.text.toString()
                        val inputStream = it?.let { it1 -> activity?.contentResolver?.openInputStream(it1) }
                        val reader: BufferedReader? = BufferedReader(InputStreamReader(inputStream))
                        var line: String? = reader?.readLine()
                        var i_line: Int = 1
                        val stringBuilder = StringBuilder()
                        i_fb_line = 1
                        while (line != null) {
                            if (i_line % 100 < 100) {
                                if ((line.firstOrNull() == 'G') or (line.firstOrNull() == 'M')) {
                                    i_line = i_line?.inc()
                                    if (line.indexOf(';') > 0) {
                                        line = line.substring(0, line.indexOf(';'))
                                    }
                                    stringBuilder.append(line)
                                    stringBuilder.append("/")
                                }
                            }
                            if (i_line % 100 == 0) {
                                if ((line.firstOrNull() == 'G') or (line.firstOrNull() == 'M')) {
                                    databaseParsedLines.child(fileName!!).child(i_fb_line.toString()).setValue(stringBuilder.toString())
                                    i_fb_line = i_fb_line.inc()
                                    stringBuilder.clear()
                                }
                            }
                            line = reader?.readLine()
                            if (line == null) {
                                databaseParsedLines.child(fileName!!).child(i_fb_line.toString()).setValue(stringBuilder.toString()).addOnSuccessListener {
                                    if (progressDialog.isShowing) progressDialog.dismiss()
                                }.addOnFailureListener{
                                    if (progressDialog.isShowing) progressDialog.dismiss()
                                }
//                                i_fb_line = i_fb_line.inc()
                                stringBuilder.clear()
                            }
                        }
//                        uploadFile()
//                        if (progressDialog.isShowing) progressDialog.dismiss()
                    }
                    setNegativeButton("Cancel") { dialog, which ->
                        Toast.makeText(activity, "File Upload Canceled", Toast.LENGTH_SHORT)
                            .show();
                    }
                    setView(editText_view)
                    show()
                }

                if (it != null) {
//                    if (it != Uri.EMPTY && it.path?.endsWith(".gcode") == true) {
                    fileUri = it!!
//                    val builder = AlertDialog.Builder(requireActivity())
//                    val editText_view =
//                        inflater.inflate(R.layout.edit_text_layout, container, false)
//                    val editText: EditText = editText_view.findViewById(R.id.et_editText)
//
//                    with(builder) {
//                        setTitle("Enter name of file!")
//                        setPositiveButton("OK") { dialog, which ->
//                            fileName = editText.text.toString()
//                            uploadFile()
//                        }
//                        setNegativeButton("Cancel") { dialog, which ->
//                            Toast.makeText(activity, "File Upload Canceled", Toast.LENGTH_SHORT)
//                                .show();
//                        }
//                        setView(editText_view)
//                        show()
//                    }
//                    } else {
//                        Toast.makeText(activity, "Cancelled, not a gcode file", Toast.LENGTH_LONG).show()
//                    }
                } else {
                    Toast.makeText(activity, "File Upload Cancelled", Toast.LENGTH_LONG).show()
                }
            })

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

    private fun uploadFile() {
//        val progressDialog = ProgressDialog(activity)
//        progressDialog.setMessage("Uploading File...")
//        progressDialog.setCancelable(false)
//        progressDialog.show()


        val formatter = SimpleDateFormat("MM/dd/yyyy, HH:mm:ss", Locale.getDefault())
        val now = Date()
        fileNameNow = formatter.format(now)
        storage = FirebaseStorage.getInstance().getReference("Print Files/"+fileName)
        uploadTask = storage.putFile(fileUri!!)
        var pfd = requireActivity().contentResolver.openFileDescriptor(fileUri!!, "r")
        var fileLength: Long? = pfd!!.getStatSize()
        fileLengthReadable = fileLength?.readableFormat()


        uploadTask.addOnFailureListener {
//            Toast.makeText(activity, "File Upload to Storage Failed", Toast.LENGTH_SHORT).show()
//            if (progressDialog.isShowing) progressDialog.dismiss()
        }.addOnSuccessListener{
//            Toast.makeText(activity, "File Upload to Storage Success", Toast.LENGTH_SHORT).show()
//            if (progressDialog.isShowing) progressDialog.dismiss()
            storage.downloadUrl.addOnSuccessListener {
                fileUrl = it.toString()
            }

            gcodeFile = gcodeFileClass(fileName, "", fileNameNow.toString(), fileLengthReadable.toString())


            val counter: Int? = 0
//            if (progressDialog.isShowing) progressDialog.dismiss()

            database.child(fileName!!).setValue(gcodeFile).addOnSuccessListener {
                database.child(fileName!!).child("numLines").setValue(i_fb_line.toString())
            }.addOnFailureListener {
                Toast.makeText(activity, "Failed Saved to Database", Toast.LENGTH_SHORT).show();
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //for receiving firebase data into recyclerview
        val layoutManager = LinearLayoutManager(context)

        userRecyclerView = view.findViewById(R.id.recycler_view)
        userRecyclerView.layoutManager = layoutManager
        userRecyclerView.setHasFixedSize(true)

        userArrayList = arrayListOf<gcodeFileClass>()
        adapter = MyAdapter(userArrayList)
        getUserData()
//        swipeToDelete()
    }


    private fun getUserData() {
//        database = FirebaseDatabase.getInstance().getReference("Print Files")
        database = FirebaseDatabase.getInstance().getReference("Print Files 3") //please delete

        database.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (userSnapshot in snapshot.children){
                        val gcodefile = userSnapshot.getValue(gcodeFileClass::class.java)
                        userArrayList.add(gcodefile!!) //note double exclamation points will throw an exception on null value
                    }
//                    userRecyclerView.adapter = MyAdapter(userArrayList)
//                    var adapter = MyAdapter(userArrayList)
                    userRecyclerView.adapter = adapter
                    adapter.setOnClickListener(object : MyAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {

                            if (!sharedViewModel.readHasStartedPrint()) {
                                Toast.makeText(activity, "Clicked on File: " + userArrayList[position].name, Toast.LENGTH_SHORT).show();
//                            val bundle = Bundle()
//                            bundle.putString("fileName",userArrayList[position].name)
//                            bundle.putString("url",userArrayList[position].url)
//                            val fragment = Home()
//                            fragment.arguments  = bundle
//                            fragmentManager?.beginTransaction()?.replace(R.id.frame_layout,fragment)?.commit()
                                //testing sharedViewModel
                                temp_fileName = userArrayList[position].name
                                temp_fileUrl = userArrayList[position].numLines //TESTING PLEASE CHANGE
                                sharedViewModel.setFileName(temp_fileName!!)
                                sharedViewModel.setFileNumLines(temp_fileUrl!!)
                                sharedViewModel.setHasFile(true)
                                sharedViewModel.setHomeIsBusy(true)
                            } else {
                                Toast.makeText(activity, "Print in progress! Please cancel current print to select a new print file.", Toast.LENGTH_LONG).show();
                            }

                        }

                        override fun onLongItemClick(position: Int) {
                            Toast.makeText(activity, "long click is working", Toast.LENGTH_LONG).show();
                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "Data retrieval error", Toast.LENGTH_SHORT).show()
            }

        })

    }

//    private fun swipeToDelete() {
//        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
//            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
//            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
//        ){
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean {
//                return true
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                val position = viewHolder.adapterPosition
//                val item = userArrayList[position]
//
//                database
//                userArrayList.removeAt(position)
//                adapter.notifyItemRemoved(position)
////                Snackbar.make(
////                    activity,
////                    "Item '$item $position Deleted",
////                    Snackbar.LENGTH_SHORT
////                ).apply {
////                    setAction("Undo"){
////
////                    }
////                    show()
////                }
////                val snackbar = Snackbar.make(requireView(), "Item '$item $position Deleted", Snackbar.LENGTH_SHORT)
//////                snackbar.setAction("Undo") {
//////                    userArrayList.add(item)
//////                }
////                snackbar.show()
//            }
//
//
//        }).attachToRecyclerView(userRecyclerView)
//    }

    fun Long.readableFormat(): String {
        if (this <= 0 ) return "0"
        val units = arrayOf("iB", "kiB", "MiB", "GiB", "TiB")
        val digitGroups = (log10(this.toDouble()) / log10(1024.00)).toInt()
        return DecimalFormat("#,##0.#").format(this / 1024.00.pow(digitGroups.toDouble())).toString() + " " + units[digitGroups]
    }}

