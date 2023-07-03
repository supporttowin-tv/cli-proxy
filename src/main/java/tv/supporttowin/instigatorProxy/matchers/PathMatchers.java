package tv.supporttowin.instigatorProxy.matchers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tv.supporttowin.instigatorProxy.constants.Service;

@Component
public class PathMatchers {
    private static String instigatorPath;

    public PathMatchers(final @Value("${backend.instigator.pathMatch}") String instigatorPath) {
        PathMatchers.instigatorPath = instigatorPath;
    }

    public static String getPathForService(final Service service) {
        return switch (service) {
            case INSTIGATOR -> PathMatchers.instigatorPath;
        };
    }
}
