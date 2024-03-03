package it.unisannio.group8.channels;

public interface Callback<T> {
    void onEvent(T t);
}
