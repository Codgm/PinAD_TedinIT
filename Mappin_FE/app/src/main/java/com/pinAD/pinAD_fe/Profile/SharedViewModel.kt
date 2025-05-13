package com.pinAD.pinAD_fe.Profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// SharedViewModel.kt
class SharedViewModel : ViewModel() {
    // 비즈니스 계정 상태를 저장하는 변수
    private val _isBusinessUser = MutableLiveData<Boolean>()
    val isBusinessUser: LiveData<Boolean> = _isBusinessUser

    // 값 변경을 위한 메서드
    fun setBusinessUser(isBusiness: Boolean) {
        _isBusinessUser.value = isBusiness
    }
}
