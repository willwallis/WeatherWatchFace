package com.example.android.sunshine.app;

/**
 * Created by willwallis on 3/7/16.
 * Pick images based on weather ID
 */
public class Utility {

    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId < 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

    /*
       * Helper method to provide the correct image according to the weather condition id returned
       * by the OpenWeatherMap call.
       *
       * @param weatherId from OpenWeatherMap API response
       * @return A string URL to an appropriate image or null if no mapping is found
       */
    public static int getImageUrlForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.thunderstorm_in_annemasse;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.rain_on_leaf;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.rain_on_thassos;
        } else if (weatherId == 511) {
            return R.drawable.fresh_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.rain_on_thassos;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.fresh_snow;
        } else if (weatherId >= 701 && weatherId < 761) {
            return R.drawable.westminster_fog_london;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.raised_dust_ahead;
        } else if (weatherId == 800) {
            return R.drawable.a_few_trees;
        } else if (weatherId == 801) {
            return R.drawable.cloudy_blue_sky;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.cloudy_hills_in;
        }
        return -1;
    }

    public static int getTextColor(int weatherId) {
        final int LIGHTLIGHT = 0;
        final int LIGHTDARK = 1;
        final int DARKLIGHT = 2;
        final int DARKDARK = 3;
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return LIGHTLIGHT;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return LIGHTLIGHT;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return LIGHTLIGHT;
        } else if (weatherId == 511) {
            return DARKLIGHT;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return LIGHTLIGHT;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return DARKLIGHT;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return LIGHTLIGHT;
        } else if (weatherId == 761 || weatherId == 781) {
            return LIGHTLIGHT;
        } else if (weatherId == 800) {
            return DARKDARK;
        } else if (weatherId == 801) {
            return DARKDARK;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return LIGHTLIGHT;
        }
        return -1;
    }

}
