package it.unisannio.group8.channels;

import org.fusesource.mqtt.client.Callback;

public class Callbacks {
    public static final EmptyCallback<Void> SIGNAL_FAILURE = new EmptyCallback<>();

    public static class EmptyCallback<T> implements Callback<T> {
        @Override
        public void onSuccess(T t) { }

        @Override
        public void onFailure(Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
