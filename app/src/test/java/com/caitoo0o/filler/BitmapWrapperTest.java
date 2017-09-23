package com.caitoo0o.filler;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.caitoo0o.filler.effects.BitmapWrapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;
import static org.robolectric.Robolectric.shadowOf;

/**
 * Tests the BitmapWrapper class basic functions and its effects
 */
@RunWith(RobolectricTestRunner.class)
public class BitmapWrapperTest {

    // Override the BitmapWrapper class to expose some of its members/functions for purposes of the tests
    public class BitmapWrapperExposed extends BitmapWrapper {
        public int getMaxPixels() { return MAX_PIXELS; }
        public int getMaxCumDeltaOfAllColors() { return MAX_CUMULATIVE_DELTA_OF_ALL_COLORS; }

        // Can't make the functions of this inner class static so this is just used to access getMaxPixels
        public BitmapWrapperExposed() {
        }

        public BitmapWrapperExposed(Bitmap bitmap) {
            super(bitmap);
        }

        @Override
        protected Bitmap getRotatedSource() {
            int[] colors = new int[] {
                    Color.parseColor("#ff0000"), Color.parseColor("#00ff00"), Color.parseColor("#0000ff"),
                    Color.parseColor("#990000"), Color.parseColor("#009900"), Color.parseColor("#000099")
            };

            Bitmap bitmap = Bitmap.createBitmap(pixelArray, 8, 8, Bitmap.Config.ARGB_8888);
            shadowOf(bitmap).setMutable(true);
            return bitmap;
        }
    }

    BitmapWrapperExposed bmwe = new BitmapWrapperExposed();

    final static int [] pixelArray =
            {
                    0xcceedd, 0x778844, 0x112211, 0x000000, 0x778844, 0xffffff, 0x114422, 0x000000,
                    0xfefefe, 0x778844, 0x112211, 0x000000, 0x114422, 0xffffff, 0x000000, 0x114422,
                    0xffffff, 0xeeddee, 0x112211, 0x000000, 0x114422, 0x000000, 0x114422, 0xffffff,
                    0xfefefe, 0x778844, 0x000000, 0x000000, 0x000000, 0x114422, 0xffffff, 0xffffff,
                    0xcceedd, 0x000000, 0x112211, 0x114422, 0x114422, 0x000000, 0xffffff, 0xffffff,
                    0x000000, 0x778844, 0xeeddee, 0x112211, 0x114422, 0x000000, 0x114422, 0xffffff,
                    0x778844, 0xffffff, 0xffffff, 0xffffff, 0x112211, 0x114422, 0x000000, 0x112233,
                    0xffffff, 0xffffff, 0xffffff, 0xffffff, 0xffffff, 0x112211, 0x114422, 0x000000
            };

    final static int [][] expectedThresholdPixelArray =
            {
                    {0xffffff, 0x000000, 0x000000, 0x000000, 0x000000, 0xffffff, 0x000000, 0x000000},
                    {0xffffff, 0x000000, 0x000000, 0x000000, 0x000000, 0xffffff, 0x000000, 0x000000},
                    {0xffffff, 0xffffff, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0xffffff},
                    {0xffffff, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0xffffff, 0xffffff},
                    {0xffffff, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0xffffff, 0xffffff},
                    {0x000000, 0x000000, 0xffffff, 0x000000, 0x000000, 0x000000, 0x000000, 0xffffff},
                    {0x000000, 0xffffff, 0xffffff, 0xffffff, 0x000000, 0x000000, 0x000000, 0x000000},
                    {0xffffff, 0xffffff, 0xffffff, 0xffffff, 0xffffff, 0x000000, 0x000000, 0x000000}
            };

    final static int [][] section1 = { {0,0}, {1,0}, {2,0}, {3,0}, {4,0}, {2,1} };
    final static int [][] section2 = { {7,0}, {6,1}, {7,1}, {5,2}, {6,2}, {7,2}, {6,3}, {7,3}, {7,4} };
    final static int [][] section3 = { {0,5}, {1,5} };
    final static int [][] section4 = { {3,6}, {4,6}, {2,7}, {3,7}, {4,7}, {5,7} };

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

