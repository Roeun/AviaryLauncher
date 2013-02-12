package com.aviary.headlessdemo;

import java.io.IOException;
import java.io.InputStream;
import org.json.JSONException;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import com.aviary.android.feather.headless.AviaryEffect;
import com.aviary.android.feather.headless.moa.moalite.MoaLiteEffectPack;
import com.aviary.android.feather.headless.moa.moalite.MoaLiteEffectPack.MoaLiteEffectItem;

public class SimpleEffectFromAssets extends BaseFragment implements OnClickListener {

	protected ImageView mImageView;
	protected Spinner mSpinner;
	protected Button mApplyButton;

	// we store the original bitmap in order to apply an effect always on the original bitmap
	protected Bitmap mOriginalBitmap;

	// This is the target bitmap, the result of an effect will be stored in this bitmap
	protected Bitmap mPreviewBitmap;

	// This will contains all the info about the effects loaded from a .zip file
	protected MoaLiteEffectPack mDefaultEffects;
	
	@Override
	public CharSequence getTitle() {
		return "Simple effect";
	}

	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		if ( container == null ) {
			return null;
		}
		return inflater.inflate( R.layout.tab1_frag_layout, container, false );
	}

	@Override
	public void onActivityCreated( Bundle savedInstanceState ) {
		super.onActivityCreated( savedInstanceState );
		initializeUI();
	}
	
	protected void initializeUI() {
		View view;
		
		view = getView().findViewById( R.id.imageView1 );
		if( null != view ) mImageView = (ImageView)view;

		view = getView().findViewById( R.id.spinner1 );
		if( null != view ) mSpinner = (Spinner) view;
		
		view = getView().findViewById( R.id.button1 );
		if( null != view ) {
			mApplyButton = (Button)view;
			mApplyButton.setOnClickListener( this );
		}
	}
	
	@Override
	public void onStart() {
		Log.i( LOG_TAG, "onStart" );
		super.onStart();
		onStarted();
	}
	
	@Override
	public void onStop() {
		Log.i( LOG_TAG, "onStop" );
		super.onStop();
		onStopped();
	}
	
	@Override
	protected void onStopped() {
		if( null != mImageView )
			mImageView.setImageBitmap( null );
		
		DecodeUtils.recycleBitmap( mOriginalBitmap );
		DecodeUtils.recycleBitmap( mPreviewBitmap );
		mDefaultEffects = null;
	}

	@Override
	protected void onStarted() {
		loadEffects();
		loadImage();
	}

	/**
	 * Loads the list of available effects from the asset file "default_effects.zip"
	 * and populate the spinner widget
	 */
	protected void loadEffects() {
		mDefaultEffects = loadDefaultEffects();
		onEffectsLoaded( mDefaultEffects );
	}
	
	protected MoaLiteEffectPack loadDefaultEffects() {
		try {
			return loadEffectsFromAssets( BASIC_EFFECTS_FILENAME );
		} catch ( IOException e ) {
			e.printStackTrace();
		} catch ( JSONException e ) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected void onEffectsLoaded( MoaLiteEffectPack effects ) {
		
		// populate the spinner with the list of available effects
		if ( null != effects && null != mSpinner ) {

			String items[] = new String[effects.getCount()];
			for ( int i = 0; i < effects.getCount(); i++ ) {
				items[i] = effects.getItemAt( i ).getDisplayName();
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>( getActivity(), android.R.layout.simple_spinner_item, items );
			adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
			mSpinner.setAdapter( adapter );
		}
	}

	void loadImage() {
		LoadImageTask task = new LoadImageTask();
		task.execute( TEST_ASSET_IMAGE );
	}
	

	protected void onApplySelectedEffect() {
		if ( null != mDefaultEffects ) {
			int position = mSpinner.getSelectedItemPosition();
			RenderTask task = new RenderTask();
			task.execute( position );
		}
	}

	@Override
	protected void onImageLoadComplete( Bitmap bitmap ) {
		onBitmapChanged( bitmap );
	}

	void onBitmapChanged( Bitmap bitmap ) {
		if ( null != mOriginalBitmap && !mOriginalBitmap.isRecycled() ) {
			if ( !mOriginalBitmap.equals( bitmap ) ) {
				mOriginalBitmap.recycle();
			}
		}

		if ( null != mPreviewBitmap && !mPreviewBitmap.isRecycled() ) {
			mPreviewBitmap.recycle();
		}

		mOriginalBitmap = bitmap;
		mPreviewBitmap = mOriginalBitmap.copy( mOriginalBitmap.getConfig(), true );
		setImageBitmap( mPreviewBitmap );
	}

	void setImageBitmap( Bitmap bitmap ) {
		if ( null != bitmap ) {
			Log.d( LOG_TAG, "bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight() );
			Log.d( LOG_TAG, "bitmap config is: " + bitmap.getConfig() );
		}
		mImageView.setImageBitmap( bitmap );
		mImageView.startAnimation( AnimationUtils.loadAnimation( getActivity(), android.R.anim.fade_in ) );
	}


	/**
	 * Render the selected effect in a separate thread
	 * @author alessandro
	 *
	 */
	class RenderTask extends BaseRenderTask<Integer, Void, Boolean> {

		@Override
		protected Boolean doInBackground( Integer... params ) {
			MoaLiteEffectItem item = mDefaultEffects.getItemAt( params[0] );

			InputStream stream;
			try {
				// open the stream from the "default_effects.zip" file
				stream = getResources().getAssets().open( BASIC_EFFECTS_FILENAME );
				
				// pass the stream to the MoaLiteEffectItem object to load the content of the effect
				byte[] content = item.loadContentFromZip( stream );

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

	@Override
	public void onClick( View v ) {
		if ( v.getId() == mApplyButton.getId() ) {
			onApplySelectedEffect();
		}
	}
}
