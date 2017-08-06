package com.io;

import controller.DatabaseService;
import handler.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hoangnh on 02/08/2017.
 */
public class ConnectFacebook extends AbstractVerticle {
    static Logger logger = LoggerFactory.getLogger(ConnectFacebook.class);
    @Override
    public void start() {
        Router router = Router.router(vertx);

        router.get("/").handler(new HomeHandler());
        router.get("/webhook").handler(new GetWebhookHandler());
        router.post("/webhook").handler(new PostWebhookHandler());
        router.get("/getOrderToDay").handler(new GetOrderTodayHandler());
        router.get("/getAllUser").handler(new GetAllUserHandler());

        vertx.createHttpServer();
        vertx.createHttpServer().requestHandler(router::accept).listen(9199);
    }


    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setWorker(true);
        vertx.deployVerticle(new ConnectFacebook(), deploymentOptions);

        DatabaseService service = new DatabaseService();
        TimerTask tasknew = new TimerTask() {
            @Override
            public void run() {
                service.setNewOrderToday();
                logger.info("Set new order for today");
            }
        };

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 1);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.add(Calendar.DATE, 1);

        Timer timer = new Timer();
        System.out.println(today.getTime());
        timer.scheduleAtFixedRate(tasknew, today.getTime(), 86400000);
    }
}
