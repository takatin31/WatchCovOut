package com.example.watchcovout_app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.fragment_map.*
import java.net.URISyntaxException


class MapFragment : Fragment(), PermissionsListener {

    private var mapView: MapView? = null
    private lateinit var mapboxMap: MapboxMap
    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private lateinit var mContext: Context
    var detached : Boolean = true
    val listPlaces = arrayListOf<Place>()
    val mapPlaces = hashMapOf<String, Place>()
    var features = arrayListOf<Feature>()
    private val PROPERTY_SELECTED = "selected"
    private val LAYER_ID = "places"
    var isProvider : Boolean = false
    var selectedPlace : LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Mapbox.getInstance(activity!!, getString(R.string.mapbox_access_token))

        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = mapBoxView
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {

                it.addImage(("marker_icon"), BitmapFactory.decodeResource(
                    resources, R.drawable.red_marker));

                // Custom map style has been loaded and map is now ready
                Log.i("Succes", "Map loaded Succefully")
                enableLocationComponent(it)

                getPlaces(it)
            }

            mapboxMap.addOnMapClickListener{
                Log.i("featurePnt", it.toString())
                val exist = handleClickIcon(mapboxMap.projection.toScreenLocation(it))
                if (!exist && isProvider){
                    selectedPlace = it
                    (activity as ServiceActivity).selectedPlace = it
                    addMarker(mapboxMap.style!!, it)
                }
                true
            }
        }
    }

    fun getPlaces(style: Style){
        val urlData = "${resources.getString(R.string.host)}/api/v0/places/"

        // Request a string response from the provided URL.
        val jsonRequestData = JsonArrayRequest(
            Request.Method.GET, urlData, null,
            Response.Listener { response ->

                if (response.length() == 0){
                    Toast.makeText(mContext, "There is no places yet", Toast.LENGTH_SHORT).show()
                }else{
                    listPlaces.clear()
                    for (i in 0 until response.length()){
                        val item = response.getJSONObject(i)
                        val idPlace = item.getString("id")
                        val titlePlace = item.getString("title")
                        val nbrPlaces = item.getInt("numberOfPlaces")
                        val lat = item.getJSONObject("location").getDouble("latitude")
                        val lng = item.getJSONObject("location").getDouble("longitude")
                        val latLng = LatLng(lat, lng)
                        val type = item.getString("type")
                        var chunk = 30

                        if (item.has("cunksOfTime")){
                            chunk = item.getInt("cunksOfTime")
                        }


                        val place = Place(idPlace, titlePlace, latLng, chunk, nbrPlaces, type)
                        listPlaces.add(place)
                        mapPlaces.put(idPlace, place)

                        val geometry = Point.fromLngLat(latLng.longitude, latLng.latitude)
                        val feature : Feature = Feature.fromGeometry(geometry)
                        feature.addStringProperty("title", titlePlace)
                        feature.addStringProperty("id", idPlace)
                        features.add(feature)
                    }

                    addMarkers(style)
                }
            },
            Response.ErrorListener { Log.d("Error", "Request error") })

        RequestHandler.getInstance(mContext).addToRequestQueue(jsonRequestData)
    }

    fun addMarkers(style : Style){


        val features = FeatureCollection.fromFeatures(features)

        Log.i("laaayer", features.toString())
        try {
            style.addSource(
                GeoJsonSource(
                    LAYER_ID,
                    features
                )
            )
        } catch (uriSyntaxException: URISyntaxException) {
            Log.i("Check the URL %s", uriSyntaxException.message)
        }

        style.addLayer(
            SymbolLayer(LAYER_ID, LAYER_ID)
                .withProperties(
                    PropertyFactory.iconImage("marker_icon"),
                    iconAllowOverlap(true),
                    iconOffset(arrayOf(0f, -8f))
                )
        )

    }

    fun addMarker(style: Style, pos : LatLng){

        style.removeLayer("new")
        style.removeSource("new")

        val geometry = Point.fromLngLat(pos.longitude, pos.latitude)
        val feature : Feature = Feature.fromGeometry(geometry)
        val newFeatures = arrayListOf<Feature>()
        newFeatures.add(feature)

        val features = FeatureCollection.fromFeatures(newFeatures)

        Log.i("laaayer", features.toString())
        try {
            style.addSource(
                GeoJsonSource(
                    "new",
                    features
                )
            )
        } catch (uriSyntaxException: URISyntaxException) {
            Log.i("Check the URL %s", uriSyntaxException.message)
        }

        style.addLayer(
            SymbolLayer("new", "new")
                .withProperties(
                    PropertyFactory.iconImage("marker_icon"),
                    iconAllowOverlap(true),
                    iconOffset(arrayOf(0f, -8f))
                )
        )
    }




    private fun handleClickIcon(screenPoint: PointF): Boolean {
        val features: List<Feature> =
            mapboxMap.queryRenderedFeatures(screenPoint, LAYER_ID)
        Log.i("feature", features.toString())
        return if (features.isNotEmpty()) {
            // Show the Feature in the TextView to show that the icon is based on the ICON_PROPERTY key/value
            placeName.text = features[0].getStringProperty("title")
            val place = mapPlaces.get(features[0].getStringProperty("id"))
            if (!isProvider){
                (activity as HomeActivity).chosenPlace = place
            }
            true
        } else {
            false
        }
    }


    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(context)) {

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(context!!)
                .trackingGesturesManagement(true)
                .accuracyColor(
                    ContextCompat.getColor(context!!,
                        R.color.blue_facebook
                    ))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(context!!, loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            // Get an instance of the LocationComponent and then adjust its settings
            mapboxMap.locationComponent.apply {

                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

                // Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

                // Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

                // Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(activity)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(activity, "الرجاء تفعيل الصلاحيات", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
            Toast.makeText(activity, "هاتقك لا يملك كل الصلاحيات", Toast.LENGTH_LONG).show()
        }
    }


    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        Log.i("life cycle", "start")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
        Log.i("life cycle", "destroy view")
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        Log.i("life cycle", "resume")
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
        Log.i("life cycle", "pause")
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
        Log.i("life cycle", "stop")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        Log.i("life cycle", "destroy")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        detached = false
    }

    override fun onDetach() {
        super.onDetach()
        detached = true
    }




}
