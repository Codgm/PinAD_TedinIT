package com.example.mappin_fe

import android.util.Log
import com.example.mappin_fe.Data.UserAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object UserUtils {

    private const val TAG = "UserUtils"

    fun fetchUserDetails(callback: (nickname: String, profilePicUrl: String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d(TAG, "Fetching user details for User ID: $userId")

        if (userId == null) {
            Log.w(TAG, "User ID is null, user might not be logged in")
            callback("Unknown", "")
            return
        }

        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("Users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userAccount = snapshot.getValue(UserAccount::class.java)
                Log.d(TAG, "Fetched UserAccount: $userAccount")

                if (userAccount != null) {
                    val nickname = userAccount.nickname ?: "Unknown"
                    val profilePicUrl = userAccount.profile_picture ?: ""
                    Log.d(TAG, "Fetched nickname: $nickname, profilePicUrl: $profilePicUrl")
                    callback(nickname, profilePicUrl)
                } else {
                    Log.w(TAG, "UserAccount is null")
                    callback("규민", "")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error fetching user details: ${error.message}")
                callback("Unknown", "")
            }
        })
    }
}