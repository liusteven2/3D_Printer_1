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

    //creating recyclerview and receiving information from firebase
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userArrayList : ArrayList<gcodeFileClass>

    //for sending information to firebase database
    private lateinit var database : DatabaseReference
    private lateinit var storage : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val view2 = inflater.inflate(R.layout.fragment_home, container, false)
//        val xPosDelivered : TextView = view2.findViewById(R.id.xPosDelivered)
        val view = inflater.inflate(R.layout.fragment_library, container, false)
//        val btn : Button = view.findViewById(R.id.submitBtn)
        val selectbtn : Button = view.findViewById(R.id.selectBtn)
//        database = FirebaseDatabase.getInstance().getReference("Users")
        database = FirebaseDatabase.getInstance().getReference("Print Files")
//        storage = FirebaseStorage.getInstance().reference.child("Gcode Files")

//        val getFile = registerForActivityResult(
//            ActivityResultContracts.GetContent(),
//            ActivityResultCallback {
//                UploadFiles(it)
//            })

//        btn.setOnClickListener{
////            selectFiles();
//            val bundle = Bundle()
//            bundle.putString("data","working??")
//            val fragment = Home()
//            fragment.arguments  = bundle
//            fragmentManager?.beginTransaction()?.replace(R.id.frame_layout,fragment)?.commit()
////            val editText : EditText = view.findViewById(R.id.submitEdittext)
////            val input = editText.text.toString()
////            val User = User(input)
////            database.child(input).setValue(User).addOnSuccessListener {
////                editText.text.clear()
////                Toast.makeText(activity, "Successfully Saved",Toast.LENGTH_SHORT).show();
////            }.addOnFailureListener {
////                Toast.makeText(activity, "Failed Saved", Toast.LENGTH_SHORT).show();
////            }
//        }

        val getFile = registerForActivityResult(ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                fileUri = it!!
                val builder = AlertDialog.Builder(requireActivity())
                val editText_view = inflater.inflate(R.layout.edit_text_layout, container, false)
                val editText: EditText = editText_view.findViewById(R.id.et_editText)

                with(builder) {
                    setTitle("Enter some Text!")
                    setPositiveButton("OK"){dialog, which ->
                        fileName = editText.text.toString()
//                        val uploadFileRef: StorageReference = storage.child(fileName+it.getLastPathSegment())
//                        storage = FirebaseStorage.getInstance().getReference("")
//                        uploadFileRef.putFile(it).addOnSuccessListener {
//                            Toast.makeText(activity, "Successfully Uploaded",Toast.LENGTH_SHORT).show();
//                        }.addOnFailureListener{
//                            Toast.makeText(activity, "Failed Upload",Toast.LENGTH_SHORT).show();
//                        }
                        uploadFile()
//                        //uploading to firebase
//                        val fileToUploadName: StorageReference = storage.child(fileName!!)
//                        fileToUploadName.putFile(fileUri!!).addOnSuccessListener {
//                            fileToUploadName.downloadUrl.addOnSuccessListener { uri ->
//                                val databaseReference:DatabaseReference = FirebaseDatabase.getInstance()
//                                    .getReferenceFromUrl("https://d-printer-b61f0-default-rtdb.firebaseio.com").child("Print Files Database")
//                                val hashMap: HashMap<String, String> = HashMap()
//                                hashMap.put(fileName!!, uri.toString())
//                                databaseReference.setValue(hashMap)
//                                Toast.makeText(activity, "File Upload Completed", Toast.LENGTH_SHORT).show();
//                            }.addOnFailureListener{
//                                Toast.makeText(activity, "File Upload to Database Failed", Toast.LENGTH_SHORT).show();
//                            }
//                        }.addOnFailureListener{
//                            Toast.makeText(activity, "File Upload to Storage Failed", Toast.LENGTH_SHORT).show();
//                        }
                    }
                    setNegativeButton("Cancel"){dialog, which ->
                        Toast.makeText(activity, "Cancel File Upload", Toast.LENGTH_SHORT).show();
                    }
                    setView(editText_view)
                    show()
                }


            })
        selectbtn.setOnClickListener{
//            val fileInent = Intent(Intent.ACTION_GET_CONTENT)
//            startActivityForResult(fileInent, 222)
            getFile.launch("application/octet-stream")
//            xPosDelivered.setText("communicated")
        }
//        MyAdapter.onItemClick
        return view
    }

    private fun uploadFile() {
        val progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Uploading File...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val fileNameNow = formatter.format(now)
        storage = FirebaseStorage.getInstance().getReference("Print Files/"+fileName)
        uploadTask = storage.putFile(fileUri!!)
//        val fileUrl = uploadTask.result.toString()
        var pfd = requireActivity().contentResolver.openFileDescriptor(fileUri!!, "r")
        var fileLength: Long? = pfd!!.getStatSize()
        var fileLengthReadable = fileLength?.readableFormat()



        Toast.makeText(activity, fileLengthReadable, Toast.LENGTH_LONG).show()

        uploadTask.addOnFailureListener {
            Toast.makeText(activity, "File Upload to Storage Failed", Toast.LENGTH_SHORT).show()
            if (progressDialog.isShowing) progressDialog.dismiss()
        }.addOnSuccessListener{
            Toast.makeText(activity, "File Upload to Storage Success", Toast.LENGTH_SHORT).show()

            storage.downloadUrl.addOnSuccessListener {
                fileUrl = it.toString()
                Toast.makeText(activity, "File location: " + fileUrl, Toast.LENGTH_SHORT).show()
            }

            val gcodeFile = gcodeFileClass(fileName, fileUrl, fileNameNow.toString(), fileLengthReadable.toString())

            if (progressDialog.isShowing) progressDialog.dismiss()
                database.child(fileName!!).setValue(gcodeFile).addOnSuccessListener {
                    Toast.makeText(activity, "Successfully Saved to Database",Toast.LENGTH_SHORT).show();
                }.addOnFailureListener {
                    Toast.makeText(activity, "Failed Saved to Database", Toast.LENGTH_SHORT).show();
                }
        }
    }

//    private fun selectFiles() {
////        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//        val intent = Intent()
//        intent.type = "gcode/*"
//        intent.setAction(Intent.ACTION_GET_CONTENT)
//        startActivityForResult(Intent.createChooser(intent,"Select Gcode File..."),1)
////        intent.action = Intent.ACTION_GET_CONTENT
////        startActivityForResult(intent, 100)
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode==222 && resultCode== Activity.RESULT_OK && data!=null && data.getData()!=null) (
//                try {
//                    val fileUri: Uri = data?.data!!
//                }catch (e: IOException) {
//                    e.printStackTrace()
//                    Toast.makeText(activity, "Failed to pull file", Toast.LENGTH_SHORT).show()
//                })
//    }


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
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (log10(this.toDouble()) / log10(1024.00)).toInt()
        return "~"+DecimalFormat("#,##0.#").format(this / 1024.00.pow(digitGroups.toDouble())).toString() + " " + units[digitGroups]
    }

//    private fun launchPicker(){
//        val mimeTypes = mutableListOf("gcode/*")
//        val config = Config(BuildConfig.API_KEY)
//    }
}

//class UploadFiles(data: Uri) {
//
//}
