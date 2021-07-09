package com.maknoon.mawaqeet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.batoulapps.adhan.CalculationMethod;
import com.batoulapps.adhan.CalculationParameters;
import com.batoulapps.adhan.Coordinates;
import com.batoulapps.adhan.Madhab;
import com.batoulapps.adhan.PrayerTimes;
import com.batoulapps.adhan.data.DateComponents;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity
{
	private static final int ACCESS_FINE_LOCATION_PERMISSION = 1;
	final private static String TAG = "MainActivity";

	TextView fajr, shrouk, zuhr, asr, maghrib, isha, longitude, latitude, altitude, altitudeMsl, timezone, city, gps;
	Double lng, lat, alt;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		fajr = findViewById(R.id.fajr);
		shrouk = findViewById(R.id.shrouk);
		zuhr = findViewById(R.id.zuhr);
		asr = findViewById(R.id.asr);
		maghrib = findViewById(R.id.maghrib);
		isha = findViewById(R.id.isha);
		longitude = findViewById(R.id.longitude);
		latitude = findViewById(R.id.latitude);
		altitude = findViewById(R.id.altitude);
		altitudeMsl = findViewById(R.id.altitudeMsl);
		timezone = findViewById(R.id.timezone);
		city = findViewById(R.id.city);
		gps = findViewById(R.id.gps);

		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			final LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
			final Location lc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (lc != null)
			{
				lng = lc.getLongitude();
				lat = lc.getLatitude();
				alt = lc.getAltitude();
				calculate(lng, lat, alt);
			}
			else
			{
				final Location loc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
				if (loc != null)
				{
					lng = loc.getLongitude();
					lat = loc.getLatitude();
					alt = loc.getAltitude();
					calculate(lng, lat, alt);
				}
			}

			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			{
				lm.addNmeaListener(new OnNmeaMessageListener()
				{
					@Override
					public void onNmeaMessage(String message, long timestamp)
					{
						if (message.startsWith("$GPGGA") || message.startsWith("$GNGNS") || message.startsWith("$GNGGA"))
						{
							final Double altitudeMsg = getAltitudeMeanSeaLevel(message);
							if (altitudeMsg != null)
								altitudeMsl.setText(getString(R.string.altitudeMsl, altitudeMsg.toString()));
						}
					}
				});
			}

			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, new LocationListener()
			{
				@Override
				public void onLocationChanged(@NonNull Location location)
				{
					lng = location.getLongitude();
					lat = location.getLatitude();
					alt = location.getAltitude();
					calculate(lng, lat, alt);
				}

				@Override
				public void onProviderDisabled(@NonNull String provider)
				{
					gps.setText(R.string.gps_disable_note);
				}

				@Override
				public void onStatusChanged(String provider, int status, Bundle extras)
				{
					/* This is called when the GPS status alters */
					switch (status)
					{
						case LocationProvider.OUT_OF_SERVICE:
							Toast.makeText(getApplicationContext(), "Status Changed: Out of Service", Toast.LENGTH_SHORT).show();
							break;
						case LocationProvider.TEMPORARILY_UNAVAILABLE:
							Toast.makeText(getApplicationContext(), "Status Changed: Temporarily Unavailable", Toast.LENGTH_SHORT).show();
							break;
						case LocationProvider.AVAILABLE:
							Toast.makeText(getApplicationContext(), "Status Changed: Available", Toast.LENGTH_SHORT).show();
							break;
					}
				}

				@Override
				public void onProviderEnabled(@NonNull String provider)
				{
					recreate();
				}
			});
		}
		else
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION);
		}
	}

	void calculate(Double lng, Double lat, Double alt)
	{
		final TimeZone tz = TimeZone.getDefault();

		longitude.setText(getString(R.string.longitude, lng.toString()));
		latitude.setText(getString(R.string.latitude, lat.toString()));
		altitude.setText(getString(R.string.altitude, alt.toString()));
		timezone.setText(getString(R.string.timezone, tz.getDisplayName(false, TimeZone.SHORT), tz.getID()));

		final Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
		try
		{
			final List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
			final Address obj = addresses.get(0);
			//final Vector<String> addr = new Vector<>(10);

			final String add = obj.getAddressLine(0);
			city.setText(getString(R.string.city, add));

			/*
			if (add != null) addr.add(add);
			add = obj.getCountryName();
			if (add != null && !addr.contains(add)) addr.add(add);
			add = obj.getCountryCode();
			if (add != null && !addr.contains(add)) addr.add(add);
			add = obj.getAdminArea();
			if (add != null && !addr.contains(add)) addr.add(add);
			add = obj.getPostalCode();
			if (add != null && !addr.contains(add)) addr.add(add);
			add = obj.getSubAdminArea();
			if (add != null && !addr.contains(add)) addr.add(add);
			add = obj.getLocality();
			if (add != null && !addr.contains(add)) addr.add(add);
			add = obj.getSubThoroughfare();
			if (add != null && !addr.contains(add)) addr.add(add);

			add = "";
			for (int i = 0; i < addr.size(); i++)
				add = add + "\n" + addr.elementAt(i);

			city.setText("Address: " + add);
			*/
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		final Coordinates coordinates = new Coordinates(lat, lng);
		final DateComponents date = DateComponents.from(new Date());
		final CalculationParameters params = CalculationMethod.DUBAI.getParameters();
		params.madhab = Madhab.SHAFI;
		//params.adjustments.fajr = 2;
		final PrayerTimes prayerTimes = new PrayerTimes(coordinates, date, params);

		final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());
		formatter.setTimeZone(tz);

		fajr.setText(getString(R.string.fajr, formatter.format(prayerTimes.fajr)));
		shrouk.setText(getString(R.string.shrouk, formatter.format(prayerTimes.sunrise)));
		zuhr.setText(getString(R.string.zuhr, formatter.format(prayerTimes.dhuhr)));
		asr.setText(getString(R.string.asr, formatter.format(prayerTimes.asr)));
		maghrib.setText(getString(R.string.maghrib, formatter.format(prayerTimes.maghrib)));
		isha.setText(getString(R.string.isha, formatter.format(prayerTimes.isha)));

		/*
		new Thread(new Runnable()
		{
			public void run()
			{
				final double d = getAltitude(longitude, latitude);
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						mTextView1.setText("Elevation (nationalmap): " + d);
					}
				});
			}
		}).start();


		double CalcWayFajr, CalcWayIsha;
		int CalcWay = 3;
		//0 Univ. Of Islamic Scinces, Karachi
		//1 Islamic Society Of North America
		//2 Muslim World League
		//3 Umm Al-Qura University
		//4 Egytion General Authority of Survey
		CalcWayFajr =-18;
		CalcWayIsha =-17;

		switch(CalcWay)
		{
			case 0:
				CalcWayFajr =  -18;
				CalcWayIsha =  -18;
				break;
			case 1:
				CalcWayFajr =  -15;
				CalcWayIsha =  -15;
				break;
			case 2:
				CalcWayFajr =  -18;
				CalcWayIsha =  -17;
				break;
			case 3:
				CalcWayFajr =  -19;
				CalcWayIsha =  -18;
				break;
			case 4:
				CalcWayFajr =  -19.5;
				CalcWayIsha =  -17.5;
				break;
		}

		final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		final int year = calendar.get(Calendar.YEAR);
		final int month = calendar.get(Calendar.MONTH) + 1;
		final int day = calendar.get(Calendar.DAY_OF_MONTH);

		final int a = year * 367;
		final int b = (month + 9) / 12;
		final int c = (year + b) * 7;
		final int d = c / 4;
		final int e = month / 9 * 275;
		final double D = a - d + e + day - 730531.5;

		final double L = myMod(280.461 + 0.9856474 * D, 360);
		final double M = myMod(357.528 + 0.9856003 * D, 360);

		final double Lambda = L + 1.915 * mySin(M) + 0.02 * mySin(2 * M);
		final double Obliquity = 23.439 - 0.0000004 * D;
		final byte Qrt = GetQuarter(Lambda);
		final double Alpha = myArcTan(myCos(Obliquity) * myTan(Lambda), Qrt);
		final double ST = myMod(100.46 + 0.985647352 * D, 360);
		final double Dec = myArcSin(mySin(Obliquity) * mySin(Lambda));

		// RAK zone number
		int Zone = 400;

		final double Noon = myMod(Alpha - ST,360);
		final double UTNoon = Noon - longitude;
		final double LocalNoon = myMod((UTNoon/15) + Zone,24);

		// TODO: Shafi AsrType 0 Or Hanafi AsrType 1
		byte AsrType = 0;

		final double AsrAlt = 90-myArcTan((AsrType+1)+myTan(abs(latitude-Dec)), (byte)0);
		final double AsrArc = myArcCos((mySin(AsrAlt)-mySin(Dec)*mySin(latitude)) /(myCos(Dec)*myCos(latitude)));
		final double AsrTime = LocalNoon + AsrArc/15;

		final double DurinalArc = myArcCos((mySin(-0.8333) - mySin(Dec)*mySin(latitude)) /(myCos(Dec)*myCos(latitude)));
		double SunRise = LocalNoon - (DurinalArc/15);
		double SunSet = LocalNoon + (DurinalArc/15);

		final double IshaArc = myArcCos((mySin(CalcWayIsha) -mySin(Dec)*mySin(latitude)) /(myCos(Dec)*myCos(latitude)));
		double IshaTime = LocalNoon +(IshaArc /15);

		final double FajrArc = myArcCos((mySin(CalcWayFajr)-mySin(Dec)*mySin(latitude)) /(myCos(Dec)*myCos(latitude)));
		double FajrTime = LocalNoon -(FajrArc /15);

		if (SunRise >= LocalNoon)
		{
			SunRise = 0;
			FajrTime = 0;
		}
		else if (FajrTime >= SunRise)
			FajrTime = 0;

		if (SunSet <= LocalNoon)
		{
			SunSet = 0;
			IshaTime = 0;
		}
		else if (IshaTime <= SunSet)
			IshaTime = 0;

		mTextView2.setText(
				"Fajr: " + Frac(FajrTime / 24) + "\n" +
				"Shrouk: " + Frac(SunRise / 24) + "\n" +
				"Zuhr: " + Frac(LocalNoon / 24) + "\n" +
				"Asr: " + Frac(AsrTime / 24) + "\n" +
				"Maghrib: " + Frac(SunSet / 24) + "\n" +
				"Isha: " + Frac(IshaTime / 24)
		);

		mTextView2.setText(
				"Fajr: " + FajrTime  + "\n" +
						"Shrouk: " + SunRise  + "\n" +
						"Zuhr: " + LocalNoon + "\n" +
						"Asr: " + AsrTime + "\n" +
						"Maghrib: " + SunSet + "\n" +
						"Isha: " + IshaTime
		);
		*/
	}

	/**
	 * Given a $GPGGA, $GNGNS, or $GNGGA NMEA sentence, return the altitude above mean sea level (geoid
	 * altitude),
	 * or null if the altitude can't be parsed.
	 *
	 * Example inputs are:
	 * $GPGGA,032739.0,2804.732835,N,08224.639709,W,1,08,0.8,19.2,M,-24.0,M,,*5B
	 * $GNGNS,015002.0,2804.733672,N,08224.631117,W,AAN,09,1.1,78.9,-24.0,,*23
	 * $GNGGA,172814.00,2803.208136,N,08225.981423,W,1,08,1.1,-19.7,M,-24.8,M,,*5F
	 *
	 * Example outputs would be:
	 * 19.2
	 * 78.9
	 * -19.7
	 *
	 * @param nmeaSentence a $GPGGA, $GNGNS, or $GNGGA NMEA sentence
	 * @return the altitude above mean sea level (geoid altitude), or null if altitude can't be
	 * parsed
	 */
	public Double getAltitudeMeanSeaLevel(String nmeaSentence)
	{
		final int ALTITUDE_INDEX = 9;
		String[] tokens = nmeaSentence.split(",");

		if (nmeaSentence.startsWith("$GPGGA") || nmeaSentence.startsWith("$GNGNS") || nmeaSentence.startsWith("$GNGGA"))
		{
			String altitude;
			try
			{
				altitude = tokens[ALTITUDE_INDEX];
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				Log.e(TAG, "Bad NMEA sentence for geoid altitude - " + nmeaSentence + " :" + e);
				return null;
			}

			if (!TextUtils.isEmpty(altitude))
			{
				Double altitudeParsed = null;
				try
				{
					altitudeParsed = Double.parseDouble(altitude);
				}
				catch (NumberFormatException e)
				{
					Log.e(TAG, "Bad geoid altitude value of '" + altitude + "' in NMEA sentence " + nmeaSentence + " :" + e);
				}
				return altitudeParsed;
			}
			else
			{
				Log.w(TAG, "Couldn't parse geoid altitude from NMEA: " + nmeaSentence);
				return null;
			}
		}
		else
		{
			Log.w(TAG, "Input must be $GPGGA, $GNGNS, or $GNGGA NMEA: " + nmeaSentence);
			return null;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == ACCESS_FINE_LOCATION_PERMISSION)
		{
			// If request is cancelled, the result arrays are empty.
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				// permission is granted. Do the job
				recreate();
			}
			else
				Log.v(TAG, "request denied");
		}
	}
}