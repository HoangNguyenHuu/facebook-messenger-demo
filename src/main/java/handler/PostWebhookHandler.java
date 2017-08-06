package handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.io.ConnectFacebook;
import controller.DatabaseService;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import model.Conversation;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.sql.Connection;
import java.util.ArrayList;

/**
 * Created by hoangnh on 05/08/2017.
 */
public class PostWebhookHandler implements Handler<RoutingContext> {

    private static String ACCESS_TOKEN = "EAALnL8Jxp3gBABeZANVXeSZBLontQ7HmdItnizuDl98PhZCAQ70VtwiKY3gRHvUYrZCECZAPxjaZCb2DXIr3RodQYZBZCwyUYUGJvBCoViYktqZCOulYcieoVsYMasKZAfIeduIfrhba6WZA7T9gZA8sRy5Ic8RnrrswBOL93FlrW8FNPdlePOCQgePM";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static final String STATE1 = "start";
    public static final String STATE2 = "order";
    public static final String STATE3 = "cancel";
    public static final String STATE4 = "change";

    public static Logger logger = LoggerFactory.getLogger(PostWebhookHandler.class);

    public static final String function1 = "1. Đặt cơm";
    public static final String function2 = "2. Hủy cơm trưa";
    public static final String function3 = "3. Đổi kiểu đặt cơm";
    public static final String confirm1 = "1. Yes";
    public static final String confirm2 = "2. No";

    public static final DatabaseService databaseService = new DatabaseService();

    ArrayList<Conversation> conversations = new ArrayList<>();

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();

