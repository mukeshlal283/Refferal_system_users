package com.example.newapp.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.newapp.R
import com.example.newapp.databinding.FragmentRegisterBinding
import com.example.newapp.model.NewUser
import com.example.newapp.model.Users
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private var auth = Firebase.auth
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRegisterBinding.inflate(layoutInflater)

        binding.alreadyHaveAccountBtn.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.registerBtn.setOnClickListener {

            val name = binding.userName.text.toString()
            val phoneNumber = binding.userPhoneNum.text.toString()
            val email = binding.userEmail.text.toString()
            val password = binding.userPassword.text.toString()
            val referralCode = binding.userRefCode.text.toString()

            if (name.isNotEmpty() && phoneNumber.isNotEmpty() && email.isNotEmpty()
                && password.isNotEmpty() && referralCode.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUserId = auth.currentUser!!.uid
                        val userData = Users(currentUserId, name, phoneNumber, email, password, "", 20, arrayListOf(), arrayListOf(NewUser(null, "New Register", 20)))
                        firestore.collection("users").document(currentUserId).set(userData).addOnSuccessListener {
                            getReferrerUserId(referralCode, currentUserId, name)
                        }
                        Toast.makeText(requireContext(), "Registered Successfully!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    }
                } .addOnFailureListener {
                    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }

        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun getReferrerUserId(referralCode: String, currentUserId: String, currentName: String) {
        firestore.collection("users").get().addOnSuccessListener { snapShot ->
            for (document in snapShot.documents) {
                val users = document.toObject(Users::class.java)
                if (users!!.referralCode == referralCode) {
                    if (users.referrerUsersList.size == 9) {
                        users.referrerUsersList.removeAt(0)
                        users.referrerUsersList.add(users.id)
                    }
                    updateReferrerUsersList(users.referrerUsersList, currentUserId, currentName)
                    break
                }
            }
        }
    }

    private fun updateReferrerUsersList(
        updatedReferrerUserList: ArrayList<String>,
        currentUserId: String,
        currentName: String
    ) {
        firestore.collection("users").document(currentUserId).update(
            "referrerUsersList", updatedReferrerUserList,
            "referralCode", getReferralCode()
        ).addOnSuccessListener {
            updatePoints(currentUserId, currentName)
        }
    }

    private fun updatePoints(currentUserId: String, currentName: String) {
        firestore.collection("users").document(currentUserId).get().addOnSuccessListener { document ->
            val referrerList = document.get("referrerUsersList") as ArrayList<String>
            for (i in 8 downTo 0) {
                val instance = firestore.collection("users").document(referrerList[i])
                when (i) {
                    8 -> { instance.get().addOnSuccessListener {
                        val newUserList = it.get("newUserList") as ArrayList<NewUser>
                        newUserList.add(NewUser(currentUserId, currentName, 10))
                        instance.update("points", FieldValue.increment(10),
                            "newUserList", newUserList)
                    } }
                    7 -> { instance.get().addOnSuccessListener {
                        val newUserList = it.get("newUserList") as ArrayList<NewUser>
                        newUserList.add(NewUser(currentUserId, currentName, 5))
                        instance.update("points", FieldValue.increment(5),
                            "newUserList", newUserList)
                    } }
                    6 -> { instance.get().addOnSuccessListener {
                        val newUserList = it.get("newUserList") as ArrayList<NewUser>
                        newUserList.add(NewUser(currentUserId, currentName, 4))
                        instance.update("points", FieldValue.increment(4),
                            "newUserList", newUserList)
                    } }
                    5, 4 -> { instance.get().addOnSuccessListener {
                        val newUserList = it.get("newUserList") as ArrayList<NewUser>
                        newUserList.add(NewUser(currentUserId, currentName, 3))
                        instance.update("points", FieldValue.increment(3),
                            "newUserList", newUserList)
                    } }
                    else -> { instance.get().addOnSuccessListener {
                        val newUserList = it.get("newUserList") as ArrayList<NewUser>
                        newUserList.add(NewUser(currentUserId, currentName, 1))
                        instance.update("points", FieldValue.increment(1),
                            "newUserList", newUserList)
                    } }
                }
            }
        }
    }

    private fun getReferralCode(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1 .. 6).map { allowedChars.random() }.joinToString("")
    }

}