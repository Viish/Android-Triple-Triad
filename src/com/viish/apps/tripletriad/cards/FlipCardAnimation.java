package com.viish.apps.tripletriad.cards;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/*  Copyright (C) <2011-2012>  <Sylvain "Viish" Berfini>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class FlipCardAnimation extends Animation 
{
	private final float mFromDegrees;
	private final float mToDegrees;
	private final float mCenterX;
	private final float mCenterY;
	private Camera mCamera;

	public FlipCardAnimation(float fromDegrees, float toDegrees, float centerX, float centerY) 
	{
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;
		mCenterX = centerX;
		mCenterY = centerY;
	}
	
	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) 
	{
		super.initialize(width, height, parentWidth, parentHeight);
		mCamera = new Camera();
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) 
	{
		final float fromDegrees = mFromDegrees;
		float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);
		
		final float centerX = mCenterX;
		final float centerY = mCenterY;
		final Camera camera = mCamera;
		
		final Matrix matrix = t.getMatrix();
		
		camera.save();
		
		camera.rotateY(degrees);
		
		camera.getMatrix(matrix);
		camera.restore();
		
		matrix.preTranslate(-centerX, -centerY);
		matrix.postTranslate(centerX, centerY);	
	}
}