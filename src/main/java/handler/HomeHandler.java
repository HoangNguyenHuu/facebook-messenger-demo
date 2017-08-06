package handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by hoangnh on 05/08/2017.
 */
public class HomeHandler implements Handler<RoutingContext>{

    @Override
    public void handle(RoutingContext routingContext) {
        System.out.println("RUNNNIINNNGGGG");
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/html; charset=UTF-8");
        response.putHeader("Access-Control-Allow-Origin", "*");
        response.setStatusCode(200);
        response.sendFile("src/web/home.html");
//        response.end();
    }
}
