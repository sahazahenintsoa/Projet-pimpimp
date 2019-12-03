package com.drawRoute;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParserTask {

	ArrayList<PolylineOptions> polyOptions_locations = new ArrayList<PolylineOptions>();

	public static ArrayList<ArrayList<LatLng>> latlng_points_routes = new ArrayList<ArrayList<LatLng>>();

	public ArrayList<PolylineOptions> parse_jsonData(String jsonData) {

		JSONObject jObject = null;
		List<List<HashMap<String, String>>> routes = null;

		try {
			jObject = new JSONObject(jsonData);
			DirectionsJSONParser parser = new DirectionsJSONParser();

			// Starts parsing data
			routes = parser.parse(jObject);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (latlng_points_routes != null && latlng_points_routes.size() > 0) {
			latlng_points_routes.clear();
		}

		if (polyOptions_locations != null && polyOptions_locations.size() > 0) {
			polyOptions_locations.clear();
		}

		if (jObject != null && routes != null) {

			ArrayList<LatLng> points = null;
			MarkerOptions markerOptions = new MarkerOptions();

			
			for (int i = 0; i < routes.size(); i++) {
				points = new ArrayList<LatLng>();

				ArrayList<LatLng> points_latLng = new ArrayList<LatLng>();
				PolylineOptions lineOptions = new PolylineOptions();

				// Fetching i-th route
				List<HashMap<String, String>> path = routes.get(i);

				// Fetching all the points in i-th route
				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);

					points.add(position);

					points_latLng.add(position);
				}

				// Adding all the points in the route to LineOptions
				lineOptions.addAll(points);
				lineOptions.width(18);
				lineOptions.color(Color.BLUE);

				latlng_points_routes.add(points_latLng);

				polyOptions_locations.add(lineOptions);
				
			}
		}
		return polyOptions_locations;
	}

}
