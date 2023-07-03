package tv.supporttowin.instigatorProxy.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import tv.supporttowin.instigatorProxy.factory.CustomPathMatchingPredicateFactory;
import tv.supporttowin.instigatorProxy.matchers.PathMatchers;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

@Component
public class PathRewriteFilter extends AbstractGatewayFilterFactory<CustomPathMatchingPredicateFactory.CustomPathMatchingConfig> {

    @Autowired
    public PathRewriteFilter() {
        super(CustomPathMatchingPredicateFactory.CustomPathMatchingConfig.class);
    }

    @Override
    public GatewayFilter apply(final CustomPathMatchingPredicateFactory.CustomPathMatchingConfig config) {
        final var pathToSet = PathMatchers.getPathForService(config.service());
        return (exchange, chain) -> {
            if (pathToSet == null) {
                return chain.filter(exchange);
            }
            final var req = exchange.getRequest();
            addOriginalRequestUrl(exchange, req.getURI());
            final var path = req.getURI().getRawPath();
            final var newPath = path.replaceAll(pathToSet, "");
            final var request = req.mutate().path(newPath).build();
            exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, request.getURI());
            return chain.filter(exchange.mutate().request(request).build());
        };
    }
}
