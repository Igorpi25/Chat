package com.ivanov.tech.chat.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivanov.tech.chat.R;

public class FragmentSplashScreen extends DialogFragment{
	private static final String TAG = FragmentSplashScreen.class
            .getSimpleName();   

	public static FragmentSplashScreen newInstance(){
		FragmentSplashScreen f=new FragmentSplashScreen();
    	
		return f;
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        
        view=inflater.inflate(R.layout.splash_screen, container, false);
         
        return view;
    }

}
