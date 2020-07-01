package com.example.watchcovout_app

import com.mapbox.mapboxsdk.geometry.LatLng

class Place (val id : String, val title : String, val location : LatLng, val chunk : Int, val nbrPlaces : Int, val type : String) {
}