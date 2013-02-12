package com.aviary.headlessdemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.JSONException;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.aviary.android.feather.headless.AviaryEffect;
import com.aviary.android.feather.headless.moa.moalite.MoaLiteEffectPack;
import com.aviary.android.feather.headless.moa.moalite.MoaLiteEffectPack.MoaLiteEffectItem;

public class SimpleEffectFromFile extends SimpleEffectFromAssets {

	/** location of the extracted files */
	private File mEffectsFolder;
	
	private Button mExtractButton;

	@Override
	public CharSequence getTitle() {
		return "Simple from file";
	}
	
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		if ( container == null ) {
			return null;
		}
		return inflater.inflate( R.layout.simple_effect_from_file, container, false );
	}	

	@Override
	public void onActivityCreated( Bundle savedInstanceState ) {
		super.onActivityCreated( savedInstanceState );
		initializeUI();
	}
	
	@Override
	protected void initializeUI() {
		super.initializeUI();
		mExtractButton = (Button) getView().findViewById( R.id.extract_button );
		mExtractButton.setOnClickListener( this );
	}
	
	@Override
	public void onClick( View v ) {
		super.onClick( v );
		
		if( v.getId() == mExtractButton.getId() ) {
			extractAndCopyEffects();
		}
	}
	
	@Override
	protected void onStarted() {
		loadImage();
	}
	
	private void extractAndCopyEffects() {
		try {
			mEffectsFolder = extractEffects();
		} catch ( IOException e ) {
			e.printStackTrace();
			new AlertDialog.Builder( getActivity() ).setTitle( "Error" ).setMessage( e.getMessage() ).show();
		}
		loadEffects();
	}
	
	@Override
	protected void onEffectsLoaded( MoaLiteEffectPack effects ) {
		super.onEffectsLoaded( effects );
		
		if( null != effects ) {
			mApplyButton.setEnabled( true );
			mExtractButton.setEnabled( false );
		}
	}
	
	@Override
	protected MoaLiteEffectPack loadDefaultEffects() {
		try {
			return loadEffectsFromFile( new File( mEffectsFolder, MoaLiteEffectPack.INDEX_FILENAME ) );
		} catch ( FileNotFoundException e ) {
			e.printStackTrace();
		} catch ( IOException e ) {
			e.printStackTrace();
		} catch ( JSONException e ) {
			e.printStackTrace();
		}
		return null;
	}


	protected void onApplySelectedEffect() {
		if ( null != mDefaultEffects ) {
			int position = mSpinner.getSelectedItemPosition();
			SimpleEffectFromFileRenderTask task = new SimpleEffectFromFileRenderTask();
			task.execute( position );
		}
	}


	/**
	 * Render the selected effect in a separate thread
	 * @author alessandro
	 * 
	 */
	class SimpleEffectFromFileRenderTask extends BaseRenderTask<Integer, Void, Boolean> {

		@Override
		protected Boolean doInBackground( Integer... params ) {
			MoaLiteEffectItem item = mDefaultEffects.getItemAt( params[0] );

			try {
				// Instead of using the asset's default_effects.zip as input stream, we just
				// load the target effect file
				File file = new File( mEffectsFolder, item.getRef() );
				
				// here we load the effect content using the path of the previously extracted file
				byte[] content = item.loadContent( file );

				// finally apply the effect to the preview bitmap, using the originalbitmap as source input
				return AviaryEffect.applyEffect( mOriginalBitmap, mPreviewBitmap, content );

			} catch ( IOException e ) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute( Boolean result ) {
			super.onPostExecute( result );

			// invalidate the image to refresh the bitmap
			mImageView.postInvalidate();
		}
	}

	/**
	 * Async load the main bitmap
	 * 
	 * @author alessandro
	 * 
	 */
	class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			onImageLoadStart();
		}

		@Override
		protected Bitmap doInBackground( String... params ) {
			return loadBitmapFromAssets( params[0] );
		}

		@Override
		protected void onPostExecute( Bitmap result ) {
			if ( null != result ) {
				onImageLoadComplete( result );
			}
		}
	}
}
