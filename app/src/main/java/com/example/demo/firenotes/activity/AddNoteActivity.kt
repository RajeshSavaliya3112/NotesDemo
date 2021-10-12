package com.example.demo.firenotes.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.ViewModelProvider
import com.example.demo.firenotes.databinding.ActivityAddEditNoteBinding
import com.example.demo.firenotes.model.Resource
import com.example.demo.firenotes.utils.Extension.snackbar
import com.example.demo.firenotes.utils.Loading
import com.example.demo.firenotes.viewmodel.NotesViewModel
import com.google.firebase.Timestamp
import java.io.IOException

class AddNoteActivity : AppCompatActivity() {

    private lateinit var addEditNoteBinding: ActivityAddEditNoteBinding
    private lateinit var userId: String
    private lateinit var mNotesViewModel: NotesViewModel
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addEditNoteBinding = ActivityAddEditNoteBinding.inflate(layoutInflater)
        setContentView(addEditNoteBinding.root)

        title = "Add Note"

        userId = intent.getStringExtra("uid")!!

        initViewModel()

        initListener()

    }

    private fun initViewModel() {
        mNotesViewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
    }

    private fun initListener() {

        addEditNoteBinding.btnSave.setOnClickListener {

            val noteTitle = addEditNoteBinding.etTitle.text.toString()
            val noteContent = addEditNoteBinding.etContent.text.toString()
            val currentTimeStamp = Timestamp.now()

            if(noteTitle.isNotEmpty() && noteContent.isNotEmpty()){
                saveNoteToFirebase(noteTitle,noteContent,currentTimeStamp)
            }else{
                snackbar("Field cannot be empty")
            }
        }

        addEditNoteBinding.addImage.setOnClickListener {
            launchGallery()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                addEditNoteBinding.imageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun saveNoteToFirebase(
        noteTitle: String,
        noteContent: String,
        currentTimeStamp: Timestamp
    ) {
        mNotesViewModel.addNote(userId,noteTitle,noteContent,currentTimeStamp,filePath)?.observe(this,{ resource ->
            when(resource){
                is Resource.Loading -> {
                    Loading.displayLoadingWithText(this,false)
                }
                is Resource.Success -> {
                    Loading.hideLoading()
                    onBackPressed()
                }
                is Resource.Error -> {
                    snackbar("${resource.message}")
                }
            }
        })
    }

    private fun launchGallery(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }
}