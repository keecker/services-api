package com.keecker.services.utils;

import com.keecker.services.utils.IpcMessage;

interface IIpcSubscriber {
    // We can use oneway, but some messages got lost when I tried it
    // Maybe provide a oneway option for publishers ?
    void onNewMessage(in IpcMessage message);
}