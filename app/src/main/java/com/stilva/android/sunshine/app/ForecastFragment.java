package com.stilva.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * A fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

	private ArrayAdapter<String> mForecastAdapter;
	private PreferenceDataObject mPreferenceObject;

	public void update(String[] strings) {
		mForecastAdapter.clear();
		// so notifyDataSetChanged() doesn't get called too often
		mForecastAdapter.setNotifyOnChange(false);
		if(strings != null) {
			for(String str : strings) {
				mForecastAdapter.add(str);
			}
		}
		mForecastAdapter.notifyDataSetChanged();
		mForecastAdapter.setNotifyOnChange(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		setHasOptionsMenu(true);

		SharedPreferences mPreferenceManager = PreferenceManager.getDefaultSharedPreferences(getActivity());

		mPreferenceObject = new PreferenceDataObject(mPreferenceManager.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default)), mPreferenceManager.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_default)));

		mForecastAdapter = new ArrayAdapter<String>(
				getActivity(),
				R.layout.list_item_forecast,
				R.id.list_item_forecast_textview,
				new ArrayList<String>()
		);

		ListView view = (ListView) rootView.findViewById(R.id.listview_forecast);
		view.setAdapter(mForecastAdapter);
		view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String forecastString = mForecastAdapter.getItem(position);
				Intent detailActivityIntent = new Intent(view.getContext(), DetailActivity.class);
				detailActivityIntent.putExtra(Intent.EXTRA_TEXT, forecastString);
				startActivity(detailActivityIntent);
			}
		});

		mFetchTask();

		return rootView;
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.forecastfragment, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if(id == R.id.action_refresh) {
			mFetchTask();

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void mFetchTask() {
		new FetchWeatherTask(this).execute(mPreferenceObject.getPostCode(), "json", mPreferenceObject.getUnit(), "7");
	}

	private class PreferenceDataObject {
		private String days = "7";
		private String unit;
		private String postCode;
		private String dataType = "json";

		public PreferenceDataObject(String postCode, String unit) {

			this.postCode = postCode;
			this.unit = unit;
		}

		public String getPostCode() {
			return this.postCode;
		}

		public String getUnit() {
			return this.unit;
		}
	}
}