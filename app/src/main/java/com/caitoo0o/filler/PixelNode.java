package com.caitoo0o.filler;

/**
 * Created by Cait on 8/14/2016.
 */
class PixelNode {
    int x;
    int y;
    int width;
    int height;
    int idx;
    int [] pixels;
    boolean [] processed;

    PixelNode(int x, int y, int width, int height, int [] pixels, boolean [] processed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.idx = x + (y * width);
        this.pixels = pixels;
        this.processed = processed;
    }
    public int getColor() {
        return 0x00FFFFFF & pixels[idx];
    }
    public void setColor(int color) {
        pixels[idx] = color;
    }
    public boolean isProcessed() {
        return processed[idx];
    }
    public void markProcessed() {
        processed[idx] = true;
    }
    public PixelNode getNorth() {
        PixelNode node = new PixelNode(x, y - 1, width, height, pixels, processed);
        return node.isWithinBounds() ? node : null;
    }
    public PixelNode getSouth() {
        PixelNode node = new PixelNode(x, y + 1, width, height, pixels, processed);
        return node.isWithinBounds() ? node : null;
    }
    public PixelNode getEast() {
        PixelNode node = new PixelNode(x + 1, y, width, height, pixels, processed);
        return node.isWithinBounds() ? node : null;
    }
    public PixelNode getWest() {
        PixelNode node = new PixelNode(x - 1, y, width, height, pixels, processed);
        return node.isWithinBounds() ? node : null;
    }
    private boolean isWithinBounds() {
        if (x < width && x >= 0 && y < height && y >= 0) {
            return true;
        } else {
            return false;
        }
    }
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}