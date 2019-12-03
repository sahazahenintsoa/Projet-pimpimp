package com.drawRoute;

import com.google.android.gms.maps.model.LatLng;

public class Create_directionUrl {

	public String getDirectionsUrl(LatLng origin, LatLng dest){

	    // Origin of route
	    String str_origin = "origin="+origin.latitude+","+origin.longitude;

	    // Destination of route
	    String str_dest = "destination="+dest.latitude+","+dest.longitude;

	    // Sensor enabled
	    String sensor = "sensor=false";

	    // Building the parameters to the web service
	    String parameters = str_origin+"&"+str_dest+"&"+sensor+"&alternatives=true";

	    // Output format
	    String output = "json";

	    String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

	    return url;
	}
	
}
