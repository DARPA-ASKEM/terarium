package software.uncharted.terarium.taskrunner.util;

import java.util.concurrent.TimeUnit;

public class TimeFormatter {

	public static String format(long millis) {
		if (millis < 0) {
			throw new IllegalArgumentException("Duration must be greater than zero!");
		}

		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
		millis -= TimeUnit.MINUTES.toMillis(seconds);

		StringBuilder sb = new StringBuilder(64);
		if (days > 0) {
			sb.append(days);
			sb.append("d ");
		}
		if (days > 0 || hours > 0) {
			sb.append(hours);
			sb.append("h ");
		}
		if (days > 0 || hours > 0 || minutes > 0) {
			sb.append(minutes);
			sb.append("m ");
		}
		if (days > 0 || hours > 0 || minutes > 0 || seconds > 0) {
			sb.append(seconds);
			sb.append("s ");
		}
		if (days > 0 || hours > 0 || minutes > 0 || seconds > 0 || millis > 0) {
			sb.append(seconds);
			sb.append("ms");
		}

		return (sb.toString());
	}
}
