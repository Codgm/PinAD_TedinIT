package com.example.mappin_fe.Profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mappin_fe.Data.CouponResponse
import com.example.mappin_fe.Data.RetrofitInstance
import com.example.mappin_fe.R
import com.navercorp.nid.oauth.NidOAuthPreferencesManager.accessToken
import kotlinx.coroutines.launch
import retrofit2.Response

//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.*

class ProfileFragment : Fragment() {

    private lateinit var imgProfilePicture: ImageView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserNickname: TextView
    private lateinit var tvUserDescription: TextView
    private lateinit var tvPoints: TextView
    private lateinit var btnChargePoints: Button
    private lateinit var btnSettings: Button
    private lateinit var tvInterests: TextView
    private lateinit var btnCouponBox: Button


    //    private lateinit var firebaseAuth: FirebaseAuth
//    private lateinit var databaseReference: DatabaseReference
    private lateinit var currentUserUid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // 뷰 초기화
        imgProfilePicture = view.findViewById(R.id.imgProfilePicture)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        tvUserNickname = view.findViewById(R.id.tvUserNickname)
        tvUserDescription = view.findViewById(R.id.tvUserDescription)
        tvPoints = view.findViewById(R.id.tvPoints)
        btnChargePoints = view.findViewById(R.id.btnChargePoints)
        btnSettings = view.findViewById(R.id.btnSettings)
        tvInterests = view.findViewById(R.id.tvInterests)
        btnCouponBox = view.findViewById(R.id.btnCouponBox)

        btnCouponBox.setOnClickListener {
            showCouponDialog()
        }


        // Firebase 초기화
//        firebaseAuth = FirebaseAuth.getInstance()
//        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
//        currentUserUid = firebaseAuth.currentUser?.uid ?: ""

        // 프로필 데이터 로드
        loadUserProfile()

        // 설정 버튼 클릭 리스너
        btnSettings.setOnClickListener {
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.main_body_container, SettingFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // 포인트 충전 버튼 클릭
        btnChargePoints.setOnClickListener {
            // 포인트 충전 로직 구현 필요
            // 예: 충전 페이지로 이동
        }

        return view
    }

    private fun showCouponDialog() {
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.main_body_container, CouponDialogFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun loadUserProfile() {
//        databaseReference.child(currentUserUid).addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val userAccount = snapshot.getValue(UserAccount::class.java)
//                userAccount?.let {
//                    tvUserEmail.text = userAccount.emailId ?: "No email provided"
//                    tvUserNickname.text = userAccount.nickname ?: "No nickname provided"
//                    tvUserDescription.text = "Introduce yourself..."  // 사용자 소개는 필요에 따라 변경 가능
//                    tvInterests.text = userAccount.interests ?: "No interests specified"
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Error handling
//                Toast.makeText(context, "Error loading user data: ${error.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
    }
}
