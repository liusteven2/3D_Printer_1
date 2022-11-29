package com.example.a3d_printer_1

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
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
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.BuildConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.tasks.await
import java.io.IOException
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
    private lateinit var storage : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)
        val selectbtn : Button = view.findViewById(R.id.selectBtn)
        database = FirebaseDatabase.getInstance().getReference("Print Files")

        val getFile = registerForActivityResult(ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                if (it != Uri.EMPTY) {
                    fileUri = it!!
                    val builder = AlertDialog.Builder(requireActivity())
                    val editText_view =
                        inflater.inflate(R.layout.edit_text_layout, container, false)
                    val editText: EditText = editText_view.findViewById(R.id.et_editText)

                    with(builder) {
                        setTitle("Enter name of file!")
                        setPositiveButton("OK") { dialog, which ->
                            fileName = editText.text.toString()
                            uploadFile()
                        }
                        setNegativeButton("Cancel") { dialog, which ->
                            Toast.makeText(activity, "Canceled File Upload", Toast.LENGTH_SHORT)
                                .show();
                        }
                        setView(editText_view)
                        show()
                    }
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
        val progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Uploading File...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        fileNameNow = formatter.format(now)
        storage = FirebaseStorage.getInstance().getReference("Print Files/"+fileName)
        uploadTask = storage.putFile(fileUri!!)
        var pfd = requireActivity().contentResolver.openFileDescriptor(fileUri!!, "r")
        var fileLength: Long? = pfd!!.getStatSize()
        fileLengthReadable = fileLength?.readableFormat()



//        Toast.makeText(activity, fileLengthReadable, Toast.LENGTH_LONG).show()

        uploadTask.addOnFailureListener {
            Toast.makeText(activity, "File Upload to Storage Failed", Toast.LENGTH_SHORT).show()
            if (progressDialog.isShowing) progressDialog.dismiss()
        }.addOnSuccessListener{
            Toast.makeText(activity, "File Upload to Storage Success", Toast.LENGTH_SHORT).show()

            storage.downloadUrl.addOnSuccessListener {
                fileUrl = it.toString()
//                Toast.makeText(activity, "File location: " + fileUrl, Toast.LENGTH_SHORT).show()
            }

            gcodeFile = gcodeFileClass(fileName, fileUrl, fileNameNow.toString(), fileLengthReadable.toString())

            val counter: Int? = 0
            if (progressDialog.isShowing) progressDialog.dismiss()
                database.child(fileName!!).setValue(gcodeFile).addOnSuccessListener {
//    ======================================================================================================================================
                    checkFileUpload()
                    if (fileUrlDatabase != fileUrl) {
                        gcodeFile = gcodeFileClass(fileName,fileUrl.toString(),fileNameNow.toString(),fileLengthReadable.toString())
                        database.child(fileName!!).setValue(gcodeFile).addOnSuccessListener {
                            checkFileUpload()
                            if (fileUrlDatabase == fileUrl)
                                Toast.makeText(activity,"Successfully Saved to Database",Toast.LENGTH_SHORT).show();
                            else
                                gcodeFile = gcodeFileClass(fileName,fileUrl.toString(),fileNameNow.toString(),fileLengthReadable.toString())
                                database.child(fileName!!).setValue(gcodeFile).addOnSuccessListener {
                                    checkFileUpload()
                                    if (fileUrlDatabase == fileUrl)
                                        Toast.makeText(activity,"Successfully Saved to Database",Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(activity,"Hmm.. We've encountered an error. Please try uploading again.",Toast.LENGTH_LONG).show();
                                }.addOnFailureListener{
                                    Toast.makeText(activity, "Failed Saved to Database", Toast.LENGTH_SHORT).show();
                                }
                        }.addOnFailureListener{
                            Toast.makeText(activity, "Failed Saved to Database", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(activity,"Successfully Saved to Database",Toast.LENGTH_SHORT).show();
                    }
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
        getUserData()
    }


    private fun getUserData() {
        database = FirebaseDatabase.getInstance().getReference("Print Files")

        database.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (userSnapshot in snapshot.children){
                        val gcodefile = userSnapshot.getValue(gcodeFileClass::class.java)
                        userArrayList.add(gcodefile!!) //note double exclamation points will throw an exception on null value
                    }
//                    userRecyclerView.adapter = MyAdapter(userArrayList)
                    var adapter = MyAdapter(userArrayList)
                    userRecyclerView.adapter = adapter
                    adapter.setOnClickListener(object : MyAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            Toast.makeText(activity, "Clicked on File: " + userArrayList[position].name, Toast.LENGTH_SHORT).show();
                            val bundle = Bundle()
                            bundle.putString("fileName",userArrayList[position].name)
                            bundle.putString("url",userArrayList[position].url)
                            val fragment = Home()
                            fragment.arguments  = bundle
                            fragmentManager?.beginTransaction()?.replace(R.id.frame_layout,fragment)?.commit()
                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    fun Long.readableFormat(): String {
        if (this <= 0 ) return "0"
        val units = arrayOf("iB", "kiB", "MiB", "GiB", "TiB")
        val digitGroups = (log10(this.toDouble()) / log10(1024.00)).toInt()
        return DecimalFormat("#,##0.#").format(this / 1024.00.pow(digitGroups.toDouble())).toString() + " " + units[digitGroups]
    }


    private fun checkFileUpload() {
        databasePC = FirebaseDatabase.getInstance().getReference().child("Print Files").child(fileName!!)
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pc = snapshot.getValue<gcodeFileClass>()
                fileUrlDatabase = pc?.url.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "DID NOT FIND DATA", Toast.LENGTH_SHORT).show();
            }
        }
        databasePC.addListenerForSingleValueEvent(postListener)}
    }
