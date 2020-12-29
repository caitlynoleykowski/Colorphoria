package com.oo0o0o.colorphoria;

import android.graphics.Bitmap;

import com.oo0o0o.colorphoria.effects.OutlineEffectDark;

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
public class OutlineEffectBaseTest {

    final static int [] pixelArray =
            {
                    0x334433, 0x334433, 0x334433, 0x334433, 0xeeffff, 0xeeffff, 0xeeffff,
                    0x334433, 0x334433, 0x334433, 0x334433, 0xeeffff, 0xeeffff, 0xeeffff,
                    0x334433, 0x334433, 0x334433, 0x334433, 0xeeffff, 0xeeffff, 0xeeffff,
                    0x334433, 0x334433, 0x334433, 0xeeffff, 0xeeffff, 0xeeffff, 0xeeffff,
                    0x334433, 0x334433, 0x334433, 0xeeffff, 0xeeffff, 0xeeffff, 0xeeffff,
                    0x334433, 0x334433, 0x334433, 0xeeffff, 0xeeffff, 0xeeffff, 0xeeffff,
                    0x334433, 0x334433, 0x334433, 0xeeffff, 0xeeffff, 0xeeffff, 0xeeffff,
                    0x334433, 0x334433, 0x334433, 0xeeffff, 0xeeffff, 0xeeffff, 0xeeffff,
            };

    final static int [][] expectedPixelArray =
            {
                    {0x000000, 0x000000, 0x000000, 0x000000, 0xffffff, 0x000000, 0x000000},
                    {0x000000, 0x000000, 0x000000, 0x000000, 0xffffff, 0x000000, 0x000000},
                    {0x000000, 0x000000, 0x000000, 0x000000, 0xffffff, 0x000000, 0x000000},
                    {0x000000, 0x000000, 0x000000, 0xffffff, 0x000000, 0x000000, 0x000000},
                    {0x000000, 0x000000, 0x000000, 0xffffff, 0x000000, 0x000000, 0x000000},
                    {0x000000, 0x000000, 0x000000, 0xffffff, 0x000000, 0x000000, 0x000000},
                    {0x000000, 0x000000, 0x000000, 0xffffff, 0x000000, 0x000000, 0x000000},
                    {0x000000, 0x000000, 0x000000, 0xffffff, 0x000000, 0x000000, 0x000000}
            };

    @Test
    public void testOutlineEffect() throws Exception {
        Bitmap bitmap = Bitmap.createBitmap(pixelArray, 7, 8, Bitmap.Config.ARGB_8888);
        shadowOf(bitmap).setMutable(true);
        new OutlineEffectDark().applyEffect(bitmap, .5, 8, null, 0); // randomseed / colorPalette not used for threshold

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
