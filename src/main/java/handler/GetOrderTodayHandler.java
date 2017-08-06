package handler;

import com.google.gson.JsonArray;
import controller.DatabaseService;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by hoangnh on 05/08/2017.
 */
public class GetOrderTodayHandler implements Handler<RoutingContext>{
    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        DatabaseService databaseService = new DatabaseService();

        JsonArray array = databaseService.getBookingToday();
        String result = array.toString();
        response.putHeader("content-type", "application/json; charset=UTF-8");
        response.putHeader("Access-Control-Allow-Origin", "*");
        response.setStatusCode(200);
        response.end(result);
    }
}
