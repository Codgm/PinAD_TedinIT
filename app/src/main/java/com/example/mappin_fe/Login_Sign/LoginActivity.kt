package com.example.mappin_fe.Login_Sign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mappin_fe.Data.UserAccount
import com.example.mappin_fe.R
import com.example.mappin_fe.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.common.SignInButton
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
                    Log.d("AuthCode", "Auth Code: $authCode")
                    firebaseAuthWithGoogle(account.idToken!!)
                    if (authCode != null) {
                        getAccessToken(authCode)
                    } else {
                        Toast.makeText(this, "Auth Code를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
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
            signInWithKakao()
        }

        btnNaverSignIn.setOnClickListener {
            signInWithNaver()
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // 이메일/비밀번호 로그인 검증
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            checkUserSettings()
                        } else {
                            Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // access_token을 받기 위한 메서드
    private fun getAccessToken(authCode: String) {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", getString(R.string.default_web_client_id)) // web client ID 사용
//            .add("client_secret", getString(R.string.client_secret)) // client secret 추가 (구글 API 콘솔에서 확인)
            .add("redirect_uri", "") // 필요한 경우 redirect URI 설정
            .add("code", authCode)
            .build()

        val request = Request.Builder()
            .url("https://oauth2.googleapis.com/token")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AccessTokenError", "Failed to get access token: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (response.isSuccessful && responseData != null) {
                    try {
                        val json = JSONObject(responseData)
                        val accessToken = json.getString("access_token")
                        Log.d("AccessToken", "Access Token: $accessToken")

                        // 필요한 경우 access_token으로 추가 작업 가능
                    } catch (e: Exception) {
                        Log.e("AccessTokenError", "Failed to parse access token response: ${e.message}")
                    }
                } else {
                    Log.e("AccessTokenError", "Failed to get access token: ${response.message}")
                }
            }
        })
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val user = authResult.user
                if (user != null) {
                    val userAccount = UserAccount(
                        idToken = idToken,
                        emailId = user.email,
                        nickname = user.displayName
                    )
                    saveUserAccountToFirebase(user.uid, userAccount)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Google Authentication Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun signInWithKakao() {
        // 카카오톡으로 로그인 가능한지 확인하고, 가능한 경우 카카오톡으로 로그인 시도
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    handleKakaoError(error)
                } else if (token != null) {
                    fetchKakaoUserInfo(token.accessToken)
                }
            }
        } else {
            // 카카오톡이 없으면 카카오 계정으로 로그인
            UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                if (error != null) {
                    handleKakaoError(error)
                } else if (token != null) {
                    fetchKakaoUserInfo(token.accessToken)
                }
            }
        }
    }

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

    // 카카오 사용자 정보 가져오는 함수
    private fun fetchKakaoUserInfo(accessToken: String) {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Toast.makeText(this, "카카오 사용자 정보 가져오기 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            } else if (user != null) {
                val userId = user.id.toString()
                val userAccount = UserAccount(
                    idToken = accessToken,
                    emailId = user.kakaoAccount?.email,
                    nickname = user.kakaoAccount?.profile?.nickname
                )
                saveUserAccountToFirebase(userId, userAccount)
            }
        }
    }


    private fun signInWithNaver() {
        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val accessToken = NaverIdLoginSDK.getAccessToken()
                        val userInfo = fetchNaverUserInfo(accessToken.toString())
                        if (userInfo != null) {
                            val userId = userInfo.emailId ?: "naver_${System.currentTimeMillis()}"
                            saveUserAccountToFirebase(userId, userInfo)
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@LoginActivity, "Naver 사용자 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "네이버 로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Toast.makeText(this@LoginActivity, "errorCode: $errorCode, errorDesc: $errorDescription", Toast.LENGTH_SHORT).show()
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        NaverIdLoginSDK.authenticate(this, oauthLoginCallback)
    }


    private fun fetchNaverUserInfo(accessToken: String): UserAccount? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://openapi.naver.com/v1/nid/me")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                val jsonObject = JSONObject(responseBody)
                val responseObj = jsonObject.getJSONObject("response")

                // 사용자 정보 추출
                val email = responseObj.optString("email")
                val nickname = responseObj.optString("nickname")

                // UserAccount 객체 생성 및 반환
                UserAccount(
                    idToken = accessToken,
                    emailId = email,
                    nickname = nickname
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveUserAccountToFirebase(userId: String, userAccount: UserAccount) {
        val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        database.setValue(userAccount)
            .addOnSuccessListener {
                checkUserSettings() // 설정 확인 및 화면 전환
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save user account: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUserSettings() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(userId)

            database.get().addOnSuccessListener { dataSnapshot ->
                val userAccount = dataSnapshot.getValue(UserAccount::class.java)

                if (userAccount != null && userAccount.nickname != null && userAccount.interests != null) {
                    // 사용자 정보가 완전하면 MainActivity로 이동
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // 사용자 정보가 불완전하면 UserSettingsActivity로 이동
                    val intent = Intent(this, UserSettingsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to check user settings", Toast.LENGTH_SHORT).show()
            }
        }
    }
}