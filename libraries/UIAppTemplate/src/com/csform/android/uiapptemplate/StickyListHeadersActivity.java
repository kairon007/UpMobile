package com.csform.android.uiapptemplate;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.csform.android.uiapptemplate.adapter.MyStickyListHeadersAdapter;
import com.nhaarman.listviewanimations.appearance.StickyListHeadersAdapterDecorator;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.util.StickyListHeadersListViewWrapper;

public class StickyListHeadersActivity extends ActionBarActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stickylistheaders);

		StickyListHeadersListView listView = (StickyListHeadersListView) findViewById(R.id.activity_stickylistheaders_listview);
		listView.setFitsSystemWindows(true);
		MyStickyListHeadersAdapter adapter = new MyStickyListHeadersAdapter(
				this);
		AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter(
				adapter);
		StickyListHeadersAdapterDecorator stickyListHeadersAdapterDecorator = new StickyListHeadersAdapterDecorator(
				animationAdapter);
		stickyListHeadersAdapterDecorator
				.setListViewWrapper(new StickyListHeadersListViewWrapper(
						listView));
		assert animationAdapter.getViewAnimator() != null;
		animationAdapter.getViewAnimator().setInitialDelayMillis(500);
		assert stickyListHeadersAdapterDecorator.getViewAnimator() != null;
		stickyListHeadersAdapterDecorator.getViewAnimator()
				.setInitialDelayMillis(500);
		listView.setAdapter(stickyListHeadersAdapterDecorator);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}