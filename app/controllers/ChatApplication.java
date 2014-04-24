package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.libs.EventSource;
import play.mvc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatApplication extends Controller {

  private static Map<String, List<EventSource>> socketsPerRoom = new HashMap<String, List<EventSource>>();

  /**
   * Controller action serving AngularJS chat page
   */
  public static Result index() {
    return ok(views.html.index.render("Chat using Server Sent Events and AngularJS"));
  }

  /**
   * Controller action for POSTing chat messages
   */
  public static Result postMessage() {
    sendEvent(request().body().asJson());
    return ok();
  }

  public static void sendEvent(JsonNode msg) {
    String room  = msg.findPath("room").textValue();
    if(socketsPerRoom.containsKey(room)) {
      for(EventSource es: socketsPerRoom.get(room)) {
        es.send(EventSource.Event.event(msg));
      }
    }
  }

  public static Result chatFeed(String room) {
    String remoteAddress = request().remoteAddress();
    Logger.info(remoteAddress + " - SSE conntected");

    return ok(new EventSource() {
      @Override
      public void onConnected() {
        EventSource currentSocket = this;

        this.onDisconnected(() -> {
          Logger.info(remoteAddress + " - SSE disconntected");
          socketsPerRoom.compute(room, (key, value) -> {
            if(value.contains(currentSocket))
              value.remove(currentSocket);
            return value;
          });
        });

        // Add socket to room
        socketsPerRoom.compute(room, (key, value) -> {
          if(value == null)
            return new ArrayList<EventSource>() {{ add(currentSocket); }};
          else
            value.add(currentSocket); return value;
        });
      }
    });
  }
}
