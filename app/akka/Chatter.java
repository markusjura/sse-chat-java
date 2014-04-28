package akka;

import akka.actor.*;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.ChatApplication;
import org.joda.time.DateTime;
import play.libs.Json;
import java.util.List;
import java.util.Random;

/**
 * Chat participant actors picking quotes at random when told to talk
 */
public class Chatter extends UntypedActor {

  private final String name;
  private final List<String> quotes;

  public Chatter(String name, List<String> quotes) {
    this.name = name;
    this.quotes = quotes;
  }

  public void onReceive(Object message) throws Exception {
    if(message == ChatActors.TALK) {
      String now = DateTime.now().toString();
      String quote = quotes.get(new Random().nextInt(quotes.size()));
      JsonNode msg = Json.newObject()
        .put("room", "room1")
        .put("text", quote)
        .put("user", name)
        .put("time", now);

      ChatApplication.sendEvent(msg);
    }
  }
}
