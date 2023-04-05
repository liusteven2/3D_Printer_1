package com.example.a3d_printer_1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import com.example.a3d_printer_1.databinding.ActivityMainBinding
import java.text.Normalizer

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding //make sure to check out viewbinding video

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Home())

//        val motionLayout = findViewById<androidx.constraintlayout.motion.widget.MotionLayout>(R.id.motionLayout)
//        motionLayout.transitionToEnd()

//        supportFragmentManager.beginTransaction().replace(R.id.frame_layout,Home()).commit()
        binding.bottomNavigationView.setOnItemSelectedListener {

            when(it.itemId) {

                R.id.home -> replaceFragment(Home())
                R.id.library -> replaceFragment(Library())
                R.id.formatting -> replaceFragment(Formatting())
//                R.id.settings -> replaceFragment(Settings())

                else -> {


                }
            }
            true
        }



    }

    private fun replaceFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()

    }

}