    @Test
    public void testThreshold() throws Exception {
        Bitmap bitmap = Bitmap.createBitmap(pixelArray, 8, 8, Bitmap.Config.ARGB_8888);
        BitmapWrapperExposed bmw = new BitmapWrapperExposed(bitmap);
        bmw.setModifiedBitmap(Bitmap.createBitmap(pixelArray, 8, 8, Bitmap.Config.ARGB_8888));
        bmw.threshold(.5);

        assertNotNull(bmw.getSourceBitmap());
        assertNotNull(bmw.getModifiedBitmap());
        assertTrue(bmw.getSourceBitmap() != bmw.getModifiedBitmap());
        for (int i = 0; i < expectedThresholdPixelArray.length; i++) {
            for (int j = 0; j < expectedThresholdPixelArray[i].length; j++) {
                int pixelValue = bmw.getSourceBitmap().getPixel(j,i);
                int expectedValue = expectedThresholdPixelArray[i][j];
                assertTrue(bmw.getModifiedBitmap().getPixel(j,i) == expectedThresholdPixelArray[i][j]);
            }
        }
    }

    @Test
    public void testRandomFloodFill() throws Exception {
        Bitmap bitmap = Bitmap.createBitmap(pixelArray, 8, 8, Bitmap.Config.ARGB_8888);
        BitmapWrapperExposed bmw = new BitmapWrapperExposed(bitmap);
        bmw.setModifiedBitmap(Bitmap.createBitmap(pixelArray, 8, 8, Bitmap.Config.ARGB_8888));
        bmw.randomFloodFill(.5, 1, null);

        assertNotNull(bmw.getSourceBitmap());
        assertNotNull(bmw.getModifiedBitmap());
        assertTrue(bmw.getSourceBitmap() != bmw.getModifiedBitmap());
        // Check that the black pixels are black
        for (int i = 0; i < expectedThresholdPixelArray.length; i++) {
            for (int j = 0; j < expectedThresholdPixelArray[i].length; j++) {
                int pixelValue = bmw.getModifiedBitmap().getPixel(j,i);
                int expectedValue = expectedThresholdPixelArray[i][j];
                if (expectedThresholdPixelArray[i][j] == 0x000000) {
                    assertTrue(pixelValue == expectedValue);
                }
            }
        }
        // Check that the sections are all the same color
        int [] firstCoordinate = section1[0];
        int y = firstCoordinate[0];
        int x = firstCoordinate[1];
        int expectedColor = bmw.getModifiedBitmap().getPixel(x,y);
        for (int [] coordinate: section1) {
            y = coordinate[0];
            x = coordinate[1];
            int pixelColor = bmw.getModifiedBitmap().getPixel(x, y);
            assertTrue(pixelColor == expectedColor);
        }
        firstCoordinate = section2[0];
        y = firstCoordinate[0];
        x = firstCoordinate[1];
        expectedColor = bmw.getModifiedBitmap().getPixel(x,y);
        for (int [] coordinate: section2) {
            y = coordinate[0];
            x = coordinate[1];
            int pixelColor = bmw.getModifiedBitmap().getPixel(x, y);
            assertTrue(pixelColor == expectedColor);
        }
        firstCoordinate = section3[0];
        y = firstCoordinate[0];
        x = firstCoordinate[1];
        expectedColor = bmw.getModifiedBitmap().getPixel(x,y);
        for (int [] coordinate: section3) {
            y = coordinate[0];
            x = coordinate[1];
            int pixelColor = bmw.getModifiedBitmap().getPixel(x, y);
            assertTrue(pixelColor == expectedColor);
        }
        firstCoordinate = section4[0];
        y = firstCoordinate[0];
        x = firstCoordinate[1];
        expectedColor = bmw.getModifiedBitmap().getPixel(x,y);
        for (int [] coordinate: section4) {
            y = coordinate[0];
            x = coordinate[1];
            int pixelColor = bmw.getModifiedBitmap().getPixel(x, y);
            assertTrue(pixelColor == expectedColor);
        }
    }
}