package com.aviary.headlessdemo;

import java.io.InputStream;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.BitmapFactory.Options;

public class DecodeUtils {

	public static Rect decodeImageBounds( InputStream stream ) {
		Options options = new Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeStream( stream, null, options );
		return new Rect( 0, 0, options.outWidth, options.outHeight );
	}

	static int getSampleSize( Context context, int imageWidth, int imageHeight ) {
		int imageSize = Math.max( imageWidth, imageHeight );
		int screenSize = Math.min( context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels );
		int sampleSize = 1;

		if ( imageSize > screenSize ) {
			sampleSize = (int) Math.ceil( (double) imageSize / screenSize );
		}
		return sampleSize;
	}
	
	public static Bitmap loadBitmap( InputStream stream, int sampleSize ) {
		Options options = new Options();
		options.inSampleSize = sampleSize;
		options.inDither = true;
		options.inPurgeable = true;
		return BitmapFactory.decodeStream( stream, null, options );
	}
	
	public static void recycleBitmap( Bitmap bitmap ) {
		if( null != bitmap && !bitmap.isRecycled() ) {
			bitmap.recycle();
		}
	}	
}
