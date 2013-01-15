package sdjug;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.Verticle;

public class SDJUGBackend extends Verticle {
    @Override
    public void start() throws Exception {

        Handler<Message> myHandler = new Handler<Message>() {
            public void handle(Message message) {
                JsonObject jsonObject = (JsonObject)message.body;
                System.out.println("I received a message: " + jsonObject.getString("message"));
            }
        };

        vertx.eventBus().registerHandler("sdjug.eventbus.message", myHandler);

    }
}


