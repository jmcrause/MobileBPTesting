package com.example.mobilebptesting;

public class FrameProcessor {

    private int sum_red = 0;
    private int sum_blue = 0;
    private int sum_green = 0;
    private int min_red = 255;
    private int min_blue = 255;
    private int min_green = 255;
    private int max_red = 0;
    private int max_blue = 0;
    private int max_green = 0;

    private float sd_red;
    private float sd_green;
    private float sd_blue;

    public int width = 0, height = 0;
    private int frameSize = 1;
    private int [] pixels;
    byte[] yuv420sp;
    double time;

    public void setFrame(byte[] frame, int frame_width, int frame_height, double t) {
        width = frame_width;
        height = frame_height;
        frameSize = width * height;
        yuv420sp = frame.clone();
        time = t;
    }

    public byte[] getFrame () {
        return yuv420sp;
    }

    public void decodeYUV420SPtoRGB() {

        pixels = new int[frameSize/4];

        sum_red = 0;
        sum_blue = 0;
        sum_green = 0;
        min_red = 255;
        min_blue = 255;
        min_green = 255;
        max_red = 0;
        max_blue = 0;
        max_green = 0;

        for (int j = 0, yp = 0; j < height; j+=2) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i+=2, yp++) {
                int y = (0xff & yuv420sp[yp*4]) - 16;
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

                if (yp >= frameSize/4) {
                    break;
                }

                int pixel = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
                pixels [yp] = pixel;
                int red = (pixel >> 16) & 0xff;
                sum_red += red;

                int green = (pixel >> 8) & 0xff;
                sum_green += green;

                int blue = (pixel) & 0xff;
                sum_blue += blue;


                if (red > max_red) {
                    max_red = red;
                }
                else if (red < min_red) {
                    min_red = red;
                }
                if (green > max_green) {
                    max_green = green;
                }
                else if (green < min_green) {
                    min_green = green;
                }
                if (blue > max_blue) {
                    max_blue = blue;
                }
                else if (blue < min_blue) {
                    min_blue = blue;
                }

            }
        }
    }

    /**
     * Returns the value of Red, Green, or Blue values by dividing sum previously
     * calculated in decodeYUV420SPtoRGB
     */

    public float getRedMean() {

        return ((float)sum_red / (float)frameSize)*4;
    }

    public float getGreenMean() {

        return ((float)sum_green / (float)frameSize)*4;
    }

    public float getBlueMean() {

        return ((float)sum_blue / (float)frameSize)*4;
    }

    /**
     * Returns the value of Red, Green, or Blue minimum values previously
     * calculated in decodeYUV420SPtoRGB
     */

    public int getRedMin() {

        return min_red;
    }

    public int getGreenMin() {

        return min_green;
    }

    public int getBlueMin() {

        return min_blue;
    }

    /**
     * Returns the value of Red, Green, or Blue maximum values previously
     * calculated in decodeYUV420SPtoRGB
     */

    public int getRedMax() {

        return max_red;
    }

    public int getGreenMax() {

        return max_green;
    }

    public int getBlueMax() {

        return max_blue;
    }

    //Standard Deviation

    public void calculateStandardDeviation() {

        double var_red = 0;
        double var_green = 0;
        double var_blue = 0;

        float mean_red = getRedMean();
        float mean_green = getGreenMean();
        float mean_blue = getBlueMean();

        for (int i = 0; i < frameSize/4; i++) {
            int red = (pixels[i] >> 16) & 0xff;
            int green = (pixels[i] >> 8) & 0xff;
            int blue = (pixels[i]) & 0xff;

            var_red = (var_red + Math.pow((red-mean_red),2));
            var_green = (var_green + Math.pow((green-mean_green),2));
            var_blue = (var_blue + Math.pow((blue-mean_blue),2));

        }

        sd_red = (float) Math.sqrt(var_red/frameSize*4);
        sd_green = (float) Math.sqrt(var_green/frameSize*4);
        sd_blue = (float) Math.sqrt(var_blue/frameSize*4);
    }

    public float getSdRed() {
        return sd_red;
    }

    public float getSdGreen() {
        return sd_green;
    }

    public float getSdBlue() {
        return sd_blue;
    }
}


