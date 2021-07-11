package com.example.mobilebptesting;

public class ImageProcessing {

    int sum_red = 0;
    int sum_blue = 0;
    int sum_green = 0;
    int frameSize = 1;

    public void decodeYUV420SPtoRGB(byte[] yuv420sp, int width, int height) {

        frameSize = width * height;

        sum_red = 0;
        sum_blue = 0;
        sum_green = 0;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & yuv420sp[yp]) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                int pixel = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
                int red = (pixel >> 16) & 0xff;
                sum_red += red;

                int green = (pixel >> 8) & 0xff;
                sum_green += green;

                int blue = (pixel) & 0xff;
                sum_blue += blue;
            }
        }
    }

    /**
     * Returns the value of Red, Green, or Blue values by dividing sum previously
     * calculated in decodeYUV420SPtoRGB
     */

    public float getRed() {

        return ((float)sum_red / (float)frameSize);
    }

    public float getGreen() {

        return ((float)sum_green / (float)frameSize);
    }

    public float getBlue() {

        return ((float)sum_blue / (float)frameSize);
    }
}
