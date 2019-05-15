package nl.djdoa.doaseries;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class GetWebsiteActivity extends ActionBarActivity {
	WebView browser = null;
	String uRLString = "http://www.google.com";
	String extra = null;
	String destinationExtra="IMDB";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_website);
		
		extra = getIntent().getStringExtra(MainActivity.URL_EXTRA);
		destinationExtra = getIntent().getStringExtra(MainActivity.IMDB_WIKI);
		if (extra !=null) uRLString = extra;
		browser = (WebView)findViewById(R.id.webView_browser);
		browser.loadUrl(extra);		
		
		browser.setWebViewClient(new WebViewClient () {
			@Override
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		        view.loadUrl(url);
		        return true;
			}
		});		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.website_menu, menu);	
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		if (id == R.id.save_url) {
			String webUrl = browser.getUrl();		
			Intent j = new Intent();
			j.putExtra(MainActivity.URL_RESULT, webUrl);
			j.putExtra(MainActivity.IMDB_WIKI, destinationExtra);
			setResult(RESULT_OK, j);
			finish();			
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	

}
