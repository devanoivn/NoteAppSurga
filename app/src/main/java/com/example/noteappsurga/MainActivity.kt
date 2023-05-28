package com.example.noteappsurga

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteappsurga.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var userId: String
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().getReference("notes")

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if user is logged in
        val user = auth.currentUser
        if (user == null) {
            // User not logged in, redirect to login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // User logged in, proceed with initialization
            userId = user.uid

            // Initialize RecyclerView
            binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)
            noteAdapter = NoteAdapter(this)
            binding.notesRecyclerView.adapter = noteAdapter

            // Call readNotes to fill RecyclerView with notes
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                readNotes(userId)
            }

            // Set click listener for Add Notes button
            binding.addNotesButton.setOnClickListener {
                val intent = Intent(this, AddNotesActivity::class.java)
                startActivity(intent)
            }

            binding.logoutButton.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            binding.profileButton.setOnClickListener {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun openUpdateDeleteActivity(note: Note) {
        val intent = Intent(this, UpdateDeleteActivity::class.java)
        intent.putExtra("noteId", note.id)
        startActivity(intent)
    }

    private fun readNotes(userId: String) {
        database.child("notes").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = snapshot.children.mapNotNull { it.getValue(Note::class.java) }

                val sortedNotes = notes.sortedByDescending { it.priority }

                // Update RecyclerView with the list of notes
                noteAdapter.submitList(sortedNotes)

                // Check if there are no notes
                if (sortedNotes.isEmpty()) {
                    binding.addNotesButton.visibility = View.VISIBLE
                } else {
                    binding.addNotesButton.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read notes, handle error
                Toast.makeText(this@MainActivity, "Failed to read notes: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Failed to read notes", error.toException())
            }
        })
    }

    private fun updateNotePriority(noteId: String, newPriority: Int) {
        val noteRef = database.child(userId).child(noteId)
        noteRef.child("priority").setValue(newPriority)
            .addOnSuccessListener {
                // Prioritas catatan berhasil diperbarui
                Toast.makeText(this, "Note priority updated.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Gagal memperbarui prioritas catatan, tangani error
                Toast.makeText(this, "Failed to update note priority.", Toast.LENGTH_SHORT).show()
            }
    }

}