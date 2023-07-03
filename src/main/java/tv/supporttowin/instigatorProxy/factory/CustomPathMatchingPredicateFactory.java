package tv.supporttowin.instigatorProxy.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;
import tv.supporttowin.instigatorProxy.constants.Service;
import tv.supporttowin.instigatorProxy.matchers.PathMatchers;

import java.util.function.Predicate;

@Component
public class CustomPathMatchingPredicateFactory extends AbstractRoutePredicateFactory<CustomPathMatchingPredicateFactory.CustomPathMatchingConfig> {

    @Autowired
    public CustomPathMatchingPredicateFactory() {
        super(CustomPathMatchingConfig.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(final CustomPathMatchingConfig config) {
        final var pathForService = PathMatchers.getPathForService(config.service());
        return serverWebExchange -> {
            final var path = serverWebExchange.getRequest().getPath().value();
            return path.startsWith(pathForService);
        };
    }

    @Validated
    public record CustomPathMatchingConfig(Service service) {

    }
}
