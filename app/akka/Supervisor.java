package akka;

import akka.actor.*;
import scala.concurrent.duration.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Supervisor initiating Romeo and Juliet actors and scheduling their talking
 */
public class Supervisor extends UntypedActor {

  private final ActorRef juliet;
  private final ActorRef romeo;

  public Supervisor() {
    this.juliet = context().actorOf(Props.create(Chatter.class, () -> new Chatter("Juliet", Quotes.juliet)));
    context().system().scheduler().schedule(
      Duration.apply(1, TimeUnit.SECONDS),
      Duration.apply(8, TimeUnit.SECONDS),
      juliet, ChatActors.TALK,
      getContext().dispatcher(), self());

    this.romeo = context().actorOf(Props.create(Chatter.class, () -> new Chatter("Romeo", Quotes.romeo)));
    context().system().scheduler().schedule(
      Duration.apply(1, TimeUnit.SECONDS),
      Duration.apply(8, TimeUnit.SECONDS),
      romeo, ChatActors.TALK,
      getContext().dispatcher(), self());
  }

  public void onReceive(Object message) throws Exception {}
}
