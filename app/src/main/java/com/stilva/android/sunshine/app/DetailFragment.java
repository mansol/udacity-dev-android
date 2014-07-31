package com.stilva.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.*;
import android.widget.TextView;

/**
 * A fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

	private static final String FORECAST_HASHTAG = "#SunshineApp";

	private String mForecast;
	private ShareActionProvider mShareActionProvider;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

		setHasOptionsMenu(true);

		Intent intent = getActivity().getIntent();
		TextView textview = (TextView) rootView.findViewById(R.id.detail_text);

		mForecast = intent.getStringExtra(Intent.EXTRA_TEXT);
		textview.setText(mForecast);

		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.detailfragment, menu);

		MenuItem shareItem = menu.findItem(R.id.action_share);
		mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

		if(mShareActionProvider != null) {

			mShareActionProvider.setShareIntent(createShareForecastIntent());
		}
	}

	private Intent createShareForecastIntent() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		// forces the stack not to remember the next
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_HASHTAG);

		return shareIntent;
	}
}