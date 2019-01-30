package com.keecker.services.interfaces.utils.test;

import com.keecker.services.interfaces.utils.IIpcSubscriber;


interface IPublisherService {
    // Interface used by the subscriber to subscribe
    void subscribeToWeather(in IIpcSubscriber subscriber);
    void subscribeToForecast(in IIpcSubscriber subscriber);
    void subscribeToAlerts(in IIpcSubscriber subscriber);
    void unsubscribeToWeather(in IIpcSubscriber subscriber);
    void unsubscribeToForecast(in IIpcSubscriber subscriber);
    void unsubscribeToAlerts(in IIpcSubscriber subscriber);

    // Interface used by the unit test
    void publishWeather(int sun, int rain);
    void publishForecast();
    void publishAlert(String msg);
    void floodForecast(int count);
    int forecastSubscribersCount();
    //int getNumberOfMessage1Subscribers();
}