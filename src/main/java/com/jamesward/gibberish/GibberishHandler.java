package com.jamesward.gibberish;

import ratpack.handling.Context;
import ratpack.handling.InjectionHandler;
import ratpack.http.client.HttpClient;
import rx.Observable;

import java.net.URI;

import static ratpack.rx.RxRatpack.*;


// A fully Reactive handler that gets a random number then gets that many random words
public class GibberishHandler extends InjectionHandler {

    final static URI RANDOM_NUMBER_URI = URI.create("http://randnum.herokuapp.com/");
    final static URI RANDOM_WORD_URI = URI.create("http://random-word.herokuapp.com/");

    protected void handle(Context context, HttpClient httpClient) {
        httpClient.get(RANDOM_NUMBER_URI)
            .map(r -> r.getBody().getText())
            .map(Integer::valueOf)
            .flatMap(i ->
                    asPromiseSingle(
                        forkAndJoin(context,
                            forkOnNext(context, Observable.range(0, i))
                                .flatMap(n ->
                                        observe(httpClient.get(RANDOM_WORD_URI))
                                            .map(r -> r.getBody().getText())
                                )
                                .reduce((a, b) -> a + " " + b)
                        )
                    )
            ).then(context::render);
    }
}