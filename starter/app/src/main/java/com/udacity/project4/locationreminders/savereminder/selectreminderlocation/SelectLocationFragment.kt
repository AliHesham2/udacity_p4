package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(),OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var maps: GoogleMap
    private lateinit var currentSelectedLatLng: LatLng
    private lateinit var currentSelectedLocationName:String
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

         fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        binding.saveLocation.setOnClickListener {
            if (this::currentSelectedLocationName.isInitialized && currentSelectedLocationName.isNotEmpty()){
                onLocationSelected()
            }else{
                _viewModel.showToast.value = "Please select Point of interest"
            }
        }


        return binding.root
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            maps = googleMap
            enableMyLocation()
            setMapStyle(maps)
            setMapPoiClick(googleMap)
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
             map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireActivity(), R.raw.my_map_style))
        } catch (e: Resources.NotFoundException) { _viewModel.showErrorMessage.value = "Can't find style. Error: "  }
    }

    private fun setupUserLocation(){
        try {
            val locationResult: Task<Location> = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        maps.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude), ZOOM))

                }
            }
        }
        }catch (e: SecurityException){ Log.e("Exception: %s", e.message, e)}

    }

    private fun setMapPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener {  poi ->
            map.clear()
            val poiMarker = map.addMarker(MarkerOptions().position(poi.latLng).title(poi.name))
            poiMarker.showInfoWindow()
            currentSelectedLatLng = poi.latLng
            currentSelectedLocationName = poi.name
        }
    }



    private fun onLocationSelected() {
        _viewModel.savePoiData(currentSelectedLatLng,currentSelectedLocationName)
        this.findNavController().popBackStack()
    }

    /* Permission methods */

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            maps.isMyLocationEnabled = true
            setupUserLocation()
        }
        else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }


    /* Menu methods */

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            maps.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            maps.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            maps.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            maps.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    companion object{
        const val ZOOM = 13F
        const val REQUEST_LOCATION_PERMISSION = 1010
    }


}
