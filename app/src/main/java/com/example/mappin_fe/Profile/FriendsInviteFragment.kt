package com.example.mappin_fe.Profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.mappin_fe.R

class FriendsInviteFragment : Fragment() {

    private lateinit var contactsList: List<String> // 폰 주소록에서 가져온 연락처 리스트
    private lateinit var inviteButton: Button // 초대 버튼

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends_invite, container, false)

        inviteButton = view.findViewById(R.id.btnInvite)
        inviteButton.setOnClickListener {
            inviteFriends()
        }

        // 폰 주소록에서 연락처를 가져오는 함수 호출
        loadContacts()

        return view
    }

    // 폰 주소록에서 연락처를 불러오는 함수
    private fun loadContacts() {
        // 권한 체크 및 주소록 접근 로직 추가
        contactsList = getContactsFromPhone() // 이 함수는 주소록을 읽어오는 메서드
        // 연락처 리스트를 UI에 표시하는 로직 추가
    }

    // 초대 기능
    private fun inviteFriends() {
        // 선택된 연락처로 앱 다운로드 링크를 발송하는 로직 추가
        sendInviteToContacts(contactsList) // 예시 함수
        // 초대 성공 시 포인트 지급 로직
        givePointsForInvitation()
    }

    private fun givePointsForInvitation() {
//        // 서버에 초대 성공 정보를 전송하고 포인트를 받는 로직
//        val userId = "현재 로그인된 사용자 ID"
//        val inviteData = mapOf("userId" to userId, "invitedContacts" to contactsList)
//
//        // 서버로 전송 후 성공 시 포인트 지급
//        serverApi.giveInvitePoints(inviteData).enqueue(object : Callback<ApiResponse> {
//            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
//                if (response.isSuccessful) {
//                    val points = response.body()?.points ?: 0
//                    Toast.makeText(context, "포인트 $points 지급 완료!", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
//                Toast.makeText(context, "포인트 지급 실패", Toast.LENGTH_SHORT).show()
//            }
//        })
    }


    // 연락처에서 가져온 정보를 기반으로 초대 링크 전송
    private fun sendInviteToContacts(contacts: List<String>) {
//        val downloadLink = "https://app.download.link"
//        for (contact in contacts) {
//            val smsManager = SmsManager.getDefault()
//            smsManager.sendTextMessage(contact, null, "앱을 다운로드하세요: $downloadLink", null, null)
//        }
    }

    // 예시로 폰에서 연락처를 가져오는 함수 (실제 구현 필요)
    private fun getContactsFromPhone(): List<String> {
        // 실제 연락처를 불러오는 로직
        return listOf("010-1234-5678", "010-9876-5432")
    }
}
