package com.example.mappin_fe.HotPin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mappin_fe.Data.ApiService
import com.example.mappin_fe.Data.PinDataResponse
import com.example.mappin_fe.Data.UserAccount
import com.example.mappin_fe.R
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HotPinFragment : Fragment() {
    private lateinit var apiService: ApiService
    private lateinit var recyclerView: RecyclerView
    private lateinit var tagSpinner: Spinner
    private lateinit var pinAdapter: PinAdapter
    private var userTags: List<String> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hot_pin, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewHotPins)
        tagSpinner = view.findViewById(R.id.spinnerTags)

        setupApiService()
        setupRecyclerView()

        // Fetch user data and populate the UI
        fetchUserData()

        return view
    }

    private fun setupApiService() {
        val retrofit = Retrofit.Builder()
            .baseUrl("YOUR_API_BASE_URL")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    private fun setupRecyclerView() {
        pinAdapter = PinAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pinAdapter
        }
    }

    private fun fetchUserData() {
//        lifecycleScope.launch {
//            try {
//                // Assume we have a method to get the current user's data
//                val userAccount = apiService.getCurrentUser()
//                userTags = userAccount.tags ?: listOf()
//                setupTagSpinner()
//                fetchHotPins(userTags.firstOrNull())
//            } catch (e: Exception) {
//                // Handle error
//            }
//        }
    }

    private fun setupTagSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, userTags)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tagSpinner.adapter = adapter

        tagSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedTag = parent.getItemAtPosition(position) as String
                fetchHotPins(selectedTag)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchHotPins(tag: String?) {
//        lifecycleScope.launch {
//            try {
//                // Assume we have an API method to fetch hot pins by tag
//                val hotPins = apiService.getHotPinsByTag(tag)
//                pinAdapter.submitList(hotPins)
//            } catch (e: Exception) {
//                // Handle error
//            }
//        }
    }
}

class PinAdapter : RecyclerView.Adapter<PinAdapter.PinViewHolder>() {
    private var pins: List<PinDataResponse> = listOf()

    fun submitList(newPins: List<PinDataResponse>) {
        pins = newPins
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_hot_pin, parent, false)
        return PinViewHolder(view)
    }

    override fun onBindViewHolder(holder: PinViewHolder, position: Int) {
        holder.bind(pins[position])
    }

    override fun getItemCount() = pins.size

    class PinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind pin data to views
        fun bind(pin: PinDataResponse) {
            // TODO: Implement binding logic
        }
    }
}