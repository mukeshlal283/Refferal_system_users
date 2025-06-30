package com.example.newapp.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.newapp.R
import com.example.newapp.adapter.PointsAdapter
import com.example.newapp.databinding.ActivityMainBinding
import com.example.newapp.model.NewUser
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: PointsAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        fetchUserData()

        binding.logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    private fun getCurrentUserEmail(): String {
        return auth.currentUser?.email.toString()
    }

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid.toString()
    }

    private fun fetchUserData() {
        binding.userEmail.text = getCurrentUserEmail()
        db.collection("users").document(getCurrentUserId()).addSnapshotListener {snapshot, error ->
            if (error != null) {
                Log.e("FIRESTORE", "Listen failed", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                binding.userEmail.text = snapshot.getString("email")
                binding.userName.text = snapshot.getString("name")
                binding.userId.text = snapshot.getString("id")
                binding.userPhoneNum.text = snapshot.getString("phoneNumber")
                binding.userPoints.text = snapshot.getLong("points").toString()
                val rawList = snapshot.get("newUserList") as List<HashMap<String, Any>>
                val newUserList = mutableListOf<NewUser>()
                rawList.forEach { map ->
                    val user = NewUser(
                        id = map["id"] as? String,
                        name = map["name"] as String,
                        points = map["points"] as Long
                    )
                    newUserList.add(user)
                }
                newUserList.reverse()
                adapter = PointsAdapter()
                binding.recyclerView.adapter = adapter
                adapter.differ.submitList(newUserList)
            }
        }

    }

}