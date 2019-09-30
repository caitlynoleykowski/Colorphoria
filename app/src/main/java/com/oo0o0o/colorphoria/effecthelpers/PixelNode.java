package com.oo0o0o.colorphoria.effecthelpers;

import android.graphics.Bitmap;

/**
 * Created by Cait on 8/14/2016.
 */
public class PixelNode {
    int x;
    int y;

    public PixelNode(int x, int y) {
        this.x = x;
        this.y = y;

    }
    public int x() { return x; }
    public int y() { return y; }
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public boolean isPixelNodeProcessed(boolean [][] processed) {
        return processed[x()][y()];
    }
    public void markPixelNodeProcessed(boolean [][]processed) {
        processed[x()][y()] = true;
    }
    public PixelNode getNorth(Bitmap bm) {
        PixelNode node = new PixelNode(x(), y() - 1);
        return node.isWithinBounds(bm) ? node : null;
    }
    public PixelNode getSouth(Bitmap bm) {
        PixelNode node = new PixelNode(x(), y() + 1);
        return node.isWithinBounds(bm) ? node : null;
    }
    public PixelNode getEast(Bitmap bm) {
        PixelNode node = new PixelNode(x() + 1, y());
        return node.isWithinBounds(bm) ? node : null;
    }
    public PixelNode getWest(Bitmap bm) {
        PixelNode node = new PixelNode(x() - 1, y());
        return node.isWithinBounds(bm) ? node : null;
    }
    public PixelNode getNorthWest(Bitmap bm) {
        PixelNode node = new PixelNode(x() - 1, y() - 1);
        return node.isWithinBounds(bm) ? node : null;
    }
    public PixelNode getSouthWest(Bitmap bm) {
        PixelNode node = new PixelNode(x() - 1, y() + 1);
        return node.isWithinBounds(bm) ? node : null;
    }
    public PixelNode getNorthEast(Bitmap bm) {
        PixelNode node = new PixelNode(x() + 1, y() - 1);
        return node.isWithinBounds(bm) ? node : null;
    }
    public PixelNode getSouthEast(Bitmap bm) {
        PixelNode node = new PixelNode(x() + 1, y() + 1);
        return node.isWithinBounds(bm) ? node : null;
    }
    private boolean isWithinBounds(Bitmap bm) {
        if (x() < bm.getWidth() && x() >= 0 && y() < bm.getHeight() && y() >= 0) {
            return true;
        } else {
            return false;
        }
    }
}