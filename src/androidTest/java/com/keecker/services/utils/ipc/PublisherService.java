/*
 * Copyright (C) 2016 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.keecker.services.utils.ipc;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

import com.keecker.services.utils.IIpcSubscriber;
import com.keecker.services.utils.IpcPublisher;
import com.keecker.services.utils.test.IPublisherService;

import java.util.Random;

/**
 * A publisher service used for ipc test.
 * This service runs in ipc_pub process.
 * <p/>
 * Contributors: Cyril Lugan
 */
public class PublisherService extends Service {
    private final IpcPublisher<Weather> mWeatherPublisher = new IpcPublisher<>(Weather.class);
    private final IpcPublisher<Weather> mForecastPublisher = new IpcPublisher<>(Weather.class);
    private final IpcPublisher<WeatherAlert> mAlertPublisher = new IpcPublisher<>(WeatherAlert.class);

    final IBinder mBinder = new IPublisherService.Stub() {
        @Override
        public void subscribeToWeather(IIpcSubscriber sub) {
            mWeatherPublisher.add(sub);
        }

        @Override
        public void subscribeToForecast(IIpcSubscriber sub) {
            mForecastPublisher.add(sub);
        }

        @Override
        public void subscribeToAlerts(IIpcSubscriber sub) {
            mAlertPublisher.add(sub);
        }

        @Override
        public void unsubscribeToWeather(IIpcSubscriber sub) {
            mWeatherPublisher.remove(sub);
        }

        @Override
        public void unsubscribeToForecast(IIpcSubscriber sub) {
            mForecastPublisher.remove(sub);
        }

        @Override
        public void unsubscribeToAlerts(IIpcSubscriber sub) {
            mAlertPublisher.remove(sub);
        }

        @Override
        public void publishWeather(int sun, int rain) {
            Weather msg = new Weather(sun, rain);
            mWeatherPublisher.publish(msg);
        }

        @Override
        public void publishForecast() {
            Weather msg = new Weather(new Random().nextInt(), new Random().nextInt());
            mForecastPublisher.publish(msg);
        }

        @Override
        public void publishAlert(String value) {
            WeatherAlert msg = new WeatherAlert(value);
            mAlertPublisher.publish(msg);
        }

        @Override
        public void floodForecast(int count) {
            Weather msg = new Weather(new Random().nextInt(), new Random().nextInt());
            for (int i = 0; i < count; i++) {
                mForecastPublisher.publish(msg);
            }
        }

        @Override
        public int forecastSubscribersCount() {
            return mForecastPublisher.getSubscribersCount();
        }
    };

    @Override public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public static final class Weather implements Parcelable {
        int sun;
        int rain;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.sun);
            dest.writeInt(this.rain);
        }

        public Weather(int sun, int rain) {
            this.sun = sun;
            this.rain = rain;
        }

        protected Weather(Parcel in) {
            this.sun = in.readInt();
            this.rain = in.readInt();
        }

        public static final Creator<Weather> CREATOR = new Creator<Weather>() {
            @Override
            public Weather createFromParcel(Parcel source) {
                return new Weather(source);
            }

            @Override
            public Weather[] newArray(int size) {
                return new Weather[size];
            }
        };
    }

    public static final class WeatherAlert implements Parcelable {
        String message;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.message);
        }

        public WeatherAlert(String message) {
            this.message = message;
        }

        protected WeatherAlert(Parcel in) {
            this.message = in.readString();
        }

        public static final Creator<WeatherAlert> CREATOR = new Creator<WeatherAlert>() {
            @Override
            public WeatherAlert createFromParcel(Parcel source) {
                return new WeatherAlert(source);
            }

            @Override
            public WeatherAlert[] newArray(int size) {
                return new WeatherAlert[size];
            }
        };
    }
}
