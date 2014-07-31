package com.stilva.android.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

	private ForecastFragment mForecastFragment;
	private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

	public FetchWeatherTask(ForecastFragment fragmentSync) {
		mForecastFragment = fragmentSync;
	}

	@Override
	protected String[] doInBackground(String... strings) {

		// These two need to be declared outside the try/catch
		// so that they can be closed in the finally block.
		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;
		// Will contain the raw JSON response as a string.
		String forecastJsonStr = null;

		try {
			// Construct the URL for the OpenWeatherMap query
			// Possible parameters are avaiable at OWM's forecast API page, at
			// http://openweathermap.org/API#forecast
			Uri.Builder uriBuilder = new Uri.Builder();

			uriBuilder.scheme("http")
					.authority("api.openweathermap.org")
					.appendPath("/data/2.5/forecast/daily")
					.appendQueryParameter("q", strings[0])
					.appendQueryParameter("mode", strings[1])
					.appendQueryParameter("units", strings[2])
					.appendQueryParameter("cnt", strings[3]);

			URL url = new URL(uriBuilder.build().toString());

			// Create the request to OpenWeatherMap, and open the connection
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();

			// Read the input stream into a String
			InputStream inputStream = urlConnection.getInputStream();
			StringBuffer buffer = new StringBuffer();
			if (inputStream == null) {
				// Nothing to do.
				forecastJsonStr = null;
			}
			reader = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			while ((line = reader.readLine()) != null) {
				// Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
				// But it does make debugging a *lot* easier if you print out the completed
				// buffer for debugging.
				buffer.append(line + "\n");
			}

			if (buffer.length() == 0) {
				// Stream was empty.  No point in parsing.
				forecastJsonStr = null;
			}
			forecastJsonStr = buffer.toString();

//			Log.i(LOG_TAG, forecastJsonStr);
		} catch (IOException e) {
			Log.e("ForecastFragment", "Error ", e);
			// If the code didn't successfully get the weather data, there's no point in attemping
			// to parse it.
			forecastJsonStr = null;
		} finally{
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (final IOException e) {
//					Log.e("ForecastFragment", "Error closing stream", e);
				}
			}
		}

		try {

			return getWeatherDataFromJson(forecastJsonStr, Integer.valueOf(strings[3]));
		} catch (JSONException e) {

			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPostExecute(String[] strings) {
		mForecastFragment.update(strings);
	}

	/* The date/time conversion code is going to be moved outside the asynctask later,
		 * so for convenience we're breaking it out into its own method now.
		 */
	private String getReadableDateString(long time){
		// Because the API returns a unix timestamp (measured in seconds),
		// it must be converted to milliseconds in order to be converted to valid date.
		Date date = new Date(time * 1000);
		SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
		return format.format(date).toString();
	}

	/**
	 * Prepare the weather high/lows for presentation.
	 */
	private String formatHighLows(double high, double low) {
		// For presentation, assume the user doesn't care about tenths of a degree.
		long roundedHigh = Math.round(high);
		long roundedLow = Math.round(low);

		String highLowStr = roundedHigh + "/" + roundedLow;
		return highLowStr;
	}

	/**
	 * Take the String representing the complete forecast in JSON Format and
	 * pull out the data we need to construct the Strings needed for the wireframes.
	 *
	 * Fortunately parsing is easy:  constructor takes the JSON string and converts it
	 * into an Object hierarchy for us.
	 */
	private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
			throws JSONException {

		// These are the names of the JSON objects that need to be extracted.
		final String OWM_LIST = "list";
		final String OWM_WEATHER = "weather";
		final String OWM_TEMPERATURE = "temp";
		final String OWM_MAX = "max";
		final String OWM_MIN = "min";
		final String OWM_DATETIME = "dt";
		final String OWM_DESCRIPTION = "main";

		JSONObject forecastJson = new JSONObject(forecastJsonStr);
		JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

		String[] resultStrs = new String[numDays];
		for(int i = 0; i < weatherArray.length(); i++) {
			// For now, using the format "Day, description, hi/low"
			String day;
			String description;
			String highAndLow;

			// Get the JSON object representing the day
			JSONObject dayForecast = weatherArray.getJSONObject(i);

			// The date/time is returned as a long.  We need to convert that
			// into something human-readable, since most people won't read "1400356800" as
			// "this saturday".
			long dateTime = dayForecast.getLong(OWM_DATETIME);
			day = getReadableDateString(dateTime);

			// description is in a child array called "weather", which is 1 element long.
			JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
			description = weatherObject.getString(OWM_DESCRIPTION);

			// Temperatures are in a child object called "temp".  Try not to name variables
			// "temp" when working with temperature.  It confuses everybody.
			JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
			double high = temperatureObject.getDouble(OWM_MAX);
			double low = temperatureObject.getDouble(OWM_MIN);

			highAndLow = formatHighLows(high, low);
			resultStrs[i] = day + " - " + description + " - " + highAndLow;
		}

		return resultStrs;
	}
}