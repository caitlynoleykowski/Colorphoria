package com.caitoo0o.filler.effects;

/**
 * Created by Cait on 8/14/2016.
 */
class PixelNode {
    int x;
    int y;

    PixelNode(int x, int y) {
        this.x = x;
        this.y = y;

    }
    public int x() { return x; }
    public int y() { return y; }
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}