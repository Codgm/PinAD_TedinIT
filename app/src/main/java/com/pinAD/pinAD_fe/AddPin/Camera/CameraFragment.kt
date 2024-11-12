package com.pinAD.pinAD_fe.AddPin.Camera

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.pinAD.pinAD_fe.AddPin.Category.CategorySelectionFragment
import com.pinAD.pinAD_fe.R
import com.pinAD.pinAD_fe.databinding.FragmentCameraBinding
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var isRecording = false

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var thumbnailAdapter: ThumbnailAdapter

    // 미디어 파일 경로를 저장할 리스트
    private val mediaFiles = mutableListOf<MediaFile>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupThumbnailRecyclerView()

        binding.thumbnailRecyclerView.apply {
            adapter = thumbnailAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        binding.captureButton.setOnClickListener { takePhoto() }
        binding.videoCaptureButton.setOnClickListener { captureVideo() }
        binding.nextButton.setOnClickListener { navigateToCategorySelection() }

        // 추가: 뒤로가기 버튼 클릭 리스너 설정
        binding.backButton.setOnClickListener { onBackToCaptureMode() }

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded!"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                    output.savedUri?.let { uri ->
                        saveMediaFile(uri, "image")
                        thumbnailAdapter.notifyDataSetChanged()  // Adapter     갱신
                    }

                }
            }
        )
    }

    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return

        // 녹화 버튼 활성화
        binding.videoCaptureButton.isEnabled = true

        val curRecording = recording
        if (curRecording != null) {
            // 녹화 중이면 중지
            curRecording.stop()
            recording = null
            isRecording = false
            binding.videoCaptureButton.setImageResource(android.R.drawable.presence_video_online)
            return
        }

        // 녹화 시작
        isRecording = true
        binding.videoCaptureButton.setImageResource(android.R.drawable.presence_video_busy)

        // 녹화 시작 메시지 표시
        Toast.makeText(requireContext(), "Video recording started", Toast.LENGTH_SHORT).show()

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(requireContext().contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        // 녹화 시작 및 설정
        recording = videoCapture.output
            .prepareRecording(requireContext(), mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.RECORD_AUDIO
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(requireContext())) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        // 아이콘과 메시지 업데이트
                        binding.videoCaptureButton.post {
                            binding.videoCaptureButton.setImageResource(android.R.drawable.presence_video_busy)
                        }

                        // 1분 타이머 시작
                        binding.videoCaptureButton.postDelayed({
                            if (isRecording) {
                                recording?.stop()
                                isRecording = false
                                binding.videoCaptureButton.setImageResource(android.R.drawable.presence_video_online)
                            }
                        }, 60000) // 60000ms = 1분
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded!"
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                            Log.d(TAG, msg)
                            saveMediaFile(recordEvent.outputResults.outputUri, "video")
                            thumbnailAdapter.notifyDataSetChanged()
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: ${recordEvent.error}")
                        }
                        isRecording = false
                        binding.videoCaptureButton.setImageResource(android.R.drawable.presence_video_online)
                    }
                }
            }
    }




    private fun setupThumbnailRecyclerView() {
        thumbnailAdapter = ThumbnailAdapter(
            mediaFiles = mediaFiles,
            onItemDeleted = { position ->
                // 삭제 후 필요한 처리
            },
            onItemClicked = { mediaFile ->
                // 전체화면 프리뷰 표시
                showPreviewScreen(mediaFile)
            }
        )

        binding.thumbnailRecyclerView.apply {
            adapter = thumbnailAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun showPreviewScreen(mediaFile: MediaFile) {
        binding.previewContainer.visibility = View.VISIBLE
        binding.viewFinder.visibility = View.GONE
        binding.controlsContainer.visibility = View.GONE

        // 미리보기 화면에서 촬영 버튼 숨기기
        when (mediaFile.type) {
            "image" -> {
                binding.capturedImageView.visibility = View.VISIBLE
                binding.capturedVideoView.visibility = View.GONE
                binding.capturedImageView.setImageURI(Uri.parse(mediaFile.uri))
            }
            "video" -> {
                binding.capturedImageView.visibility = View.GONE
                binding.capturedVideoView.visibility = View.VISIBLE
                binding.capturedVideoView.setVideoURI(Uri.parse(mediaFile.uri))
                binding.capturedVideoView.start()
            }
        }
    }

    private fun onBackToCaptureMode() {
        binding.previewContainer.visibility = View.GONE
        binding.viewFinder.visibility = View.VISIBLE
        binding.controlsContainer.visibility = View.VISIBLE
        binding.capturedVideoView.stopPlayback()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, videoCapture)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(requireContext(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
        }
    }

    private fun getRealPathFromUri(contentUri: Uri): String? {
        val cursor = requireContext().contentResolver.query(contentUri, null, null, null, null)
        return cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            it.moveToFirst()
            it.getString(columnIndex)
        }
    }

    private fun saveMediaFile(uri: Uri, type: String) {
        val mediaFilePath = getRealPathFromUri(uri)
        if (mediaFilePath != null) {
            mediaFiles.add(MediaFile(mediaFilePath, type))
            Log.d(TAG, "Saved media file: $mediaFilePath, Type: $type")
        } else {
            Log.e(TAG, "Failed to get file path from URI: $uri")
        }
    }


    private fun navigateToCategorySelection() {
        val mediaFilesJson = Gson().toJson(mediaFiles)
        val bundle = Bundle().apply {
            putString("MEDIA_FILES", mediaFilesJson)
        }
        val categorySelectionFragment = CategorySelectionFragment().apply {
            arguments = bundle
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, categorySelectionFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}

data class MediaFile(val uri: String, val type: String)