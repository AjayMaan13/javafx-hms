package com.hotel.events;

import java.util.ArrayList;
import java.util.List;

/**
 * GoF Observer: a subject holds a list of observers and notifies all of them when something
 * happens, without knowing what any observer actually does. This decoupling is the point —
 * {@link RoomAvailabilityPublisher} never imports {@link WaitlistSubscriber} concretely;
 * they're only wired together in AppConfig.
 */
public abstract class Subject<T> {

    private final List<Observer<T>> observers = new ArrayList<>();

    public void attach(Observer<T> observer) {
        observers.add(observer);
    }

    public void detach(Observer<T> observer) {
        observers.remove(observer);
    }

    protected void notifyObservers(T event) {
        for (Observer<T> observer : List.copyOf(observers)) {
            observer.onEvent(event);
        }
    }
}
