package handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by hoangnh on 05/08/2017.
 */
public class GetWebhookHandler implements Handler<RoutingContext>{
    private static String VERIFY_KEY = "check_my_url_web";
    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();

        String mode = request.getParam("hub.mode");
        String key = request.getParam("hub.verify_token");
        String challenge = request.getParam("hub.challenge");
        if (mode.equals("subscribe") && key.equals(VERIFY_KEY)) {
            System.out.println("Verify success");
            response.putHeader("content-type", "application/json; charset=UTF-8");
            response.putHeader("Access-Control-Allow-Origin", "*");
            response.setStatusCode(200);
            response.end(challenge);

        } else {
            System.out.println("Error verify");
            response.putHeader("content-type", "application/json; charset=UTF-8");
            response.putHeader("Access-Control-Allow-Origin", "*");
            response.setStatusCode(403);
            response.end();
        }
    }
}
