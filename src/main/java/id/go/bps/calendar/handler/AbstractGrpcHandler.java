package id.go.bps.calendar.handler;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.vertx.ext.web.Router;

public abstract class AbstractGrpcHandler extends AbstractHandler {
    protected ManagedChannel channel;

    public AbstractGrpcHandler(Router router) {
        super(router);
    }

    public AbstractGrpcHandler(Router router, String host, int port) {
        this(router);

        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();
    }
}
