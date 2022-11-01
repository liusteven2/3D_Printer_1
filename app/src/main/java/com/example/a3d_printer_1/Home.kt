package com.example.a3d_printer_1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class Home : Fragment() {

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val btn : Button = view.findViewById(R.id.applyBtn)
        btn.setOnClickListener{
            val editText : EditText = view.findViewById(R.id.applyEdittext)
            val input = editText.text.toString()

            //below is for communicating to another fragment
//            val bundle = Bundle()
//            bundle.putString("data",input)
//            val fragment = Formatting()
//            fragment.arguments = bundle
//            fragmentManager?.beginTransaction()?.replace(R.id.frame_layout,fragment)?.commit()
            //above is for communicating to another fragment

            database = FirebaseDatabase.getInstance().getReference("Users")
            val User = User(input)
            database.child(input).setValue(User).addOnSuccessListener {
                editText.text.clear()
                Toast.makeText(activity, "Successfully Saved", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Toast.makeText(activity, "Failed Saved", Toast.LENGTH_SHORT).show()
            }
        }
        return view
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

