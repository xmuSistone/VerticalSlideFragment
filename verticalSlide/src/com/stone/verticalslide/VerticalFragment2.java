package com.stone.verticalslide;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class VerticalFragment2 extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.vertical_fragment2, null);
		initView(rootView);
		return rootView;
	}

	/**
	 * 初始化ListView
	 * 
	 * @param rootView
	 *            根View
	 */
	private void initView(View rootView) {
		ListView listview = (ListView) rootView
				.findViewById(R.id.fragment2_listview);
		ListAdapter adapter = new BaseAdapter() {
			private LayoutInflater inflater;

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (inflater == null) {
					inflater = LayoutInflater.from(getActivity());
				}

				if (null == convertView) {
					convertView = inflater.inflate(
							R.layout.fragment2_list_item, null);
				}
				return convertView;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public Object getItem(int position) {
				return position;
			}

			@Override
			public int getCount() {
				return 100;
			}
		};

		listview.setAdapter(adapter);
	}
}
