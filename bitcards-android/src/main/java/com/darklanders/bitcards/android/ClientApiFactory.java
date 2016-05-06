package com.darklanders.bitcards.android;

/**
 * Created by Chris on 24/04/2016.
 */
public interface ClientApiFactory {

    ClientApi connect() throws Exception;
}
