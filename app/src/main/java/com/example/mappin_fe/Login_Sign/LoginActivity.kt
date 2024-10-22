package com.example.mappin_fe.Login_Sign

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mappin_fe.Data.LoginRequest
import com.example.mappin_fe.Data.RegisteredAccount
import com.example.mappin_fe.Data.RetrofitInstance
import com.example.mappin_fe.Data.UserAccount
import com.example.mappin_fe.R
import com.example.mappin_fe.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.oauth.view.NidOAuthLoginButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnGoogleSignIn: SignInButton
    private lateinit var btnNaverSignIn: NidOAuthLoginButton
    private lateinit var btnKakaoSignIn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_pwd)
        val btnLogin: Button = findViewById(R.id.btn_login)
        val btnRegister: Button = findViewById(R.id.btn_register)
        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in)
        btnKakaoSignIn = findViewById(R.id.btn_kakao_sign_in)
        btnNaverSignIn = findViewById(R.id.btn_naver_sign_in)

        // Google Sign-In 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestServerAuthCode(getString(R.string.default_web_client_id))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Activity Result API 설정
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.result
                    val authCode = account.serverAuthCode
                    val idToken = account.idToken
                    Log.d("idtoken", "$idToken")
                    Log.d("AuthCode", "Auth Code: $authCode")
                    sendIdTokenToServer(account.idToken!!)
                } catch (e: Exception) {
                    Toast.makeText(this, "Google Sign-In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }


        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        // 카카오 로그인
        btnKakaoSignIn.setOnClickListener {
//            signInWithKakao()
        }

        btnNaverSignIn.setOnClickListener {
//            signInWithNaver()
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // 이메일/비밀번호 로그인 검증
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                login(email, password)
            }
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(email: String, password: String) {
        // FCM 토큰 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcm_token = task.result // 비동기 호출로 토큰 가져오기

                // CoroutineScope를 사용하여 비동기 로그인 처리
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitInstance.api.loginown(RegisteredAccount(email, password, fcm_token!!)) // FCM 토큰 포함
                        withContext(Dispatchers.Main) {
                            when (response.code()) {
                                200 -> {
                                    // 로그인 성공
                                    Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@LoginActivity, UserSettingsActivity::class.java).apply {
                                    }
                                    startActivity(intent)
                                    finish()
                                }
                                400 -> {
                                    // 로그인 실패
                                    Toast.makeText(this@LoginActivity, "로그인 실패: 잘못된 이메일 또는 비밀번호", Toast.LENGTH_SHORT).show()
                                }
                                403 -> {
                                    // 계정 비활성화
                                    Toast.makeText(this@LoginActivity, "계정이 비활성화 상태입니다", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    // 기타 에러 처리
                                    Toast.makeText(this@LoginActivity, "오류 발생: ${response.code()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this@LoginActivity, "FCM 토큰을 가져오는 데 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // 로그인 시도 전 확인
    private fun signInWithGoogle() {
        try {
            Log.d("GoogleSignIn", "Starting Google Sign In")
            val signInIntent = googleSignInClient.signInIntent
            Log.d("GoogleSignIn", "Created sign in intent")
            googleSignInLauncher.launch(signInIntent)
            Log.d("GoogleSignIn", "Launched sign in intent")
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Error in signInWithGoogle", e)
        }
    }

    private fun sendIdTokenToServer(idToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // FCM 토큰 가져오기
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                Log.d("fcmToken", "$fcmToken")
                val loginRequest = LoginRequest(id_token = idToken, fcm_token = fcmToken)
                val response = RetrofitInstance.api.loginUser(loginRequest)

                withContext(Dispatchers.Main) {
                    when (response.code()) {
                        201 -> { // 로그인 성공
                            val loginResponse = response.body()
                            val accessToken = loginResponse?.access_token // 서버에서 발급한 access_token
                            val refreshToken = loginResponse?.refresh_token

                            if (accessToken != null) {
                                Log.d("AccessToken", "Access Token: $accessToken")
                                // access_token을 이용한 추가 작업 처리
                                RetrofitInstance.setTokens(accessToken, refreshToken)

                                // UserSettingActivity로 이동
                                val intent = Intent(this@LoginActivity, UserSettingsActivity::class.java).apply {
                                    putExtra("ACCESS_TOKEN", accessToken)
                                }
                                startActivity(intent)
                                finish() // 현재 액티비티 종료
                            } else {
                                Log.e("AccessTokenError", "Access Token is null")
                            }
                        }
                        200 -> { // 이미 로그인된 상태
                            val loginResponse = response.body()
                            val accessToken = loginResponse?.access_token
                            val refreshToken = loginResponse?.refresh_token
                            Log.d("LoginStatus", "User is already logged in.")
                            Log.d("AccessToken", "Access Token: $accessToken")
                            if (accessToken != null) {
                                RetrofitInstance.setTokens(accessToken, refreshToken)
                                checkUserSettings(accessToken)
                            } else {
                                Log.e("LoginError", "Access token is null when checking user settings")
                            }
                        }
                        else -> { // 다른 오류 처리
                            Log.e("LoginError", "Login failed: ${response.message()}")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LoginError", "Exception during login: ${e.message}")
                }
            }
        }
    }



//    private fun signInWithKakao() {
//        // 카카오톡으로 로그인 가능한지 확인하고, 가능한 경우 카카오톡으로 로그인 시도
//        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
//            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
//                if (error != null) {
//                    handleKakaoError(error)
//                } else if (token != null) {
//                    fetchKakaoUserInfo(token.accessToken)
//                }
//            }
//        } else {
//            // 카카오톡이 없으면 카카오 계정으로 로그인
//            UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
//                if (error != null) {
//                    handleKakaoError(error)
//                } else if (token != null) {
//                    fetchKakaoUserInfo(token.accessToken)
//                }
//            }
//        }
//    }

    // 카카오 로그인 에러 처리 함수
    private fun handleKakaoError(error: Throwable) {
        when {
            error is ClientError && error.reason == ClientErrorCause.Cancelled ->
                Toast.makeText(this, "카카오 로그인을 취소했습니다", Toast.LENGTH_SHORT).show()
            error is ClientError && error.reason == ClientErrorCause.NotSupported ->
                Toast.makeText(this, "카카오톡이 설치되어 있지 않습니다", Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(this, "카카오 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }

//    // 카카오 사용자 정보 가져오는 함수
//    private fun fetchKakaoUserInfo(accessToken: String) {
//        UserApiClient.instance.me { user, error ->
//            if (error != null) {
//                Toast.makeText(this, "카카오 사용자 정보 가져오기 실패: ${error.message}", Toast.LENGTH_SHORT).show()
//            } else if (user != null) {
//                val userId = user.id.toString()
//                val userAccount = UserAccount(
//                    idToken = accessToken,
//                    emailId = user.kakaoAccount?.email,
//                    nickname = user.kakaoAccount?.profile?.nickname
//                )
//                saveUserAccountToFirebase(userId, userAccount)
//            }
//        }
//    }


//    private fun signInWithNaver() {
//        val oauthLoginCallback = object : OAuthLoginCallback {
//            override fun onSuccess() {
//                CoroutineScope(Dispatchers.IO).launch {
//                    try {
//                        val accessToken = NaverIdLoginSDK.getAccessToken()
//                        val userInfo = fetchNaverUserInfo(accessToken.toString())
//                        if (userInfo != null) {
//                            val userId = userInfo.emailId ?: "naver_${System.currentTimeMillis()}"
//                            saveUserAccountToFirebase(userId, userInfo)
//                        } else {
//                            withContext(Dispatchers.Main) {
//                                Toast.makeText(this@LoginActivity, "Naver 사용자 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    } catch (e: Exception) {
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(this@LoginActivity, "네이버 로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//            }
//
//            override fun onFailure(httpStatus: Int, message: String) {
//                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
//                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
//                Toast.makeText(this@LoginActivity, "errorCode: $errorCode, errorDesc: $errorDescription", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onError(errorCode: Int, message: String) {
//                onFailure(errorCode, message)
//            }
//        }
//
//        NaverIdLoginSDK.authenticate(this, oauthLoginCallback)
//    }


//    private fun fetchNaverUserInfo(accessToken: String): UserAccount? {
//        val client = OkHttpClient()
//        val request = Request.Builder()
//            .url("https://openapi.naver.com/v1/nid/me")
//            .addHeader("Authorization", "Bearer $accessToken")
//            .build()
//
//        return try {
//            val response = client.newCall(request).execute()
//            val responseBody = response.body?.string()
//            if (response.isSuccessful && responseBody != null) {
//                val jsonObject = JSONObject(responseBody)
//                val responseObj = jsonObject.getJSONObject("response")
//
//                // 사용자 정보 추출
//                val email = responseObj.optString("email")
//                val nickname = responseObj.optString("nickname")
//
//                // UserAccount 객체 생성 및 반환
//                UserAccount(
//                    idToken = accessToken,
//                    emailId = email,
//                    nickname = nickname
//                )
//            } else {
//                null
//            }
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    private fun saveUserAccountToFirebase(userId: String, userAccount: UserAccount) {
//        val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
//        database.setValue(userAccount)
//            .addOnSuccessListener {
//                checkUserSettings() // 설정 확인 및 화면 전환
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Failed to save user account: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }

    private fun checkUserSettings(accessToken: String?) {
        if (accessToken == null) {
            Log.e("CheckUserSettingsError", "Access Token is null")
            return
        }
        val sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE)
        val isSettingsCompleted = sharedPreferences.getBoolean("isSettingsCompleted", false)

        if (isSettingsCompleted) {
            // 설정이 완료된 경우 MainActivity로 이동
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // 설정이 완료되지 않은 경우 UserSettingsActivity로 이동
            val intent = Intent(this@LoginActivity, UserSettingsActivity::class.java).apply {
                putExtra("ACCESS_TOKEN", accessToken)
            }
            startActivity(intent)
            finish()
        }
    }
}