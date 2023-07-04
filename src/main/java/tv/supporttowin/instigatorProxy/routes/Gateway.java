package tv.supporttowin.instigatorProxy.routes;

import io.netty.handler.logging.LogLevel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.factory.SetRequestHeaderGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.Buildable;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;
import tv.supporttowin.instigatorProxy.constants.Service;
import tv.supporttowin.instigatorProxy.factory.CustomPathMatchingPredicateFactory;
import tv.supporttowin.instigatorProxy.factory.CustomPathMatchingPredicateFactory.CustomPathMatchingConfig;
import tv.supporttowin.instigatorProxy.filter.InstigatorRequestBodyRewriterFilterFactory;
import tv.supporttowin.instigatorProxy.filter.PathRewriteFilter;
import tv.supporttowin.instigatorProxy.matchers.PathMatchers;

import javax.validation.constraints.NotNull;
import java.util.function.Function;

@Configuration
public class Gateway {
    private static final Log log = LogFactory.getLog(Gateway.class);

    private final CustomPathMatchingPredicateFactory custompathMatchingPredicateFactory;

    private final SetRequestHeaderGatewayFilterFactory setRequestHeaderGatewayFilterFactory;

    private final InstigatorRequestBodyRewriterFilterFactory requestBodyMutatingFilter;

    private final PathRewriteFilter pathRewriteFilter;

    @Autowired
    public Gateway(
            final CustomPathMatchingPredicateFactory custompathMatchingPredicateFactory,
            final SetRequestHeaderGatewayFilterFactory setRequestHeaderGatewayFilterFactory,
            final PathRewriteFilter pathRewriteFilter,
            final InstigatorRequestBodyRewriterFilterFactory requestBodyMutatingFilter
    ) {
        this.custompathMatchingPredicateFactory = custompathMatchingPredicateFactory;
        this.setRequestHeaderGatewayFilterFactory = setRequestHeaderGatewayFilterFactory;
        this.pathRewriteFilter = pathRewriteFilter;
        this.requestBodyMutatingFilter = requestBodyMutatingFilter;
    }

    @Bean
    public RouteLocator instigatorRoute(final @NotNull RouteLocatorBuilder builder,
                                        final @Value("${backend.instigator.url}") String instigatorURL) {
        return builder.routes()
                .route("instigator-Route", getPredicateSpecBuildableFunction(Service.INSTIGATOR, instigatorURL))
                .build();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.create().wiretap("LoggingFilter", LogLevel.INFO, AdvancedByteBufFormat.TEXTUAL);
    }

    private Function<PredicateSpec, Buildable<Route>> getPredicateSpecBuildableFunction(final Service service, final String url) {
        log.info("Loaded route '" + service + "' for path '" + PathMatchers.getPathForService(service) + "' mapped to '" + url + "'");
        return r ->
                r.predicate(custompathMatchingPredicateFactory.apply(new CustomPathMatchingConfig(service)))
                        .filters(f -> f
                                .filter(setRequestHeaderGatewayFilterFactory.apply(c -> c.setName("Content-Type").setValue("application/json")))
                                .filter(requestBodyMutatingFilter.apply(new CustomPathMatchingConfig(Service.INSTIGATOR)))
                                .filter(pathRewriteFilter.apply(new CustomPathMatchingConfig(service)))
                        )
                        .uri(url);
    }

}
