package com.oo0o0o.colorphoria;

import android.graphics.Bitmap;

import com.oo0o0o.colorphoria.effects.ThresholdEffect;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

/**
 * Created by oo0o0o on 4/17/2018.
 */
@RunWith(RobolectricTestRunner.class)
public class ThresholdEffectTest {

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

    @Test
    public void testThreshold() throws Exception {
        Bitmap bitmap = Bitmap.createBitmap(pixelArray, 8, 8, Bitmap.Config.ARGB_8888);
        shadowOf(bitmap).setMutable(true);
        new ThresholdEffect().applyEffect(bitmap, .5, 8, null, 0); // randomseed / colorPalette not used for threshold

        for (int i = 0; i < expectedThresholdPixelArray.length; i++) {
            for (int j = 0; j < expectedThresholdPixelArray[i].length; j++) {
                int pixelValue = bitmap.getPixel(j,i);
                int expectedValue = expectedThresholdPixelArray[i][j];
                assertTrue(pixelValue == expectedValue);
            }
        }
    }
}
