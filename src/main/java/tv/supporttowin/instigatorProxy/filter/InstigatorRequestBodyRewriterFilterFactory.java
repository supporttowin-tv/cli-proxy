package tv.supporttowin.instigatorProxy.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import tv.supporttowin.instigatorProxy.constants.Service;
import tv.supporttowin.instigatorProxy.factory.CustomPathMatchingPredicateFactory;
import tv.supporttowin.instigatorProxy.rewriters.InstigatorRewriteFunction;

@Component
public class InstigatorRequestBodyRewriterFilterFactory extends AbstractGatewayFilterFactory<CustomPathMatchingPredicateFactory.CustomPathMatchingConfig> {

    private final InstigatorRewriteFunction instigatorRewriteFunction;

    @Autowired
    public InstigatorRequestBodyRewriterFilterFactory(InstigatorRewriteFunction instigatorRewriteFunction) {
        super(CustomPathMatchingPredicateFactory.CustomPathMatchingConfig.class);
        this.instigatorRewriteFunction = instigatorRewriteFunction;
    }

    @Override
    public GatewayFilter apply(CustomPathMatchingPredicateFactory.CustomPathMatchingConfig config) {
        final var cfg = new ModifyRequestBodyGatewayFilterFactory.Config();
        final var func = getRewriteFunction(config.service());
        if (func == null) {
            return (exchange, chain) -> chain.filter(exchange);
        }
        cfg.setRewriteFunction(String.class, String.class, func);
        return (exchange, chain) -> new ModifyRequestBodyGatewayFilterFactory().apply(cfg).filter(exchange, chain);
    }

    private RewriteFunction<String, String> getRewriteFunction(final Service service) {
        return switch (service) {
            case INSTIGATOR -> instigatorRewriteFunction;
        };
    }

}
