package com.tworoot2.shortsapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.database.*
import com.tworoot2.shortsapp.AdapterClasses.VideoAdapter
import com.tworoot2.shortsapp.DataClasses.VideoData

class MainActivity : AppCompatActivity() {

    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    private lateinit var arrayList: ArrayList<VideoData>
    lateinit var viewPager: ViewPager2
    lateinit var uploadVideoBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        viewPager = findViewById(R.id.viewPager)
        uploadVideoBtn = findViewById(R.id.uploadVideoBtn)

        uploadVideoBtn.setOnClickListener{
            startActivity(Intent(this@MainActivity,UploadVideos::class.java))
        }

        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Videos")
        arrayList = ArrayList()

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    for (data in snapshot.children) {

                        arrayList.add(
                            VideoData(
                                data.child("title").getValue(String::class.java).toString(),
                                data.child("url").getValue(String::class.java).toString()
                            )
                        )


                    }
                    val adapter = VideoAdapter(this@MainActivity, arrayList)
                    viewPager.adapter = adapter

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }
}