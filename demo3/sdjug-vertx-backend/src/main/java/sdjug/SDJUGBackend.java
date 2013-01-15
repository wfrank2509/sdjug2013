package sdjug;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.Verticle;

public class SDJUGBackend extends Verticle {
    @Override
    public void start() throws Exception {

        Handler<Message<JsonObject>> loadHandler = new Handler<Message<JsonObject>>() {
            public void handle(final Message<JsonObject> message) {
                System.out.println("I received a load message!");
                processLoad(message);
            }
        };
        vertx.eventBus().registerHandler("sdjug.eventbus.message.load", loadHandler);

        Handler<Message> saveHandler = new Handler<Message>() {
            public void handle(Message message) {
                System.out.println("I received a save message!");
                processSave(message);
            }
        };
        vertx.eventBus().registerHandler("sdjug.eventbus.message.save", saveHandler);
    }

    private void processLoad(final Message<JsonObject> message) {
        message.reply(new JsonObject().putString("reply", "Loaded data"));
    }

    private void processSave(final Message<JsonObject> message) {
        System.out.println(message.body);

        JsonObject replyJson  = new JsonObject();
        replyJson.putString("reply", "Saved data");
        replyJson.putNumber("id", System.currentTimeMillis());

        message.reply(replyJson);
    }

}


