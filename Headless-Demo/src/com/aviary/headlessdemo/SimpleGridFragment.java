package com.aviary.headlessdemo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import com.aviary.android.feather.headless.AviaryEffect;
import com.aviary.android.feather.headless.moa.moalite.MoaLiteEffectPack.MoaLiteEffectItem;

/**
 * Using the original bitmap as input bitmap let's create an output Bitmap 
 * ( with the same size of the original bitmap )
 * on which we render a certain number of effects in order to create a "grid"
 * 
 * @author alessandro
 */
public class SimpleGridFragment extends SimpleEffectFromAssets {
	
	@Override
	public CharSequence getTitle() {
		return "Grid";
	}

	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		if ( container == null ) return null;
		return inflater.inflate( R.layout.tab2_frag_layout, container, false );
	}

	@Override
	protected void initializeUI() {
		mImageView = (ImageView) getView().findViewById( R.id.imageView1 );
		mApplyButton = (Button) getView().findViewById( R.id.button1 );
		mApplyButton.setOnClickListener( this );
	}
	
	protected int getMaxSquare( int value ) {
		if( value < 2 ) return 0;
		int c = (int) Math.floor( Math.sqrt( value ) );
		return c*c;
	}

	@Override
	protected void onApplySelectedEffect() {
		int totalCells = getMaxSquare( mDefaultEffects.getCount() );
		if( totalCells > 0 ) {
			RenderTask task = new RenderTask();
			task.execute( totalCells );
		} else {
			Log.e( LOG_TAG, "Insufficient number of cells" );
		}
	}

	/**
	 * Create a byte[][] array with the content of the effects we want to apply
	 * @param size
	 * @return
	 * @throws IOException
	 */
	protected byte[][] loadGridEffects( int size ) throws IOException {
		if ( size < 1 ) return null;

		int total = mDefaultEffects.getCount();
		byte[][] effects = new byte[size][];
		Random random = new Random();

		InputStream stream = getResources().getAssets().open( BASIC_EFFECTS_FILENAME );
		BufferedInputStream buffer = new BufferedInputStream( stream );

		for ( int i = 0; i < size; i++ ) {
			buffer.mark( buffer.available() );
			MoaLiteEffectItem item = mDefaultEffects.getItemAt( random.nextInt( total ) );
			byte[] content = item.loadContentFromZip( buffer );
			effects[i] = content;
			buffer.reset();
		}
		return effects;
	}

	class RenderTask extends BaseRenderTask<Integer, Void, Boolean> {

		@Override
		protected Boolean doInBackground( Integer... params ) {

			byte[][] effects;
			try {
				effects = loadGridEffects( params[0] );
			} catch ( IOException e ) {
				e.printStackTrace();
				return false;
			}
			
			// Pass an array of byte[] as input
			return AviaryEffect.applyGrid( mOriginalBitmap, mPreviewBitmap, effects );
		}

		@Override
		protected void onPostExecute( Boolean result ) {
			super.onPostExecute( result );
			mImageView.postInvalidate();
		}
	}
}
