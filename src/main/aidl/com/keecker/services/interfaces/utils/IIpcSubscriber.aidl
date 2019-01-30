package com.keecker.services.interfaces.utils;

import com.keecker.services.interfaces.utils.IpcMessage;

interface IIpcSubscriber {
    // We can use oneway, but some messages got lost when I tried it
    // Maybe provide a oneway option for publishers ?
    void onNewMessage(in IpcMessage message);
}
