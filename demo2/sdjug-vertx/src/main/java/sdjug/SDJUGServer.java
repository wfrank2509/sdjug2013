package sdjug;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.Verticle;


public class SDJUGServer extends Verticle {
    @Override
    public void start() throws Exception {
        HttpServer server = vertx.createHttpServer();
        server.setAcceptBacklog(50000);

        server.requestHandler(new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
                System.out.println("Received HTTP call...");
                String file = req.path.equals("/") ? "index.html" : req.path;
                req.response.sendFile("webroot/" + file);

                EventBus eventBus = vertx.eventBus();

                JsonObject jsonObject = new JsonObject();
                jsonObject.putString("message","This is my test message...");

                eventBus.send("sdjug.eventbus.message", jsonObject);
            }
        }).listen(8090);
    }
}

