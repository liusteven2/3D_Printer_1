package com.example.a3d_printer_1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a3d_printer_1.databinding.ActivityMainBinding
import com.example.a3d_printer_1.databinding.FragmentLibraryBinding
import com.google.firebase.database.*

class Library : Fragment() {

    //creating recyclerview and receiving information from firebase
    private lateinit var dbref : DatabaseReference
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userArrayList : ArrayList<User>

    //for sending information to firebase database
    private lateinit var binding : ActivityMainBinding
    private lateinit var database : DatabaseReference
//    private var fragmentLibraryBinding : FragmentLibraryBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
////        // Inflate the layout for this fragment
////        return inflater.inflate(R.layout.fragment_library, container, false)
//
//
//        //for sending information to firebase database
////        binding = ActivityMainBinding.inflate(LayoutInflater)
////        binding.submitBtn.seton
//        val view: View = inflater!!.inflate(R.layout.fragment_library, container, false)
//        submitBtn.setOnClickListener {
//
//        }
//        binding.submitBtn.onClick {
//            showToast("hello binding!")
//        }
//        return view
        val view = inflater.inflate(R.layout.fragment_home, container, false)
//        val btn : Button = view.findViewById(R.id.submitBtn)
//        btn.setOnClickListener{
//            val editText : EditText = view.findViewById(R.id.ediText)
//            val input = editText.text.toString()
////            database = FirebaseDatabase.getInstance().getReference("Users")
////            val User = User(input)
////            database.child(input).setValue(User).addOnSuccessListener {
////                Toast.makeText(activity, "Successfully Saved",Toast.LENGTH_SHORT).show();
////            }.addOnFailureListener {
////                Toast.makeText(activity, "Failed Saved", Toast.LENGTH_SHORT).show();
////            }
//        }
        return view
    }



//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
////        //for receiving firebase data into recyclerview
////        val layoutManager = LinearLayoutManager(context)
////
////        userRecyclerView = view.findViewById(R.id.recycler_view)
////        userRecyclerView.layoutManager = layoutManager
////        userRecyclerView.setHasFixedSize(true)
////
////        userArrayList = arrayListOf<User>()
//////        getUserData()
//    }


    private fun getUserData() {
        dbref = FirebaseDatabase.getInstance().getReference("Users")

        dbref.addValueEventListener(object : ValueEventListener{
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
}