package software.uncharted.terarium.hmiserver.configuration;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class TerariumEnvPostProcessor implements EnvironmentPostProcessor {

    private static final String TERARIUM_USER_QUEUE_SUFFIX = "terarium.userqueue.suffix";


    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        environment.getSystemProperties().put(TERARIUM_USER_QUEUE_SUFFIX, getQueueSuffix());
    }

    public String getQueueSuffix() {

        String hostName = UUID.randomUUID().toString();
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
						if(inetAddress.getHostName().equalsIgnoreCase("staging") || inetAddress.getHostName().equalsIgnoreCase("production")){
							System.err.println("Hostname is staging or production, using random queue name. This may have unintended consequences");
							return hostName;
						}
            hostName = inetAddress.getHostName();
        } catch (UnknownHostException e) {
            //This happens before our logger is initialized. Need to use System.out
            System.err.println("UnknownHostException: " + e.getMessage());
        }
        return hostName;
    }

}
