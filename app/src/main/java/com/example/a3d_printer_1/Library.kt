package com.example.a3d_printer_1

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a3d_printer_1.databinding.ActivityMainBinding
import com.example.a3d_printer_1.databinding.FragmentLibraryBinding
import com.google.firebase.database.*
import com.google.firebase.storage.BuildConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class Library : Fragment() {

//    //Uri for file in device
    private var fileUri: Uri? = null
    private var fileName: String? = null

    //creating recyclerview and receiving information from firebase
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userArrayList : ArrayList<User>

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
        val view = inflater.inflate(R.layout.fragment_library, container, false)
        val btn : Button = view.findViewById(R.id.submitBtn)
        val selectbtn : Button = view.findViewById(R.id.selectBtn)
        database = FirebaseDatabase.getInstance().getReference("Users")
        storage = FirebaseStorage.getInstance().reference.child("Gcode Files")

//        val getFile = registerForActivityResult(
//            ActivityResultContracts.GetContent(),
//            ActivityResultCallback {
//                UploadFiles(it)
//            })

        btn.setOnClickListener{
//            selectFiles();
            val editText : EditText = view.findViewById(R.id.submitEdittext)
            val input = editText.text.toString()
            val User = User(input)
            database.child(input).setValue(User).addOnSuccessListener {
                editText.text.clear()
                Toast.makeText(activity, "Successfully Saved",Toast.LENGTH_SHORT).show();
            }.addOnFailureListener {
                Toast.makeText(activity, "Failed Saved", Toast.LENGTH_SHORT).show();
            }
        }

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
        }
        return view
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

        userArrayList = arrayListOf<User>()
        getUserData()
    }


    private fun getUserData() {
        database = FirebaseDatabase.getInstance().getReference("Users")

        database.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (userSnapshot in snapshot.children){
                        val user = userSnapshot.getValue(User::class.java)
                        userArrayList.add(user!!) //note double exclamation points will throw an exception on null value
                    }
                    userRecyclerView.adapter = MyAdapter(userArrayList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

//    private fun launchPicker(){
//        val mimeTypes = mutableListOf("gcode/*")
//        val config = Config(BuildConfig.API_KEY)
//    }
}

//class UploadFiles(data: Uri) {
//
//}
