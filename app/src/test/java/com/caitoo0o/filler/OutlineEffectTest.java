package com.caitoo0o.filler;

import android.graphics.Bitmap;

import com.caitoo0o.filler.effects.OutlineEffect;
import com.caitoo0o.filler.effects.ThresholdEffect;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

/**
 * Created by caitoo0o on 4/17/2018.
 */
@RunWith(RobolectricTestRunner.class)
public class OutlineEffectTest {

    final static int [] pixelArray =
            {
                    0x778844, 0x778844, 0x778844, 0x778844, 0x114422, 0x114422, 0x114422,
                    0x778844, 0x778844, 0x778844, 0x778844, 0x114422, 0x114422, 0x114422,
                    0x778844, 0x778844, 0x778844, 0x778844, 0x114422, 0x114422, 0x114422,
                    0x778844, 0x778844, 0x778844, 0xcceedd, 0xcceedd, 0x114422, 0x114422,
                    0x778844, 0x778844, 0x778844, 0xcceedd, 0xcceedd, 0xcceedd, 0x114422,
                    0x778844, 0x778844, 0x778844, 0xcceedd, 0xcceedd, 0xcceedd, 0x114422,
                    0x778844, 0x778844, 0x778844, 0xcceedd, 0xcceedd, 0xcceedd, 0x114422,
                    0x778844, 0x778844, 0x778844, 0xcceedd, 0xcceedd, 0xcceedd, 0x114422,
            };

    final static int [][] expectedPixelArray =
            {
                    {0x778844, 0x778844, 0x778844, 0x778844, 0x114422, 0x114422, 0x114422},
                    {0x778844, 0x000000, 0x778844, 0x778844, 0x114422, 0x000000, 0x114422},
                    {0x778844, 0x000000, 0x778844, 0x778844, 0x114422, 0x000000, 0x114422},
                    {0x778844, 0x000000, 0x778844, 0xcceedd, 0xcceedd, 0x114422, 0x114422},
                    {0x778844, 0x000000, 0x778844, 0xcceedd, 0x000000, 0xcceedd, 0x114422},
                    {0x778844, 0x000000, 0x778844, 0xcceedd, 0x000000, 0xcceedd, 0x114422},
                    {0x778844, 0x000000, 0x778844, 0xcceedd, 0x000000, 0xcceedd, 0x114422},
                    {0x778844, 0x778844, 0x778844, 0xcceedd, 0xcceedd, 0xcceedd, 0x114422}
            };

    @Test
    public void testOutlineEffect() throws Exception {
        Bitmap bitmap = Bitmap.createBitmap(pixelArray, 7, 8, Bitmap.Config.ARGB_8888);
        shadowOf(bitmap).setMutable(true);
        new OutlineEffect().applyEffect(bitmap, .9, 8, null, 0); // randomseed / colorPalette not used for threshold

        // Check that the black pixels are black
        for (int i = 0; i < expectedPixelArray.length; i++) {
            for (int j = 0; j < expectedPixelArray[i].length; j++) {
                int pixelValue = bitmap.getPixel(j, i);
                int expectedValue = expectedPixelArray[i][j];
                if (expectedPixelArray[i][j] == 0x000000) {
                    assertTrue(pixelValue == expectedValue);
                } else {
                    assertTrue(pixelValue != 0x000000);
                }
            }
        }
    }
}
