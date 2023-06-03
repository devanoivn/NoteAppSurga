package com.example.noteappsurga

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.noteappsurga.databinding.ActivityAddNotesBinding

class AddNotesActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var userId: String
    private lateinit var binding: ActivityAddNotesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().getReference("notes")

        // Get current user ID
        val user = FirebaseAuth.getInstance().currentUser
        userId = user?.uid ?: ""

        binding.saveButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val content = binding.contentEditText.text.toString()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                createNote(title, content)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createNote(title: String, content: String) {
        val id = database.child("notes").push().key
        val note = Note(id, title, content)

        if (id != null) {
            database.child("notes").child(userId).child(id).setValue(note)
                .addOnSuccessListener {
                    // Note created successfully
                    Toast.makeText(this, "Note created.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { exception ->
                    // Failed to create note, handle error
                    val errorMessage = exception.message
                    Toast.makeText(this, "Failed to create note: $errorMessage", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Failed to generate note ID.", Toast.LENGTH_SHORT).show()
        }
    }

}
