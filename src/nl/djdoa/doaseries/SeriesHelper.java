package nl.djdoa.doaseries;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.widget.Toast;


public class SeriesHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME="series.db";
	private static final int SCHEMA_VERSION=1;
	private static File exportDir;
	private static final String backupDBFileName  = "/SeriesDatabaseExports//" + DATABASE_NAME;	
	private static final String backupDBPath  = "/SeriesDatabaseExports";
	
	public SeriesHelper (Context context) {
		super(context, DATABASE_NAME, null, SCHEMA_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE series (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, " +
				"season INTEGER, episode INTEGER, lastviewed TEXT, continued TEXT, status INTEGER, " +
				"image TEXT, wiki TEXT, imdb TEXT, image_data BLOB);");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//db.execSQL("ALTER TABLE series ADD COLUMN image_data BLOB");

	}
	
	public Cursor getAll() {
		String querryAll="SELECT _id, name, season, episode, lastviewed, continued, "+ 
				"status, image, wiki, imdb, image_data FROM series ORDER BY status, name ASC";
		
		return(getReadableDatabase().rawQuery(querryAll, null));			
	}
	
	public Cursor getById(String id) {
		String[] args={id};
		String querrybyID="SELECT _id, name, season, episode, lastviewed, continued, "+ 
				"status, image, wiki, imdb, image_data FROM series WHERE _id=?";
		
		return(getReadableDatabase().rawQuery(querrybyID, args));
	}
	
	public void insert(String name, Integer season, Integer episode, String lastViewed, String continued, 
			Integer status, String image, String wiki, String imdb, byte[] image_data) {
		ContentValues cv = new ContentValues();
		
		cv.put("name", name);
		cv.put("season", season);
		cv.put("episode", episode);
		cv.put("lastViewed", lastViewed);
		cv.put("continued", continued);
		cv.put("status", status);
		cv.put("image", image);
		cv.put("wiki", wiki);
		cv.put("imdb", imdb);
		cv.put("image_data", image_data);
		
		getWritableDatabase().insert("series", "name", cv);		
	}
	
	public void update(String id, String name, Integer season, Integer episode, String lastViewed, String continued, 
	Integer status, String image, String wiki, String imdb, byte[] image_data) {
		String[] args={id};
		ContentValues cv = new ContentValues();
		
		cv.put("name", name);
		cv.put("season", season);
		cv.put("episode", episode);
		cv.put("lastViewed", lastViewed);
		cv.put("continued", continued);
		cv.put("status", status);
		cv.put("image", image);
		cv.put("wiki", wiki);
		cv.put("imdb", imdb);
		cv.put("image_data", image_data);
		
		getWritableDatabase().update("series", cv, "_id=?", args);			
	}
	
	public void delete(String id) {
		String[] args={id};
		getWritableDatabase().delete("series", "_id=?", args);
	}
	
	public String getName(Cursor c) {
		return(c.getString(c.getColumnIndex("name")));		
	}
	
	public Integer getSeason(Cursor c) {
		return(c.getInt(c.getColumnIndex("season")));
	}
	public Integer getEpisode(Cursor c) {
		return(c.getInt(c.getColumnIndex("episode")));
	}
	
	public String getLastViewed(Cursor c) {
		return(c.getString(c.getColumnIndex("lastviewed")));
	}
	
	public String getContinued(Cursor c) {
		return(c.getString(c.getColumnIndex("continued")));
	}
	
	public Integer getStatus (Cursor c) {
		return(c.getInt(c.getColumnIndex("status")));
	}
	
	public String getImage(Cursor c) {
		return(c.getString(c.getColumnIndex("image")));
	}
	
	public String getWiki (Cursor c) {
		return(c.getString(c.getColumnIndex("wiki")));
	}
	
	public String getImdb (Cursor c) {
		return(c.getString(c.getColumnIndex("imdb")));
	}
	
	public byte[] getImageBytes (Cursor c) {
		byte [] temp = null;
		try {
			temp= c.getBlob(c.getColumnIndex("image_data"));
			
		} catch (Exception e) {
			temp = null;
		}				
		return(temp);
	}
	
	public void exportDB(Context context) {
		//close();
		// make export directory if it not already exists
		exportDir = new File(Environment.getExternalStorageDirectory() + backupDBPath);
		if(!exportDir.exists())
		 { if(exportDir.mkdir())
		 	{ // directory is created
			 Toast.makeText(context, "Export directory created", Toast.LENGTH_LONG).show();			
		 	}
		 }
		
		try {
            File sd = Environment.getExternalStorageDirectory();
            //File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
	           
                
               File currentDB = context.getApplicationContext().getDatabasePath(DATABASE_NAME);
	          // File currentDB = new File(data, currentDBPath);
	           File backupDB = new File(sd, backupDBFileName);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                
                
                Toast.makeText(context, backupDB.toString(),
                        Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {

            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG)
                    .show();

        }
    }
	
	public void importDB(Context context) {
		close();
		
		 try {
             File sd = Environment.getExternalStorageDirectory();
             //File data  = Environment.getDataDirectory();

             if (sd.canWrite()) {
                // String  currentDBPath= "//data//" + "PackageName"
               //          + "//databases//" + "DatabaseName";
                 
            	  File currentDB = context.getApplicationContext().getDatabasePath(DATABASE_NAME);
    	          // File currentDB = new File(data, currentDBPath);
    	           File backupDB = new File(sd, backupDBFileName);

                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                 
                 Toast.makeText(context, backupDB.toString(),
                         Toast.LENGTH_LONG).show();

             }
         } catch (Exception e) {

             Toast.makeText(context, e.toString(), Toast.LENGTH_LONG)
                     .show();

         }
  

	}
		
	
	
		


}
