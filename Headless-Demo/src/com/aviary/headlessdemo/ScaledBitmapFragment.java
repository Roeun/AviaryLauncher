package com.aviary.headlessdemo;

import android.graphics.Bitmap;

/**
 * Apply an effect on a destination Bitmap which is half the size
 * of the original bitmap
 * 
 * @author alessandro
 *
 */
public class ScaledBitmapFragment extends SimpleEffectFromAssets {
	
	@Override
	public CharSequence getTitle() {
		return "Scaled";
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

		// Create a scaled version of the original bitmap
		mPreviewBitmap = Bitmap.createScaledBitmap( mOriginalBitmap, mOriginalBitmap.getWidth()/2, mOriginalBitmap.getHeight()/2, false );
		setImageBitmap( mOriginalBitmap );
	}
	
	protected void onApplySelectedEffect() {
		if ( null != mDefaultEffects ) {
			int position = mSpinner.getSelectedItemPosition();
			RenderScaledTask task = new RenderScaledTask();
			task.execute( position );
		}
	}	

	/**
	 * Render the selected effect in a separate thread
	 * @author alessandro
	 *
	 */
	class RenderScaledTask extends RenderTask {

		@Override
		protected void onPostExecute( Boolean result ) {
			super.onPostExecute( result );

			if( result ) {
				mImageView.setImageBitmap( mPreviewBitmap );
				mImageView.postInvalidate();
			}
		}
	}
}
