package com.pinAD.pinAD_fe.Profile.coupon

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.pinAD.pinAD_fe.R

class QrCodeDialogFragment(private val qrCodeBitmap: Bitmap) : DialogFragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_qr_code, container, false)

        val imageView = view.findViewById<ImageView>(R.id.qrCodeImageView)
        imageView.setImageBitmap(qrCodeBitmap)

        return view
    }

    // 레이아웃에 맞게 QR 코드를 표시하기 위한 다이얼로그 레이아웃을 정의합니다.
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_qr_code)
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }
}
