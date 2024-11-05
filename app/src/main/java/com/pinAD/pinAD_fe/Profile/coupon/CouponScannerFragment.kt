// CouponScannerFragment.kt
package com.pinAD.pinAD_fe.Profile.coupon

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.budiyev.android.codescanner.*
import com.pinAD.pinAD_fe.Data.coupon.CouponVerifyRequest
import com.pinAD.pinAD_fe.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.pinAD.pinAD_fe.R

class CouponScannerFragment : Fragment() {
    private lateinit var codeScanner: CodeScanner
    private val CAMERA_PERMISSION_REQUEST = 123

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_coupon_scanner, container, false)

        if (checkCameraPermission()) {
            setupScanner(view)
        } else {
            requestCameraPermission()
        }

        return view
    }

    private fun setupScanner(view: View) {
        val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(requireContext(), scannerView)

        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback { result ->
                activity?.runOnUiThread {
                    verifyCoupon(result.text)
                }
            }

            errorCallback = ErrorCallback {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(),
                        "Scanner error: ${it.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun verifyCoupon(couponCode: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.verifyCoupon(CouponVerifyRequest(couponCode))
                activity?.runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Coupon verified successfully!",
                            Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(context, "Invalid coupon code",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }

    override fun onResume() {
        super.onResume()
        if (::codeScanner.isInitialized) {
            codeScanner.startPreview()
        }
    }

    override fun onPause() {
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
        super.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupScanner(requireView())
                } else {
                    Toast.makeText(context,
                        "Camera permission is required to scan QR codes",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}