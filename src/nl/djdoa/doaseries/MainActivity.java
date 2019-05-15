package nl.djdoa.doaseries;

import nl.djdoa.doaseries.R.drawable;
import android.R.color;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
public final static String ID_EXTRA="nl.djdoa._ID";
public final static String URL_EXTRA="nl.djdoa.URL";
public final static String URL_RESULT="nl.djdoa.URLRESULT";
public final static String IMDB_WIKI="nl.djdoa.IMDB or WIKI";

public final String TAG = "DoaSerie";
Cursor model=null;
SeriesHelper helper=null;
SeriesAdapter adapter=null;
ListView lijst = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
				
		Log.d(TAG, "content loaded");
		helper=new SeriesHelper(this);
		Log.d(TAG, "helper initiated");		
		initList();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		int id = item.getItemId();
		if (id == R.id.add_new_serie) {
			Intent i = new Intent(this, DetailActivity.class);
			startActivity(i);
			
			return true;
		} else if (id == R.id.export_database) {
			Toast.makeText(this, "saving", Toast.LENGTH_LONG).show();
			helper.exportDB(MainActivity.this);
		}
		else if (id == R.id.import_database) helper.importDB(this);
		
		return super.onOptionsItemSelected(item);
	}	
	
	@Override
		protected void onDestroy() {			
			super.onDestroy();
			helper.close();
		}
	
	private void initList() {
		if(model!=null) {
			stopManagingCursor(model);
			model.close();
		}
		
		model=helper.getAll();
		startManagingCursor(model);
		Log.d(TAG, "model loaded initiated");
		
		adapter=new SeriesAdapter(model);
		//setListAdapter(adapter);
		
		Log.d(TAG, "SeriesAdapter loaded");
		
		ListView lijst = (ListView)findViewById(R.id.serieslist);
		lijst.setAdapter(adapter);
		lijst.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?>parent, View view, int position,
					long id) {
				Intent i=new Intent(MainActivity.this, DetailActivity.class);

				i.putExtra(ID_EXTRA, String.valueOf(id));
				startActivity(i);
				
			}			
		});
		Log.d(TAG, "listview & listener loaded");
	}
	
	class SeriesAdapter extends CursorAdapter {
		SeriesAdapter(Cursor c) {
			super(MainActivity.this, c);
		}
		@Override
		public void bindView(View row, Context ctxt, Cursor c) {
			SeriesHolder holder = (SeriesHolder)row.getTag();
			
			holder.populateFrom(c, helper);			
		}
		
		@Override
		public View newView(Context ctxt, Cursor c, ViewGroup parent) {
			Log.d(TAG, "newView called");
			LayoutInflater inflater=getLayoutInflater();			
			View row=inflater.inflate(R.layout.row, parent, false);
			SeriesHolder holder=new SeriesHolder(row);
			row.setTag(holder);
			return(row);
		}
	 }
	

	static class SeriesHolder {
		private TextView name=null;
		private TextView season=null;		
		private TextView episode=null;
		private ImageView statusImage=null;
		private ImageView serieImage=null;
		byte [] imageByteArray=null;
		Bitmap bbp=null;
		
				
		SeriesHolder (View row) {
			
			name=(TextView)row.findViewById(R.id.series_name);			
			season=(TextView)row.findViewById(R.id.season);
			episode=(TextView)row.findViewById(R.id.episode);
			statusImage=(ImageView)row.findViewById(R.id.image_status);
			serieImage=(ImageView)row.findViewById(R.id.image_serie);
		}
		
		void populateFrom(Cursor c, SeriesHelper helper) {			
			if (bbp != null) bbp.recycle();
			
			name.setText(helper.getName(c));
			season.setText(String.valueOf(helper.getSeason(c)));
			episode.setText(String.valueOf(helper.getEpisode(c)));
			switch (helper.getStatus(c)) {
				case 0: statusImage.setImageResource(R.drawable.ic_play);
				break;
				case 1: statusImage.setImageResource(R.drawable.ic_pause);
				break;
				case 2: statusImage.setImageResource(R.drawable.ic_stop);
				break;
			default:  statusImage.setImageResource(R.drawable.ic_play);
			}
			byte [] bijtarray = helper.getImageBytes(c);
		
		if (bijtarray !=null) {
			bbp = BitmapFactory.decodeByteArray(bijtarray, 0, bijtarray.length);
			serieImage.setImageBitmap(bbp);
			
			bbp=null;			
	}
	  }
	}



}
