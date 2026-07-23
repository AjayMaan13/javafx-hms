package com.hotel.events;

/** GoF Observer: something that reacts to events published by a {@link Subject}. */
public interface Observer<T> {

    void onEvent(T event);
}
