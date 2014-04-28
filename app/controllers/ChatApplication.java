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

  /** Keeps track of all connected browsers per room **/
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

  /**
   * Send event to all channels (browsers) which are connected to the room
   */
  public static void sendEvent(JsonNode msg) {
    String room  = msg.findPath("room").textValue();
    if(socketsPerRoom.containsKey(room)) {
      socketsPerRoom.get(room).stream().forEach(es -> es.send(EventSource.Event.event(msg)));
    }
  }

  /**
   * Establish the SSE HTTP 1.1 connection.
   * The new EventSource socket is stored in the socketsPerRoom Map
   * to keep track of which browser is in which room.
   *
   * onDisconnected removes the browser from the socketsPerRoom Map if the
   * browser window has been exited.
   * @return
   */
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
