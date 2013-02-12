package com.aviary.headlessdemo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.aviary.android.feather.headless.AviaryEffect;
import com.aviary.android.feather.headless.moa.moalite.MoaLiteEffectPack.MoaLiteEffectItem;

/**
 * This sample will show how to apply multiple effects at once.
 * 
 * @author alessandro
 *
 */
public class MultipleEffectsFromAsset extends SimpleEffectFromAssets {

	protected Button mButton2;
	private Collection<Integer> mSelectedEffects;
	
	
	@Override
	public CharSequence getTitle() {
		return "Multiple";
	}

	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		if ( container == null ) {
			return null;
		}
		return inflater.inflate( R.layout.multieffects_frag_layout, container, false );
	}

	protected void initializeUI() {
		super.initializeUI();
		mButton2 = (Button) getView().findViewById( R.id.button2 );
		mButton2.setOnClickListener( this );
		
		// disable the apply button by default
		mApplyButton.setEnabled( false );
	}

	@Override
	public void onClick( View v ) {
		super.onClick( v );
		
		if( v.getId() == mButton2.getId() ) {
			onSelectEffects();
		}
	}
	
	private void onSelectEffects() {
		if( mDefaultEffects == null || mDefaultEffects.getCount() < 1 ) {
			return;
		}
		
		final HashMap<CharSequence, Integer> selectedEffects = new HashMap<CharSequence, Integer>();
		final CharSequence[] strings = new CharSequence[mDefaultEffects.getCount()];

		for ( int i = 0; i < mDefaultEffects.getCount(); i++ ) {
			strings[i] = mDefaultEffects.getItemAt( i ).getDisplayName();
		}

		boolean[] selected = new boolean[mDefaultEffects.getCount()];

		OnMultiChoiceClickListener listener = new OnMultiChoiceClickListener() {

			@Override
			public void onClick( DialogInterface dialog, int which, boolean isChecked ) {
				if ( !isChecked ) {
					selectedEffects.remove( strings[which] );
				} else {
					selectedEffects.put( strings[which], which );
				}
			}
		};

		DialogInterface.OnClickListener oklistener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick( DialogInterface dialog, int which ) {
				onEffectsSelectionChanged( selectedEffects.values() );
			}
		};

		Dialog dialog = new AlertDialog.Builder( getActivity() ).setMultiChoiceItems( strings, selected, listener ).setPositiveButton( android.R.string.ok, oklistener ).setNegativeButton( android.R.string.cancel, null ).create();
		dialog.show();		
	}
	
	private void onEffectsSelectionChanged( Collection<Integer> newSelection ) {
		mSelectedEffects = null;
		mApplyButton.setEnabled( false );
		if ( newSelection != null && newSelection.size() > 0 ) {
			mSelectedEffects = newSelection;
			mApplyButton.setEnabled( true );
		}
	}	
	
	@SuppressWarnings("unchecked")
	protected void onApplySelectedEffect() {
		if ( null != mDefaultEffects && null != mSelectedEffects && mSelectedEffects.size() > 0 ) {
			MultipleEffectsFromAssetRenderTask task = new MultipleEffectsFromAssetRenderTask();
			task.execute( mSelectedEffects );
		}
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

	/**
	 * Render the selected effect in a separate thread
	 * @author alessandro
	 *
	 */
	class MultipleEffectsFromAssetRenderTask extends BaseRenderTask<Collection<Integer>, Void, Boolean> {

		@Override
		protected Boolean doInBackground( Collection<Integer>... params ) {
			
			Collection<Integer> list = params[0];
			Iterator<Integer> iterator = list.iterator();
			

			byte[][] effects = new byte[list.size()][];

			InputStream stream;
			try {
				stream = getResources().getAssets().open( BASIC_EFFECTS_FILENAME );
			} catch ( IOException e ) {
				e.printStackTrace();
				return false;
			}
			
			BufferedInputStream buffer = new BufferedInputStream( stream );
			int bufferSize;
			int index = 0;
			
			try {
				bufferSize = buffer.available();
			} catch ( IOException e1 ) {
				e1.printStackTrace();
				return false;
			}
			
			while( iterator.hasNext() ) {
				buffer.mark( bufferSize );
				MoaLiteEffectItem item = mDefaultEffects.getItemAt( iterator.next().intValue() );
				
				Log.d( LOG_TAG, "loading content from: " + item.getDisplayName() );
				
				byte[] content;
				try {
					content = item.loadContentFromZip( buffer );
					Log.d( LOG_TAG, "content size: " + content.length );
				} catch ( IOException e ) {
					e.printStackTrace();
					return false;
				}
				
				effects[index++] = content;
				
				try {
					buffer.reset();
				} catch ( IOException e ) {
					e.printStackTrace();
					return false;
				}
			}
			
			return AviaryEffect.applyEffects( mOriginalBitmap, mPreviewBitmap, effects );
		}

		@Override
		protected void onPostExecute( Boolean result ) {
			super.onPostExecute( result );
			
			// invalidate the image to refresh the bitmap
			mImageView.postInvalidate();
		}
	}

}
