package com.example.mobilebptesting;

import android.content.res.AssetFileDescriptor;

import java.io.IOException;
import java.nio.MappedByteBuffer;

public class BloodPressureModel {

    float [][] X_in = new float[73][30];

    public int calculateBP (double [] ppg){

        // Get Peaks

        // Find zero crossings of PPG gradient, to determine peaks
        int count = 0;
        int waves_count = 0;
        int [] peaks = new int [50];
        int end = 0;
        for (int i = 150; i < 599; i++)
        {
            // Find average of last 4 seconds
            double sum_ppg_2s = 0;
            for (int j = i-60; j < i; j++) {
                sum_ppg_2s += ppg[j];
            }
            double ave_ppg_2s = sum_ppg_2s/120;
            if (ppg[i] > ppg[i-1] && ppg[i] > ppg[i+1] && ppg[i] > ave_ppg_2s) {
                if (count==0) {
                    peaks[count] = i;
                    end = i;
                    count++;
                }
                else if (i > end + 10) {
                    peaks[count] = i;
                    end = i;
                    count++;
                }
                else if (ppg[end] < ppg[i]) {
                    peaks[count-1] = i;
                    end = i;
                }
            }
        }


        // Split up into single waves ************************************************************
        for (int i = 0; i < count-1; i++) {
            int T = peaks[i+1]-peaks[i];
            //10% of T
            int left_10p = (int) Math.ceil(T*0.1);
            //5% of T
            int right_5p = (int) Math.ceil(T*0.05);

            // Make sure wave is less than 45 samples long
            if (left_10p + T + right_5p < 45) {

                //Isolate
                double [] single_wave = new double[45];
                double single_max = 0;
                double single_min = 255;
                // Find min and max values for normalisation
                for (int j = peaks[i]-left_10p; j < right_5p; j++) {
                    if (ppg[j] < single_min){
                        single_min = ppg[j];
                    }
                    if (ppg[j] > single_max){
                        single_max = ppg[j];
                    }
                }
                for (int j = 0; j < 45; j++) {
                    if (j < left_10p + T + right_5p){
                        // Create normalised signal
                        single_wave[j] = (ppg[j+peaks[i]-left_10p]-single_min)/(single_max-single_min);
                    }
                    else {
                        single_wave[j] = 0;
                    }
                }

                // Generate FFT for wave***********************************************************
                double [] dft_mag = new double[14];
                double [] dft_phase = new double[14];
                dft(single_wave,dft_mag,dft_phase);


                // Normalise FFT signals***********************************************************
                double [] dft_mag_norm = new double[14];
                double [] dft_phase_norm = new double[14];

                double mag_max = 0;
                double mag_min = 255;
                for (int j = 0; j < 14; j++) {
                    if (dft_mag[j] < mag_min){
                        mag_min = dft_mag[j];
                    }
                    if (dft_mag[j] > mag_max){
                        mag_max = dft_mag[j];
                    }
                }
                for (int j = 0; j < 14; j++) {
                    dft_mag_norm[j] = (dft_mag[j]-mag_min)/(mag_max-mag_min);
                    dft_phase_norm[j] = (dft_phase[j] + Math.PI)/(2*Math.PI);
                }

                // Join FFT and time signals,  and input into model

                X_in[waves_count] = concat(single_wave,dft_mag_norm,dft_phase_norm);

                waves_count++;

            }

        }

        return waves_count;
    }

    public float [] getX (int wave) {
        return X_in[wave];
    }

    private void dft(double[] inreal , double[] outmag, double[] outphase) {
        int n = 14;
        for (int k = 0; k < n; k++) {  // For each output element
            double sumreal = 0;
            double sumimag = 0;
            for (int t = 0; t < n; t++) {  // For each input element
                double angle = 2 * Math.PI * t * k / n;
                sumreal +=  inreal[t] * Math.cos(angle);
                sumimag += -inreal[t] * Math.sin(angle);
            }
            outmag[k] = Math.sqrt(Math.pow(sumreal,2) + Math.pow(sumimag,2));
            outphase[k] = Math.atan(sumimag/sumreal);
        }
    }

    private float[] concat (double [] array1, double [] array2, double [] array3) {
        int len1 = array1.length;
        int len2 = array2.length;
        int len3 = array3.length;

        float [] joined_array = new float[len1+len2+len3];

        for (int i = 0; i < len1; i++) {
            joined_array[i] = (float) array1[i];
        }
        for (int i = len1; i < len1+len2; i++) {
            joined_array[i] = (float) array2[i-len1];
        }
        for (int i = len1+len2; i < len1+len2+len3; i++) {
            joined_array[i] = (float) array3[i-len1-len2];
        }

        return joined_array;
    }


}
