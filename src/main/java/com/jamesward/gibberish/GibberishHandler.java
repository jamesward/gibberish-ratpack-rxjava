package com.jamesward.gibberish;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.rx.RxRatpack;
import rx.Observable;
import rx.apache.http.ObservableHttp;

import java.net.URI;


// A fully Reactive handler that gets a random number then gets that many random words
public class GibberishHandler implements Handler {

    final static URI RANDOM_NUMBER_URI = URI.create("http://randnum.herokuapp.com/");
    final static URI RANDOM_WORD_URI = URI.create("http://random-word.herokuapp.com/");

    public void handle(final Context context) {

        CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();
        httpClient.start();

        Observable<Integer> num = ObservableHttp.createRequest(HttpAsyncMethods.createGet(RANDOM_NUMBER_URI), httpClient)
                .toObservable()
                .flatMap(response -> response.getContent().map(bb -> new Integer(new String(bb))));

        Observable<String> words = num.flatMap(i ->
                        ObservableHttp.createRequest(HttpAsyncMethods.createGet(RANDOM_WORD_URI), httpClient)
                                .toObservable()
                                .flatMap(response -> response.getContent().map(String::new))
                                .repeat(i)
                                .reduce((a, b) -> a + " " + b)
        );

        RxRatpack.asPromiseSingle(words).then(context::render);
    }
}