package software.uncharted.terarium.db.migration.callbacks;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

@Slf4j
public record FlywayMigrationCompleteCallback(ApplicationContext applicationContext) implements Callback {

  @Override
  public boolean supports(final Event event, final Context context) {
    return event == Event.AFTER_MIGRATE;
  }

  @Override
  public boolean canHandleInTransaction(final Event event, final Context context) {
    return true;
  }

  @Override
  public void handle(final Event event, final Context context) {
    if (this.supports(event, context)) {
      log.info("Migration Completed Successfully, shutting down");

      SpringApplication.exit(applicationContext, new ExitCodeGenerator() {
        @Override
        public int getExitCode() {
          return 0;
        }
      });
      System.exit(0);
    }
  }

  @Override
  public String getCallbackName() {
    return "Migration Complete Callback";
  }
}
