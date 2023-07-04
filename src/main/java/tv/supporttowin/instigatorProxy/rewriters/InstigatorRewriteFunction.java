package tv.supporttowin.instigatorProxy.rewriters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class InstigatorRewriteFunction implements RewriteFunction<String, String> {

    private static final Log log = LogFactory.getLog(InstigatorRewriteFunction.class);

    private final ObjectMapper mapper;

    @Autowired
    public InstigatorRewriteFunction(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Publisher<String> apply(ServerWebExchange serverWebExchange, String oldBody) {
        try {
            final var originalJson = mapper.readTree(oldBody);
            var customerNode = mapper.createObjectNode();
            customerNode.put("mobile", originalJson.get("customerMobile").asText());
            final var rootObj = mapper.createObjectNode();
            rootObj.put("client_id", originalJson.get("client_id").asText());
            rootObj.put("campaign_id", originalJson.get("campaign_id").asText());
            rootObj.set("customer", customerNode);
            String newBody = mapper.writeValueAsString(rootObj);
            return Mono.just(newBody);
        } catch (Exception e) {
            log.error("Unable to read JSON", e);
            return Mono.just(oldBody);
        }
    }

}
