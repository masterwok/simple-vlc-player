package com.masterwok.simplevlcplayer.interfaces;

/**
 * Simple callable interface that accepts a generic parameter. Mostly
 * used to allow for lambda expressions.
 *
 * @param <T> The type returned from the call method.
 */
public interface ResultCallable<T> {

    /**
     * Invoke the function.
     *
     * @return The return value of the function as type, T.
     */
    T call();
}
