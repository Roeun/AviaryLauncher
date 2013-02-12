package com.aviary.headlessdemo;

import java.util.List;
import java.util.Vector;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Window;
import com.aviary.android.feather.headless.AviaryEffect;
import com.aviary.android.feather.headless.filters.NativeFilterProxy.AviaryInitError;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;

/**
 * See the {@link #initializeAviaryLibrary()} method
 * 
 * @author alessandro
 * 
 */
public class MainActivity extends FragmentActivity {

	/**
	 * This is your API-KEY. Don't use this key, but instead grab a new one from http://aviary.com/android
	 * */
	public static final String API_KEY = "2BUJB0-JMkuOS9lmUf1Z5g";

	@SuppressWarnings("unused")
	private static final String LOG_TAG = "main";

	private ViewPager mViewPager;
	private PagerAdapter mPagerAdapter;

	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	   requestWindowFeature(Window.FEATURE_PROGRESS);
	    
		setContentView( R.layout.activity_main );
		
		setProgressBarIndeterminateVisibility(false);
		setProgressBarVisibility(false);

		initializeAviaryLibrary();
		intializeAdapters();
	}

	private void initializeAviaryLibrary() {

		// This is a one time inizialization method. You need to init the AviaryEffect
		// library only once per Context.
		AviaryInitError error = AviaryEffect.init( getBaseContext(), API_KEY );

		if ( error != AviaryInitError.NoError ) {
			// if there is an error initializing the library, show an error dialog
			new AlertDialog.Builder( this ).setIcon( android.R.drawable.ic_dialog_alert ).setTitle( "Init Error" ).setMessage( "Error initializing the Aviary libraries: " + error.name() ).show();
		}
	}

	private void intializeAdapters() {
		mViewPager = (ViewPager) super.findViewById( R.id.pager );

		List<BaseFragment> fragments = new Vector<BaseFragment>();
		fragments.add( (BaseFragment) Fragment.instantiate( this, SimpleEffectFromAssets.class.getName() ) );
		fragments.add( (BaseFragment) Fragment.instantiate( this, SimpleEffectFromFile.class.getName() ) );
		fragments.add( (BaseFragment) Fragment.instantiate( this, ScaledBitmapFragment.class.getName() ) );
		fragments.add( (BaseFragment) Fragment.instantiate( this, MultipleEffectsFromAsset.class.getName() ) );
		fragments.add( (BaseFragment) Fragment.instantiate( this, SimpleGridFragment.class.getName() ) );
		fragments.add( (BaseFragment) Fragment.instantiate( this, AdvancedGridFragment.class.getName() ) );
		mPagerAdapter = new PagerAdapter( getSupportFragmentManager(), fragments );

		mViewPager.setAdapter( this.mPagerAdapter );

		//TabPageIndicator indicator = (TabPageIndicator) findViewById( R.id.indicator );
      TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
      indicator.setViewPager(mViewPager);
      indicator.setFooterIndicatorStyle(IndicatorStyle.Underline);
	}
}
