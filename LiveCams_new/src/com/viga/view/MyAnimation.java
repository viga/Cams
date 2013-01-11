package com.viga.view;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;

public class MyAnimation {
	public static void startAnimationIN(ViewGroup viewGroup, int duration){
		for(int i = 0; i < viewGroup.getChildCount(); i++ ){
			viewGroup.getChildAt(i).setVisibility(View.VISIBLE);
			viewGroup.getChildAt(i).setFocusable(true);
			viewGroup.getChildAt(i).setClickable(true);
		}
		
		Animation animation;
		animation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
		animation.setFillAfter(true);
		animation.setDuration(duration);
		
		viewGroup.startAnimation(animation);
		
	}
	
	public static void startAnimationOUT(final ViewGroup viewGroup, int duration , int startOffSet){
		Animation animation;
		animation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
		animation.setFillAfter(true);
		animation.setDuration(duration);
		animation.setStartOffset(startOffSet);
		animation.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {
				
			}
			
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			public void onAnimationEnd(Animation animation) {
				for(int i = 0; i < viewGroup.getChildCount(); i++ ){
					viewGroup.getChildAt(i).setVisibility(View.GONE);
					viewGroup.getChildAt(i).setFocusable(false);
					viewGroup.getChildAt(i).setClickable(false);
				}
				
			}
		});
		
		viewGroup.startAnimation(animation);
	}
}
