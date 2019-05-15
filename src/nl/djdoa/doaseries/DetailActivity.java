package nl.djdoa.doaseries;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

public class DetailActivity extends ActionBarActivity {
	String TAG="DetailActivity";
	protected static final int REQUESTCODE = 100;
	protected static final int IMAGEREQUESTCODE = 200;
	private Bitmap bitmap;	
	
	String seriesID=null;	
	SeriesHelper helper=null;
	
	EditText name=null;
	NumberPicker season=null;
	NumberPicker episode=null;
	EditText lastViewed=null;
	EditText continued=null;
	Spinner status=null;
	ImageView image=null;
	Button imdbButton;
	Button wikiButton;
	ImageButton markCalendarButton;
	EditText imdb=null;
	EditText wiki=null;
	
	byte [] imageByteArray=null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);			
		
		helper=new SeriesHelper(this);
		
		seriesID=getIntent().getStringExtra(MainActivity.ID_EXTRA); //Seriesid=null when add new show is chosen
		
		image = (ImageView)findViewById(R.id.image_series_big);
		
		name=(EditText)findViewById(R.id.name);
		
		season=(NumberPicker)findViewById(R.id.seasonpicker);
		season.setMaxValue(20);
		season.setMinValue(1);
		
		episode=(NumberPicker)findViewById(R.id.episodepicker);
		episode.setMaxValue(30);
		episode.setMinValue(1);
		
		lastViewed=(EditText)findViewById(R.id.lastviewed);		
		lastViewed.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					putCurrentDate(lastViewed);
					lastViewed.clearFocus();
				}				
			}
		});		
		
		continued=(EditText)findViewById(R.id.continued);		
		continued.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					pickDate(continued);
					continued.clearFocus();				
				}				
			}
		});		
		
		status=(Spinner)findViewById(R.id.statusspinner);		
		imdb=(EditText)findViewById(R.id.imdb_url);		
		wiki=(EditText)findViewById(R.id.wiki_url);
		
		wikiButton=(Button)findViewById(R.id.wiki_button);
		wikiButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (wiki.getText().toString().equals("")) {
					Toast.makeText(DetailActivity.this, "Please select weblink first", Toast.LENGTH_LONG).show();
					selectURL("WIKI");									
				} 
			else {
				Intent k = new Intent(Intent.ACTION_VIEW, Uri.parse(wiki.getText().toString()));
				startActivity(k);	
				}				
			}
		});
		
		imdbButton=(Button)findViewById(R.id.imdb_button);
		imdbButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (imdb.getText().toString().equals("")) {
					Toast.makeText(DetailActivity.this, "Please select weblink first", Toast.LENGTH_LONG).show();
					selectURL("IMDB");									
				} 
			else {
				Intent k = new Intent(Intent.ACTION_VIEW, Uri.parse(imdb.getText().toString()));
				startActivity(k);	
				}				
			}
		});
		
		markCalendarButton=(ImageButton)findViewById(R.id.put_in_agenda);
		markCalendarButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				enterCalendar();				
			}
		});
		
		if (seriesID!=null) load();
	}	
	
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (seriesID==null) {
			menu.findItem(R.id.viewed_episode).setEnabled(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		getMenuInflater().inflate(R.menu.detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		if (id == R.id.save) save();
		if (id == R.id.delete) {
			helper.delete(seriesID);
			finish();
		}
		if (id == R.id.viewed_episode) {			
			fwdOneEpisode();
		}
		if (id == R.id.getIMDB) selectURL("IMDB");
		
		if (id == R.id.getWiki) selectURL("WIKI");
				
		if (id == R.id.getImage) pickImage();
			
		return super.onOptionsItemSelected(item);
	}		
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUESTCODE && resultCode == RESULT_OK) {
			String newURL = data.getStringExtra(MainActivity.URL_RESULT);			
			if (data.getStringExtra(MainActivity.IMDB_WIKI).equals("IMDB")) {
				imdb.setText(newURL);
			} else wiki.setText(newURL);			
		}
		if (requestCode == IMAGEREQUESTCODE && resultCode == RESULT_OK) {
			try {
                // We need to recyle unused bitmaps
                if (bitmap != null) {
                    bitmap.recycle();
                }
                InputStream stream = getContentResolver().openInputStream(
                        data.getData());
                bitmap = BitmapFactory.decodeStream(stream);
                stream.close();
                image.setImageBitmap(bitmap);                
                
                ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
                bitmap = createScaledBmpKeepAspectRatio(bitmap, 450);
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream1);
                imageByteArray = stream1.toByteArray();
                stream1.close();
                
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }			
		}
	}
	
	private void load() {
		Cursor c=helper.getById(seriesID);
		
		c.moveToFirst();
		name.setText(helper.getName(c));
		
		season.setValue(helper.getSeason(c));
		episode.setValue(helper.getEpisode(c));
		status.setSelection(helper.getStatus(c));	
		lastViewed.setText(helper.getLastViewed(c));
		continued.setText(helper.getContinued(c));
		wiki.setText(helper.getWiki(c));
		imdb.setText(helper.getImdb(c));
		
		imageByteArray = helper.getImageBytes(c);
				
		if (imageByteArray !=null) {
		Bitmap bmp = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
		image.setImageBitmap(bmp);
		
		bmp=null;
		}		
	}
	
	private void save() {
	
		String a = name.getText().toString();
		Integer b = season.getValue();
		Integer c = episode.getValue();
		String d = lastViewed.getText().toString();
		String e = continued.getText().toString();
		Integer f = status.getSelectedItemPosition();
		String g = "";
		String h = wiki.getText().toString();
		String i = imdb.getText().toString();
		byte[] j =  null;
		
		if (imageByteArray !=null) j=imageByteArray;
		
		if (a.equals("")) Toast.makeText(DetailActivity.this, "Please enter unique series name", Toast.LENGTH_LONG).show();
		
		if (seriesID!=null) {
			helper.update(seriesID,a, b, c, d, e, f, g, h, i, j);
		} else {		
				helper.insert(a, b, c, d, e, f, g, h, i, j);
		}
		finish();
	}
	
	private void pickDate(EditText datumveld) {
		String inhoud = datumveld.getText().toString();
		int mYear, mMonth, mDay;
		
		if (inhoud.trim().length()>5) { // bevat veld al een datum?
			
			String[] dayMonth=inhoud.split("-"); // ontleed Edittext inhoud in dag, maand en jaar
			
			mYear = Integer.parseInt(dayMonth[2]);
			mMonth = Integer.parseInt(dayMonth[1]) -1;
			mDay = Integer.parseInt(dayMonth[0]);				
		}
		else {			
			Calendar c = Calendar.getInstance(); // neem anders de huidige datum
			mYear = c.get(Calendar.YEAR);
			mMonth = c.get(Calendar.MONTH);
			mDay = c.get(Calendar.DAY_OF_MONTH);
		}
		
		
		DatePickerDialog dpd = new DatePickerDialog(DetailActivity.this,
				new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						// Display Selected date in textbox
						continued.setText(dayOfMonth + "-"
								+ (monthOfYear + 1) + "-" + year);

					}
				}, mYear, mMonth, mDay);
		dpd.show();
	}
	
	private void putCurrentDate(EditText datumveld) {
		Calendar c = Calendar.getInstance(); 
		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		int mDay = c.get(Calendar.DAY_OF_MONTH);
		datumveld.setText(mDay + "-"
				+ (mMonth + 1) + "-" + mYear);
	}
	
	private void fwdOneEpisode () {
		int oud = episode.getValue();
		episode.setValue(oud+1);
		putCurrentDate(lastViewed);
		save();
	}
	
	public void selectURL (String vindDeze) {
		String search = "http://www.google.com";
		
		if (vindDeze.equals("IMDB")) {
				search = "http://imdb.com/search/title?title=" + name.getText().toString() + "&title_type=tv_series";
		}
		if (vindDeze.equals("WIKI")) {
				search = "http://en.wikipedia.org/w/index.php?title=Special:Search&search="
						+ "list of " + name.getText().toString() + "+ episodes" + "&fulltext=Search";
		}
		Intent i = new Intent(DetailActivity.this, GetWebsiteActivity.class);
		i.putExtra(MainActivity.URL_EXTRA, search);
		i.putExtra(MainActivity.IMDB_WIKI, vindDeze);
		startActivityForResult(i, REQUESTCODE);		
	}
	
	public void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, IMAGEREQUESTCODE);
    }	

