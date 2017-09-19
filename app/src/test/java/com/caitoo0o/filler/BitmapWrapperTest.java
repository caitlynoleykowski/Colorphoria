package com.caitoo0o.filler;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.caitoo0o.filler.effects.BitmapWrapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(RobolectricTestRunner.class)
public class BitmapWrapperTest {

    public class BitmapWrapperExposed extends BitmapWrapper {
        public int getMaxPixels() { return MAX_PIXELS; }
    }

    BitmapWrapperExposed bmwe = new BitmapWrapperExposed();

    @Test
    public void testGetScaleFactorZero() throws Exception {
        doGetScaleFactorTest(0, 0);
    }

    @Test
    public void testGetScaleFactorSmallNumber() throws Exception {
        doGetScaleFactorTest(100, 100);
    }

    @Test
    public void testGetScaleFactorLargeNumber() throws Exception {
        doGetScaleFactorTest(100000000, 100000000);
    }

    private void doGetScaleFactorTest(int width, int height) {
        double scaleFactor = BitmapWrapper.getScaleFactor(width, height);

        int pixels = width * height;
        if (pixels <= bmwe.getMaxPixels()) {
            assertTrue(scaleFactor == 1.0);
        } else {
            assertTrue(scaleFactor > 0);
            assertTrue(scaleFactor < 1);
        }
    }

    @Test
    public void testRandomBleedFillBasic() throws Exception {
        Bitmap testPic1 = BitmapFactory.decodeResource(Robolectric.application.getResources(), R.drawable.test_pic_1);
        BitmapWrapper bmw = new BitmapWrapper(testPic1);
        bmw.randomBleedFill(.5, 8, null);
        assertNotNull(bmw.getSourceBitmap());
        assertNotNull(bmw.getModifiedBitmap());
        assertTrue(bmw.getSourceBitmap() != bmw.getModifiedBitmap());
        assertNotEquals(bmw.getSourceBitmap(), bmw.getModifiedBitmap());
    }

    @Test
    public void testThresholdBasic() throws Exception {
        Bitmap testPic1 = BitmapFactory.decodeResource(Robolectric.application.getResources(), R.drawable.test_pic_1);
        BitmapWrapper bmw = new BitmapWrapper(testPic1);
        bmw.threshold(.5);
        assertNotNull(bmw.getSourceBitmap());
        assertNotNull(bmw.getModifiedBitmap());
        assertTrue(bmw.getSourceBitmap() != bmw.getModifiedBitmap());
        assertNotEquals(bmw.getSourceBitmap(), bmw.getModifiedBitmap());
    }

    @Test
    public void testRandomLineFillBasic() throws Exception {
        Bitmap testPic1 = BitmapFactory.decodeResource(Robolectric.application.getResources(), R.drawable.test_pic_1);
        BitmapWrapper bmw = new BitmapWrapper(testPic1);
        bmw.randomLineFill(.5, 8, null);
        assertNotNull(bmw.getSourceBitmap());
        assertNotNull(bmw.getModifiedBitmap());
        assertTrue(bmw.getSourceBitmap() != bmw.getModifiedBitmap());
        assertNotEquals(bmw.getSourceBitmap(), bmw.getModifiedBitmap());
    }

    @Test
    public void testRandomFloodFillBasic() throws Exception {
        Bitmap testPic1 = BitmapFactory.decodeResource(Robolectric.application.getResources(), R.drawable.test_pic_1);
        BitmapWrapper bmw = new BitmapWrapper(testPic1);
        bmw.randomFloodFill(.5, 8, null);
        assertNotNull(bmw.getSourceBitmap());
        assertNotNull(bmw.getModifiedBitmap());
        assertTrue(bmw.getSourceBitmap() != bmw.getModifiedBitmap());
        assertNotEquals(bmw.getSourceBitmap(), bmw.getModifiedBitmap());
    }
}