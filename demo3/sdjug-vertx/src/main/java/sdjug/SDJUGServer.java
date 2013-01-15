package sdjug;

import org.vertx.java.core.Handler;
import org.vertx.java.core.SimpleHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
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
        server.requestHandler( createRouteMatcher() ).listen(8090, "localhost");
    }

    private RouteMatcher createRouteMatcher() {
        final RouteMatcher routeMatcher = new RouteMatcher();

        // Hanlde GET request
        routeMatcher.get("/sensordata", new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
                vertx.eventBus().send("sdjug.eventbus.message.load",
                                        new JsonObject(),
                                        createLoadReplyHandler(req));
            }
        });

        // Hanlde POST request
        routeMatcher.post("/sensordata", new Handler<HttpServerRequest>() {
            public void handle(final HttpServerRequest req) {

                // Read the whole post body via data handler
                final Buffer body = new Buffer(64);

                req.dataHandler(new Handler<Buffer>() {
                    public void handle(Buffer buffer) {
                        body.appendBuffer(buffer);
                    }
                });

                // Done reading the whole request body
                req.endHandler(new SimpleHandler() {
                    public void handle() {
                        JsonObject postJson = new JsonObject(body.toString("UTF-8"));
                        vertx.eventBus().send("sdjug.eventbus.message.save", postJson, createSaveReplyHandler(req));
                    }
                });
            }
        });

        return routeMatcher;
    }

    private Handler<Message<JsonObject>> createLoadReplyHandler(final HttpServerRequest req) {
          return new Handler<Message<JsonObject>>() {
              @Override
              public void handle(final Message<JsonObject> jsonObjectMessage) {
                  req.response.end("You loaded sensordata: " + jsonObjectMessage.body.getString("reply"));
              }
          };
    }

    private Handler<Message<JsonObject>> createSaveReplyHandler(final HttpServerRequest req) {
        return new Handler<Message<JsonObject>>() {
            @Override
            public void handle(final Message<JsonObject> jsonObjectMessage) {
                req.response.statusCode = 201;
                req.response.putHeader("Content-Type", "application/json; charset=UTF-8");
                req.response.putHeader("Location", "sensordata/" + jsonObjectMessage.body.getNumber("id"));
                req.response.end("You saved sensordata: " + jsonObjectMessage.body);
            }
        };
    }
}