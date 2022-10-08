package com.tworoot2.shortsapp

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.tworoot2.shortsapp.DataClasses.VideoData
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat

class UploadVideos : AppCompatActivity() {
    lateinit var uploadBtn: Button
    lateinit var addTagBtn: Button
    lateinit var tagText: EditText
    lateinit var tagTextView: TextView
    lateinit var videoSelected: TextView
    lateinit var noVideoSelected: TextView
    lateinit var videoUriText: TextView
    lateinit var titleText: EditText
    lateinit var selectVideo: LinearLayout

    private var videoUri: Uri? = null
    private val REQUEST_VIDEO_CAPTURE = 101
    private val REQUEST_VIDEO_GALLERY = 102

    lateinit var tagArrayList: ArrayList<String>
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference
    lateinit var progressBar: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_videos)
        uploadBtn = findViewById(R.id.uploadBtn)
        addTagBtn = findViewById(R.id.addTagBtn)
        tagText = findViewById(R.id.tagText)
        tagTextView = findViewById(R.id.tagTextView)
        videoSelected = findViewById(R.id.videoSelected)
        noVideoSelected = findViewById(R.id.noVideoSelected)
        titleText = findViewById(R.id.titleText)
        videoUriText = findViewById(R.id.videoUriText)
        selectVideo = findViewById(R.id.selectVideo)
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        progressBar = ProgressDialog(this@UploadVideos)
        progressBar.setTitle("Uploading video to database")
        progressBar.setCancelable(false)

        selectVideo.setOnClickListener {
            showDialog()
        }

        tagArrayList = ArrayList()
        var i = 0
        addTagBtn.setOnClickListener {
            if (tagText.text == null || tagText.text.isEmpty()) {
                tagText.error = "Enter tags"
            } else {
                if (i == 0) {
                    tagTextView.text = tagText.text.toString()
                    i++
                } else {
                    tagTextView.text = tagTextView.text.toString() + ", " + tagText.text.toString()
                    i++
                }
                tagArrayList.add(tagText.text.toString())
            }
        }

        uploadBtn.setOnClickListener {
            if (titleText.text.toString() == "" || titleText.text.toString()
                    .isEmpty() || videoUri == null
            ) {
                if (titleText.text.toString() == "" || titleText.text.toString().isEmpty()) {
                    titleText.error = "Enter title"
                }
                if (videoUri == null) {
                    Toast.makeText(this, "Please selected the video", Toast.LENGTH_LONG).show()
                }
            } else {

                progressBar.show()
                val uploadRef: StorageReference =
                    storageReference.child("Videos")
                        .child(titleText.text.toString() + (0..100).random() + "tRT" + ".mp4")

                uploadRef.putFile(videoUri!!).addOnSuccessListener {
                    uploadRef.downloadUrl.addOnSuccessListener {
                        progressBar.setTitle("Uploading title & des....")
                        val mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
                        val mReference: DatabaseReference = mDatabase.getReference("Videos")

                        val videoDatas: VideoData =
                            VideoData(titleText.text.toString(), it.toString())

                        mReference.child(System.currentTimeMillis().toString()).setValue(videoDatas)
                            .addOnSuccessListener {

                                progressBar.dismiss()
                                Toast.makeText(this, "Added", Toast.LENGTH_LONG).show()

                                startActivity(Intent(this@UploadVideos,MainActivity::class.java))


                            }

                    }
                }

            }
        }

    }

    private fun showDialog() {
        val dialog = Dialog(this@UploadVideos)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.video_options_dialog)
        val gallery = dialog.findViewById(R.id.gallery) as Button
        val record = dialog.findViewById(R.id.record) as Button
        gallery.setOnClickListener {
            val intent: Intent =
                Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_VIDEO_GALLERY)
            dialog.dismiss()
        }
        record.setOnClickListener {
            dispatchTakeVideoIntent()
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun dispatchTakeVideoIntent() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
            takeVideoIntent.resolveActivity(packageManager)?.also {
                val videoFile: File? = try {
                    createVideoFile()
                } catch (ex: IOException) {

                    null
                }
                videoFile?.also {
                    videoUri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.provider",
                        it
                    )
                    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createVideoFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(java.util.Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return File.createTempFile(
            "Video_${timeStamp}_",
            ".mp4",
            storageDir
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Log.d("VideoPath", videoUri?.path.toString())
//            uploadImage()
            Toast.makeText(this, "" + videoUri?.path.toString(), Toast.LENGTH_LONG).show()
            videoUriText.text = videoUri?.path.toString()
            videoSelected.visibility = View.VISIBLE
            noVideoSelected.visibility = View.GONE
        }
        if (requestCode == REQUEST_VIDEO_GALLERY && resultCode == RESULT_OK) {
            if (data != null) {
                videoSelected.visibility = View.VISIBLE
                noVideoSelected.visibility = View.GONE
                videoUri = data.data
                videoUriText.text = videoUri?.path.toString()

            }
        }
    }
}