private Bitmap createScaledBmpKeepAspectRatio(Bitmap bitje, int maxSide) {
		
		int orgHeight = bitje.getHeight();
		int orgWidth = bitje.getWidth();
		
		int scaledWidth = (orgWidth >= orgHeight) ? maxSide 
				: (int) ((float) maxSide * ((float) orgWidth / (float) orgHeight));
		
		int scaledHeight = (orgHeight >= orgWidth) ? maxSide
				: (int) ((float) maxSide * ((float) orgHeight / (float) orgWidth));
		
		Bitmap scaledGalleryPic = Bitmap.createScaledBitmap(bitje, scaledWidth, scaledHeight, true);
		
		return scaledGalleryPic;		
	}
/**
 * Niet gebruik, wel handig	
 * @param showMessage shows alertdialogbox with this message
 */
public void showSimpleAlert(String showMessage) {
	
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setMessage(showMessage)
	       .setCancelable(true)
	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();
	           }
	       });
	AlertDialog alert = builder.create();
	alert.show();
}

public boolean enterCalendar () {
	String datum =continued.getText().toString();
	if (datum.equals("")) {
		Toast.makeText(DetailActivity.this, "Please insert valid date first", Toast.LENGTH_LONG).show();
		pickDate(continued);
		return false;
	} 
	
	int mYear, mMonth, mDay;
	
	if (datum.trim().length()>5) { 		
		String[] dayMonth=datum.split("-"); // ontleed Edittext inhoud in dag, maand en jaar
		String eventTitle = "New season " + (season.getValue()+1) + " of " + name.getText().toString() + " begins!";	
		mYear = Integer.parseInt(dayMonth[2]);
		mMonth = Integer.parseInt(dayMonth[1]) -1;
		mDay = Integer.parseInt(dayMonth[0]);
		
		Calendar cali = Calendar.getInstance();
		cali.set(mYear, mMonth, mDay, 12, 0);
		if (cali.getTimeInMillis()<System.currentTimeMillis()) {
			Toast.makeText(DetailActivity.this, "Please insert a date in the future", Toast.LENGTH_LONG).show();
			return false;
		} else {
			Intent intent = new Intent(Intent.ACTION_INSERT)
	        .setData(Events.CONTENT_URI)
	        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cali.getTimeInMillis())	       
	        .putExtra(Events.TITLE, eventTitle)
	        .putExtra(Events.DESCRIPTION, "A new season of your favourite tv show is about to begin. Start downloading now")
	        .putExtra(Events.HAS_ALARM, true)
	        //.putExtra(Events.EVENT_LOCATION, "The gym")
	        .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_FREE);
	       // .putExtra(Intent.EXTRA_EMAIL, "rowan@example.com,trevor@example.com");
			startActivity(intent);
		}	
	}		
	return false;
}
}


