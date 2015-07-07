package com.bentyn.traincoll.android;

import com.bentyn.traincoll.android.communication.MessageController;
import com.bentyn.traincoll.android.map.TrainMarker;
import com.bentyn.traincoll.android.map.TrainMarkerController;
import com.bentyn.traincoll.android.train.TrainController;
import com.bentyn.traincoll.commons.communication.MessageType;
import com.bentyn.traincoll.commons.data.TrainData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

import javax.inject.Inject;

import dagger.ObjectGraph;
import de.tavendo.autobahn.WebSocketException;

public class MainActivity extends FragmentActivity implements LocationListener{
	private TextView latituteField;
	private TextView longitudeField;
	private TextView speedField;
	private TextView headingField;
	private LocationManager locationManager;
	private String provider;
	private GoogleMap googleMap;
	private static final String TAG = "WEBSOCKET";
	private ObjectGraph objectGraph;
	@Inject
	MessageController messageController;
	@Inject
	TrainMarkerController markerController;
	@Inject
	TrainController trainController;



	public static final int MARKER_COLOR= Color.YELLOW;
	public static final double COLLISION_RANGE = 10000;
	public static final int REMOVE_INTERVAL=10000;

	private void connectToWebSocket() {

		try {
			messageController.connect();
			Log.d(TAG, messageController.toString());
		} catch (WebSocketException e) {

			Log.d(TAG, e.toString());
		}
	}

	private void initTrain(){

	}
	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// initialize DI
		objectGraph = ObjectGraph.create(new MainModule(this));
		objectGraph.inject(this);

		setContentView(R.layout.activity_main);
		latituteField = (TextView) findViewById(R.id.LatValView);
		longitudeField = (TextView) findViewById(R.id.LngValView);
		speedField = (TextView) findViewById(R.id.SpeedValView);
		headingField =(TextView) findViewById(R.id.HeadingValView);

		connectToWebSocket();
		initTrain();
		initRemoveTask();

		SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		googleMap = supportMapFragment.getMap();
		googleMap.setMyLocationEnabled(true);

		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the locatioin provider -> use
		// default
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);

		// Initialize the location fields
		if (location != null) {
			onLocationChanged(location);
		} else {
			latituteField.setText("Location not available");
			longitudeField.setText("Location not available");
		}
	}

	/* Request updates at startup */
	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(provider, 400, 1, this);
	}

	/* Remove the location listener updates when Activity is paused */
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		// send position update
		TrainData train = new TrainData();
		train.setLatitude(location.getLatitude());
		train.setLongitude(location.getLongitude());
		train.setSpeed(location.getSpeed());
		train.setHeading(location.getBearing());
		train=trainController.addMyPosition(train);
		messageController.sendMessage(MessageType.POSITION_UPDATE, train);
		// set Text values
		latituteField.setText(String.valueOf(location.getLatitude()));
		longitudeField.setText(String.valueOf(location.getLongitude()));
		speedField.setText(String.valueOf(train.getSpeed()));
		headingField.setText(String.valueOf(train.getHeading()));
		// set Map markers
		markerController.insertOrUpdate(train,googleMap,this);

		googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(train.getLatitude(),train.getLongitude())));
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));
	}
	// removing trains that are not in range
	public void initRemoveTask() {
		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				try {
					markerController.removeOutOfRange(trainController.getMyPosition(),COLLISION_RANGE);

				} catch (Exception e) {

				}
			}
		};
		timer.schedule(timerTask, 0, REMOVE_INTERVAL);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,Toast.LENGTH_SHORT).show();
	}

	public GoogleMap getGoogleMap() {
		return googleMap;
	}
}
