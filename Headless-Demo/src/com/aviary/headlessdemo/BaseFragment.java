package com.aviary.headlessdemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.json.JSONException;
import com.aviary.android.feather.headless.moa.moalite.MoaLiteEffectPack;
import com.aviary.android.feather.headless.moa.moalite.MoaLiteParserFactory;
import com.aviary.android.feather.headless.moa.moalite.MoaLiteParserFactory.MoaLiteParser;
import com.aviary.android.feather.headless.utils.IOUtils;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;


public abstract class BaseFragment extends Fragment {
	public abstract CharSequence getTitle();
	protected abstract void onStarted();
	protected abstract void onStopped();
	
	protected void onImageLoadStart() {}
	protected void onImageLoadComplete( Bitmap bitmap ) {}
	
	public static final String LOG_TAG = BaseFragment.class.getSimpleName();	
	
	// default image used for tests
	public static final String TEST_ASSET_IMAGE = "20120807_090849.jpg";	
	
	/** filename of the default effects pack **/
	public static final String BASIC_EFFECTS_FILENAME = "default_effects.zip";	
	
	/** Destination folder for the effects zip file */
	public static final String EXTRACTED_EFFECTS_FOLDER = "effects/default_effects";
	
	/**
	 * We provide the effects packs as single ".zip" file, but you can also extract them into a temporary folder in order to improve
	 * the I/O operations.
	 * 
	 * Now you can create an Instance of the {@link MoaLiteEffectPack} from the "effects.json" file directly without need to parse
	 * the entire .zip file every time:
	 * 
	 * example: MoaliteParser parser = MoaliteParserFactory.create(); MoaliteEffectPack pack = parser.parseStream( new
	 * FileInputStream(new File("effects.json") ) );
	 * 
	 * @return The extracted folder File
	 * @throws IOException
	 * 
	 */
	public File extractEffects() throws IOException {

		// Warning: Every time the Activity is started we extract and copy all the content of the default_effects.zip file
		// into the app's files directory. This is just a demo so we don't want to put too much logic here...
		File mExtractedEffectsFolder;

		if ( ( mExtractedEffectsFolder = IOUtils.mkdirs( getActivity().getFilesDir(), EXTRACTED_EFFECTS_FOLDER ) ) != null ) {
			Log.i( LOG_TAG, mExtractedEffectsFolder.getAbsolutePath() + ", " + mExtractedEffectsFolder.isDirectory() );

			InputStream stream = getResources().getAssets().open( BaseFragment.BASIC_EFFECTS_FILENAME );
			ZipInputStream zip = new ZipInputStream( stream );

			ZipEntry entry;
			while ( zip.available() > 0 ) {

				entry = zip.getNextEntry();
				if ( entry == null ) {
					break;
				}

				File dstFile = new File( mExtractedEffectsFolder, entry.getName() );
				OutputStream output = new FileOutputStream( dstFile );

				IOUtils.copyFile( zip, output );
				
				// Note. The index file is always "effects.json", all the other files are the effects described in the index file
				Log.d( LOG_TAG, "copying: " + entry.getName() + " into " + dstFile.getAbsolutePath() + ", " + dstFile.exists() );

				zip.closeEntry();
			}
		}
		return mExtractedEffectsFolder;
	}	

	/**
	 * Unzip and decode the .zip file from the assets folder
	 * @param filename
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	MoaLiteEffectPack loadEffectsFromAssets( String filename ) throws IOException, JSONException {
		// First create the parser in order to parse the effects
		MoaLiteParser parser = MoaLiteParserFactory.create();

		// Then create and open a stream to read the effects from
		InputStream stream = getResources().getAssets().open( filename );

		// Send the stream to the parser
		MoaLiteEffectPack pack = parser.parseZip( stream );
		return pack;
	}	
	
	/**
	 * Decode the "effects.json" file from a real file.
	 * Note that the "effects.json" file is always contained inside the "effects.zip" pack file.
	 * If you don't want to parse the effects every time from the assets, then you can extract the zip
	 * and copy the files inside your app's private file folder.
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JSONException
	 */
	MoaLiteEffectPack loadEffectsFromFile( final File file ) throws FileNotFoundException, IOException, JSONException {
		MoaLiteParser parser = MoaLiteParserFactory.create();
		return parser.parseStream( new FileInputStream( file ) );
	}	

	Bitmap loadBitmapFromAssets( String assetName ) {

		InputStream stream;

		try {
			stream = getResources().getAssets().open( assetName );
		} catch ( IOException e ) {
			e.printStackTrace();
			return null;
		}

		Rect rect = DecodeUtils.decodeImageBounds( stream );

		// compute the image sample size to be used for the final bitmap decode
		int sampleSize = DecodeUtils.getSampleSize( getActivity().getBaseContext(), rect.width(), rect.height() );

		try {
			stream = getResources().getAssets().open( assetName );
		} catch ( IOException e ) {
			e.printStackTrace();
			return null;
		}

		return DecodeUtils.loadBitmap( stream, sampleSize );
	}
	
	public abstract class BaseRenderTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			getActivity().setProgressBarIndeterminateVisibility( true );
		}
		
		@Override
		protected void onPostExecute( Result result ) {
			super.onPostExecute( result );
			getActivity().setProgressBarIndeterminateVisibility( false );
		}
		
	}
}
