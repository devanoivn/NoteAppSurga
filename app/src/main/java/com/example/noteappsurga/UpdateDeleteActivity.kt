package com.example.noteappsurga

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.noteappsurga.databinding.ActivityUpdateDeleteBinding

class UpdateDeleteActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var noteId: String
    private lateinit var binding: ActivityUpdateDeleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateDeleteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().getReference("notes")

        // Get note ID from intent
        noteId = intent.getStringExtra("noteId") ?: ""

        // Check if noteId is not empty
        if (noteId.isNotEmpty()) {
            // Set click listener for Update button
            binding.updateButton.setOnClickListener {
                val title = binding.titleEditText.text.toString()
                val content = binding.contentEditText.text.toString()

                updateNoteTitleAndContent(title, content)
            }

            // Set click listener for Delete button
            binding.deleteButton.setOnClickListener {
                deleteNote()
            }

            // Load note data
            loadNoteData()
        } else {
            Toast.makeText(this@UpdateDeleteActivity, "Invalid note ID.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadNoteData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            database.child("notes").child(userId).child(noteId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val note = snapshot.getValue(Note::class.java)

                    if (note != null) {
                        binding.titleEditText.setText(note.title)
                        binding.contentEditText.setText(note.content)
                    } else {
                        // Catatan tidak ditemukan, tangani kasus ini
                        Toast.makeText(this@UpdateDeleteActivity, "Note not found.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read note data, handle error
                    Toast.makeText(this@UpdateDeleteActivity, "Failed to load note data.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
        } else {
            // User ID tidak tersedia, tangani kasus ini
            Toast.makeText(this, "User ID not available.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateNoteTitleAndContent(title: String, content: String) {
        val noteUpdates = hashMapOf<String, Any>(
            "title" to title,
            "content" to content
        )

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            database.child("notes").child(userId).child(noteId).updateChildren(noteUpdates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Note updated.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update note.", Toast.LENGTH_SHORT).show()
                }
        } else {
            // User ID tidak tersedia, tangani kasus ini
            Toast.makeText(this, "User ID not available.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun deleteNote() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            database.child("notes").child(userId).child(noteId).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Note deleted.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete note.", Toast.LENGTH_SHORT).show()
                }
        } else {
            // User ID tidak tersedia, tangani kasus ini
            Toast.makeText(this, "User ID not available.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

