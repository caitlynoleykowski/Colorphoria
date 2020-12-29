package com.oo0o0o.colorphoria;

import android.graphics.Bitmap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

import com.oo0o0o.colorphoria.effects.RandomFloodFillEffect;

/**
 * Created by oo0o0o on 4/17/2018.
 */
@RunWith(RobolectricTestRunner.class)
public class RandomFloodFillEffectTest {

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
    public void testRandomFloodFill() throws Exception {
        Bitmap bitmap = Bitmap.createBitmap(pixelArray, 8, 8, Bitmap.Config.ARGB_8888);
        shadowOf(bitmap).setMutable(true);
        new RandomFloodFillEffect().applyEffect(bitmap, .5, 8, null, 0); // randomseed / colorPalette not used for threshold

        // Check that the black pixels are black
        for (int i = 0; i < expectedThresholdPixelArray.length; i++) {
            for (int j = 0; j < expectedThresholdPixelArray[i].length; j++) {
                int pixelValue = bitmap.getPixel(j,i);
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
        int expectedColor = bitmap.getPixel(x,y);
        for (int [] coordinate: section1) {
            y = coordinate[0];
            x = coordinate[1];
            int pixelColor = bitmap.getPixel(x, y);
            assertTrue(pixelColor == expectedColor);
        }
        firstCoordinate = section2[0];
        y = firstCoordinate[0];
        x = firstCoordinate[1];
        expectedColor = bitmap.getPixel(x,y);
        for (int [] coordinate: section2) {
            y = coordinate[0];
            x = coordinate[1];
            int pixelColor = bitmap.getPixel(x, y);
            assertTrue(pixelColor == expectedColor);
        }
        firstCoordinate = section3[0];
        y = firstCoordinate[0];
        x = firstCoordinate[1];
        expectedColor = bitmap.getPixel(x,y);
        for (int [] coordinate: section3) {
            y = coordinate[0];
            x = coordinate[1];
            int pixelColor = bitmap.getPixel(x, y);
            assertTrue(pixelColor == expectedColor);
        }
        firstCoordinate = section4[0];
        y = firstCoordinate[0];
        x = firstCoordinate[1];
        expectedColor = bitmap.getPixel(x,y);
        for (int [] coordinate: section4) {
            y = coordinate[0];
            x = coordinate[1];
            int pixelColor = bitmap.getPixel(x, y);
            assertTrue(pixelColor == expectedColor);
        }
    }
}