        routingContext.request().bodyHandler(buffer -> {
            String body = buffer.toString();
            logger.info("Receive: \n" + body);
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(body);

            try {
                JsonObject messaging = element.getAsJsonObject()
                        .get("entry")
                        .getAsJsonArray()
                        .get(0)
                        .getAsJsonObject()
                        .get("messaging")
                        .getAsJsonArray()
                        .get(0)
                        .getAsJsonObject();
                String senderId = messaging.get("sender")
                        .getAsJsonObject()
                        .get("id")
                        .getAsString();
                if (messaging.has("message")) {
                    String messageText = messaging.get("message")
                            .getAsJsonObject()
                            .get("text")
                            .getAsString();
                    if (getConversation(senderId) != null) {
                        Conversation conversation = getConversation(senderId);
                        conversations.remove(conversation);
                        answerTextGenerator(conversation);
                        conversations.add(conversation);
                        logger.info("Conversions after size: " + conversations.size());
                    } else {
                        if (checkUser(senderId) == false) {
                            sendTextMessage(senderId, "Sorry, bạn chưa có trong cơ sở dữ liệu của chúng tôi");
                        } else {
                            Conversation conversation = createCoversation(senderId);
                            answerTextGenerator(conversation);
                            conversations.add(conversation);
                        }
                    }
                } else if (messaging.has("postback")) {
                    String messageText = messaging.get("postback").
                            getAsJsonObject().
                            get("title").
                            getAsString();
                    if (getConversation(senderId) != null) {
                        Conversation conversation = getConversation(senderId);
                        conversations.remove(conversation);
                        answerPostbackGenerator(conversation, messageText);
                    } else {
                        if (checkUser(senderId) == false) {
                            sendTextMessage(senderId, "Sorry, bạn chưa có trong cơ sở dữ liệu của chúng tôi");
                        } else {
                            Conversation conversation = createCoversation(senderId);
                            answerPostbackGenerator(conversation, messageText);
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            response.putHeader("content-type", "application/json; charset=UTF-8");
            response.putHeader("Access-Control-Allow-Origin", "*");
            response.setStatusCode(200);
            response.end("OK");

        });
    }

    public void answerPostbackGenerator(Conversation conversation, String message) {
        String currentState = conversation.getCurrentState();
        String recipient = conversation.getFacebook_id();
        String name = conversation.getName();
        boolean auto = conversation.isAuto();
        logger.debug("Okie, da bat duoc:  " + message);
        if (currentState.equals(STATE1) && message.equals(function1)) {
            boolean checkBooking = databaseService.checkOrder(recipient);
            if (checkBooking == false) {
                String query = "Bạn " + name + " chọn chức năng đặt cơm trưa nay. Mời bạn xác nhận: ";
                sendButtonStateMessage(recipient, query);

                conversation.setCurrentState(STATE2);
                conversations.add(conversation);
            }else {
                String query = "Bạn " + name + " đã đặt cơm trước đó trên hệ thống rồi. Chào bạn";
                sendTextMessage(recipient, query);

                conversations.add(conversation);
            }
        } else if (currentState.equals(STATE1) && message.equals(function2)) {
            boolean checkBooking = databaseService.checkOrder(recipient);
            if(checkBooking == true) {
                String query = "Bạn " + name + " chọn chức năng hủy cơm trưa nay. Mời bạn xác nhận: ";
                sendButtonStateMessage(recipient, query);

                conversation.setCurrentState(STATE3);
                conversations.add(conversation);
            }else {
                String query = "Bạn " + name + " chưa có đặt cơm trên hệ thống. Chào bạn";
                sendTextMessage(recipient, query);

                conversations.add(conversation);
            }
        } else if (currentState.equals(STATE1) && message.equals(function3)) {

            if (auto == true) {
                String query = "Bạn " + name + " hiện đang ở chế độ tự động đặt cơm. Bạn muốn chuyển sang chế độ báo cơm. Mời bạn xác nhận: ";
                sendButtonStateMessage(recipient, query);

                conversation.setCurrentState(STATE4);

                conversations.add(conversation);
            } else {
                String query = "Bạn " + name + " hiện đang ở chế độ báo cơm. Bạn muốn chuyển sang chế độ tự động đặt cơm. Mời bạn xác nhận: ";
                sendButtonStateMessage(recipient, query);

                conversation.setCurrentState(STATE4);

                conversations.add(conversation);
            }
        } else if (currentState.equals(STATE2) && message.equals(confirm1)) {
            databaseService.order(recipient);
            String messageResponse = "Ok. Bạn " + name + " đã đặt cơm thành công";
            sendTextMessage(recipient, messageResponse);

            conversation.setCurrentState(STATE1);
            conversations.add(conversation);
        } else if (currentState.equals(STATE2) && message.equals(confirm2)) {
            String messageResponse = "Bạn đã không đặt cơm trưa nay! Chào bạn";
            sendTextMessage(recipient, messageResponse);

            conversation.setCurrentState(STATE1);
            conversations.add(conversation);
        } else if (currentState.equals(STATE3) && message.equals(confirm1)) {
            databaseService.cancel(recipient);
            String messageResponse = "Ok. Bạn " + name + " đã hủy cơm trưa thành công";
            sendTextMessage(recipient, messageResponse);

            conversation.setCurrentState(STATE1);
            conversations.add(conversation);
        } else if (currentState.equals(STATE3) && message.equals(confirm2)) {
            String messageResponse = "Bạn đã không hủy cơm trưa nay! Chào bạn";
            sendTextMessage(recipient, messageResponse);

            conversation.setCurrentState(STATE1);
            conversations.add(conversation);
        } else if (currentState.equals(STATE4) && message.equals(confirm1)) {
            boolean mode = !auto;
            databaseService.changeMode(recipient, mode);
            String messageResponse = "Ok. Bạn " + name + " đã chuyển sang chế độ tự động đặt cơm thành công";
            if (mode == false){
                messageResponse = "Ok. Bạn " + name + " đã chuyển sang chế độ báo cơm thành công";
            }
            sendTextMessage(recipient, messageResponse);

            conversation.setCurrentState(STATE1);
            conversations.add(conversation);
        } else if (currentState.equals(STATE4) && message.equals(confirm2)) {
            String messageResponse = "Bạn vẫn đang ở chế độ tự động đặt cơm! Chào bạn";
            if(auto == false){
                messageResponse = "Bạn vẫn đang ở chế độ báo cơm! Chào bạn";
            }
            sendTextMessage(recipient, messageResponse);

            conversation.setCurrentState(STATE1);
            conversations.add(conversation);
        }
    }

    public void answerTextGenerator(Conversation conversation) {
        String currentState = conversation.getCurrentState();
        String recipient = conversation.getFacebook_id();
        String name = conversation.getName();
        boolean auto = conversation.isAuto();
        if (currentState.equals(STATE1)) {
            sendButtonStartMessage(recipient);
        } else if (currentState.equals(STATE2)) {
            String query = "Bạn " + name + " chọn chức năng đặt cơm trưa nay. Mời bạn xác nhận: ";
            sendButtonStateMessage(recipient, query);
        } else if (currentState.equals(STATE3)) {
            String query = "Bạn " + name + " chọn chức năng hủy cơm trưa nay. Mời bạn xác nhận: ";
            sendButtonStateMessage(recipient, query);
        } else if (currentState.equals(STATE4)) {
            if (auto == true) {
                String query = "Bạn " + name + " hiện đang ở chế độ tự động đặt cơm. Bạn muốn chuyển sang chế độ báo cơm. Mời bạn xác nhận: ";
                sendButtonStateMessage(recipient, query);
            } else {
                String query = "Bạn " + name + " hiện đang ở chế độ báo cơm. Bạn muốn chuyển sang chế độ tự động đặt cơm. Mời bạn xác nhận: ";
                sendButtonStateMessage(recipient, query);
            }
        }
    }

    public void sendTextMessage(String recipientId, String messageText) {
        JsonObject recipient = new JsonObject();
        recipient.addProperty("id", recipientId);
        JsonObject message = new JsonObject();
        message.addProperty("text", messageText);
        JsonObject messageJson = new JsonObject();
        messageJson.add("recipient", recipient);
        messageJson.add("message", message);
        String messageData = messageJson.toString();
        logger.info("Send: \n" + messageData);
        callSendApi(messageData);
    }

    public void sendButtonStartMessage(String recipientId) {
        JsonObject recipient = new JsonObject();
        recipient.addProperty("id", recipientId);

        JsonArray buttons = new JsonArray();
        JsonObject object1 = new JsonObject();
        object1.addProperty("type", "postback");
        object1.addProperty("title", function1);
        object1.addProperty("payload", "DEVELOPER_DEFINED_PAYLOAD");

        JsonObject object2 = new JsonObject();
        object2.addProperty("type", "postback");
        object2.addProperty("title", function2);
        object2.addProperty("payload", "DEVELOPER_DEFINED_PAYLOAD");

        JsonObject object3 = new JsonObject();
        object3.addProperty("type", "postback");
        object3.addProperty("title", function3);
        object3.addProperty("payload", "DEVELOPER_DEFINED_PAYLOAD");

        buttons.add(object1);
        buttons.add(object2);
        buttons.add(object3);

        JsonObject payload = new JsonObject();
        payload.addProperty("template_type", "button");
        payload.addProperty("text", "Mời bạn chọn chức năng:");
        payload.add("buttons", buttons);

        JsonObject attachment = new JsonObject();
        attachment.addProperty("type", "template");
        attachment.add("payload", payload);

        JsonObject message = new JsonObject();
        message.add("attachment", attachment);

        JsonObject messageJson = new JsonObject();
        messageJson.add("recipient", recipient);
        messageJson.add("message", message);
        String messageData = messageJson.toString();
        System.out.println(messageData);
        callSendApi(messageData);
    }

    public void sendButtonStateMessage(String recipientId, String query) {
        JsonObject recipient = new JsonObject();
        recipient.addProperty("id", recipientId);

        JsonArray buttons = new JsonArray();
        JsonObject object1 = new JsonObject();
        object1.addProperty("type", "postback");
        object1.addProperty("title", confirm1);
        object1.addProperty("payload", "DEVELOPER_DEFINED_PAYLOAD");

        JsonObject object2 = new JsonObject();
        object2.addProperty("type", "postback");
        object2.addProperty("title", confirm2);
        object2.addProperty("payload", "DEVELOPER_DEFINED_PAYLOAD");

        buttons.add(object1);
        buttons.add(object2);

        JsonObject payload = new JsonObject();
        payload.addProperty("template_type", "button");
        payload.addProperty("text", query);
        payload.add("buttons", buttons);

        JsonObject attachment = new JsonObject();
        attachment.addProperty("type", "template");
        attachment.add("payload", payload);

        JsonObject message = new JsonObject();
        message.add("attachment", attachment);

        JsonObject messageJson = new JsonObject();
        messageJson.add("recipient", recipient);
        messageJson.add("message", message);
        String messageData = messageJson.toString();
        System.out.println(messageData);
        callSendApi(messageData);
    }

    String post(String url, String json) {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            return response.toString();
            // Do something with the response.
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Exception";
    }

    public Conversation getConversation(String facebook_id) {
        for (int i = 0; i < conversations.size(); i++) {
            Conversation conversation = conversations.get(i);
            if (conversation.getFacebook_id().equals(facebook_id)) {
                return conversation;
            }
        }
        return null;
    }

    public boolean checkUser(String sender_id) {
        try {
            java.sql.Connection connection = DriverManager.getConnection("jdbc:mysql://db4free.net:3307/book_lunch", "anhnguyenvn219", "thiengiac@219");
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM User WHERE facebook_id=" + sender_id);
            if (!rs.first()) {
                return false;
            } else {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Conversation createCoversation(String sender_id) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://db4free.net:3307/book_lunch", "anhnguyenvn219", "thiengiac@219");
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM User WHERE facebook_id=" + sender_id);
            if (!rs.first()) {
                return null;
            } else {
                String name = rs.getString("name");
                String facebook_id = rs.getString("facebook_id");
                boolean auto = rs.getBoolean("auto");
                System.out.println(name + ", " + facebook_id + ", " + auto);
                Conversation conversation = new Conversation(facebook_id, name, auto, STATE1);
                return conversation;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void callSendApi(String messageData) {
        logger.debug("Size: " + conversations.size());
        String url = " https://graph.facebook.com/v2.6/me/messages?access_token=" + ACCESS_TOKEN;
        String response = post(url, messageData);
        logger.info("Respone after send message: \n" + response);
    }
}
