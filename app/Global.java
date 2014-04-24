import akka.ChatActors;
import play.Application;
import play.GlobalSettings;

public class Global extends GlobalSettings {

  @Override
  public void onStart(Application app) {
    ChatActors.start();
  }

  @Override
  public void onStop(Application app) {
    ChatActors.stop();
  }
}
