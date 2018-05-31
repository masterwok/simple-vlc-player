package com.masterwok.simplevlcplayer.interfaces;

/**
 * Simple runnable interface that accepts a generic parameter. Mostly
 * used to allow for lambda expressions.
 *
 * @param <T> The type passed to the run method.
 */
public interface ParamRunnable<T> {

    /**
     * Invoke the function with a parameter of type, T.
     *
     * @param t The value parameter of type, T.
     */
    void run(T t);

}
