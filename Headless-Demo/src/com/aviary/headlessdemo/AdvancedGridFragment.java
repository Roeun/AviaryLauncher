package com.aviary.headlessdemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import org.json.JSONException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import com.aviary.android.feather.headless.AviaryEffect;
import com.aviary.android.feather.headless.moa.moalite.MoaLiteEffectPack;
import com.aviary.android.feather.headless.moa.moalite.MoaLiteEffectPack.MoaLiteEffectItem;

public class AdvancedGridFragment extends SimpleGridFragment {

	private GridView mGridView;
	private Button mExtractButton;
	
	private int totalCells = 1;
	
	private File mEffectsDir;
	
	
	@Override
	public CharSequence getTitle() {
		return "Advanced Grid";
	}

	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		if ( container == null ) return null;
		return inflater.inflate( R.layout.grid2_frag_layout, container, false );
	}

	@Override
	protected void initializeUI() {
		super.initializeUI();
		mGridView = (GridView) getView().findViewById( R.id.gridView1 );
		mExtractButton = (Button) getView().findViewById( R.id.extract_button );
		mExtractButton.setOnClickListener( this );
	}
	
	@Override
	public void onClick( View v ) {
		super.onClick( v );
		
		if( v.getId() == mExtractButton.getId() ) {
			loadEffects();
		}
	}
	
	@Override
	protected MoaLiteEffectPack loadDefaultEffects() {
		try {
			mEffectsDir = extractEffects();
		} catch ( IOException e ) {
			e.printStackTrace();
			return null;
		}
		
		try {
			return loadEffectsFromFile( new File( mEffectsDir, MoaLiteEffectPack.INDEX_FILENAME ) );
		} catch ( FileNotFoundException e ) {
			e.printStackTrace();
		} catch ( IOException e ) {
			e.printStackTrace();
		} catch ( JSONException e ) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	protected void onStarted() {
		mApplyButton.setEnabled( false );
		mExtractButton.setEnabled( true );
		loadImage();
	}
	
	@Override
	protected void onStopped() {
		mGridView.setAdapter( null );
		super.onStopped();
	}

	@Override
	void setImageBitmap( Bitmap bitmap ) {
		mGridView.setAdapter( new GridAdapter( bitmap, totalCells, false ) );
	}

	@Override
	protected void onEffectsLoaded( MoaLiteEffectPack effects ) {

		if ( null != effects ) {
			mApplyButton.setEnabled( true );
			mExtractButton.setEnabled( false );
			totalCells = getMaxSquare( effects.getCount() );
			if ( totalCells > 0 ) {
				int numCols = (int) Math.sqrt( totalCells );
				mGridView.setAdapter( new GridAdapter( mPreviewBitmap, totalCells, false ) );
				mGridView.setNumColumns( numCols );
			}
		}
		super.onEffectsLoaded( effects );
	}

	@Override
	protected void onApplySelectedEffect() {
		if ( totalCells > 0 ) {
			RenderTask task = new RenderTask();
			task.execute( totalCells );
		} else {
			Log.e( LOG_TAG, "Insufficient number of cells" );
		}
	}
	
	/**
	 * Create a File array with the content of the effects we want to apply
	 * @param size
	 * @return
	 * @throws IOException
	 */
	protected File[] getEffectsFileList( int size ) throws IOException {
		if ( size < 1 ) return null;

		int total = mDefaultEffects.getCount();
		File[] files = new File[size];
		Random random = new Random();

		for ( int i = 0; i < size; i++ ) {
			MoaLiteEffectItem item = mDefaultEffects.getItemAt( random.nextInt( total ) );
			files[i] = new File( mEffectsDir, item.getRef() );
		}
		return files;
	}	

	class RenderTask extends BaseRenderTask<Integer, Void, Boolean> {

		@Override
		protected Boolean doInBackground( Integer... params ) {

			File[] effects;
			try {
				effects = getEffectsFileList( params[0] );
			} catch ( IOException e ) {
				e.printStackTrace();
				return false;
			}

			return AviaryEffect.applyGrid( mOriginalBitmap, mPreviewBitmap, effects );
		}

		@Override
		protected void onPostExecute( Boolean result ) {
			super.onPostExecute( result );
			mGridView.setAdapter( new GridAdapter( mPreviewBitmap, totalCells, true ) );
		}
	}

	class GridAdapter extends BaseAdapter {

		Bitmap mBitmap;
		int mRoot;
		int nCells;
		int mCellWidth;
		int mCellHeight;
		float mScaleX, mScaleY;
		boolean mTranslate;

		Paint paint = new Paint();
		Matrix matrix = new Matrix();
		Canvas canvas = new Canvas();

		public GridAdapter( Bitmap bitmap, int i, boolean translate ) {
			mBitmap = bitmap;
			nCells = i;

			int root = (int) Math.sqrt( i );
			mCellWidth = bitmap.getWidth() / root;
			mCellHeight = bitmap.getHeight() / root;
			mRoot = root;

			mScaleX = (float) mCellWidth / bitmap.getWidth();
			mScaleY = (float) mCellHeight / bitmap.getHeight();
			mTranslate = translate;
		}

		@Override
		public int getCount() {
			return nCells;
		}

		@Override
		public Object getItem( int position ) {
			int x = getX( position );
			int y = getY( position );

			Bitmap bitmap = Bitmap.createBitmap( mCellWidth, mCellHeight, mBitmap.getConfig() );
			canvas.setBitmap( bitmap );

			matrix.reset();

			if ( mTranslate ) {
				matrix.setTranslate( -x, -y );
			} else {
				matrix.setScale( mScaleX, mScaleY );
			}
			canvas.drawBitmap( mBitmap, matrix, paint );
			return bitmap;
		}

		int getX( int position ) {
			return mCellWidth * ( position % mRoot );
		}

		int getY( int position ) {
			return mCellHeight * ( position / mRoot );
		}

		@Override
		public long getItemId( int position ) {
			return 0;
		}

		@Override
		public View getView( int position, View convertView, ViewGroup parent ) {
			ImageView imageView = new ImageView( AdvancedGridFragment.this.getActivity() );
			imageView.setImageBitmap( (Bitmap) getItem( position ) );
			imageView.setScaleType( ImageView.ScaleType.CENTER_INSIDE );
			imageView.setLayoutParams( new GridView.LayoutParams( GridView.LayoutParams.WRAP_CONTENT, GridView.LayoutParams.WRAP_CONTENT ) );
			return imageView;
		}
	}
}
