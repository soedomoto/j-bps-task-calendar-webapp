package id.go.bps.calendar.handler;

import com.google.protobuf.Empty;
import id.go.bps.request.RequestID;
import id.go.bps.user.Position;
import id.go.bps.user.PositionServiceGrpc;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;

import java.util.Iterator;

public class TaskHandler extends AbstractGrpcHandler {
    private PositionServiceGrpc.PositionServiceBlockingStub stub;

    public TaskHandler(Router router, String host, int port) {
        super(router, host, port);
        stub = PositionServiceGrpc.newBlockingStub(channel);
    }

    public void setup() {
        router.get("/api/position/list").handler(requestHandler -> {
            Iterator<Position> positions = stub.list(Empty.newBuilder().build());

            requestHandler.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(positions));
        });

        router.get("/api/position/:id").handler(requestHandler -> {
            String id = requestHandler.request().getParam("id");

            Position position = null;
            try {
                position = stub.get(RequestID.newBuilder().setId(id).build());
            } catch (Exception e) {}

            HttpServerResponse response = requestHandler.response()
                    .putHeader("content-type", "application/json; charset=utf-8");

            if(response != null) {
                response.end(Json.encodePrettily(position));
            } else {
                response.end();
            }
        });

        router.post("/api/position/add").handler(requestHandler -> {

        });

        router.post("/api/position/edit/:id").handler(requestHandler -> {

        });

        router.get("/api/position/delete/:id").handler(requestHandler -> {

        });

        router.post("/api/position/search").handler(requestHandler -> {

        });

        router.post("/api/position/search/raw").handler(requestHandler -> {

        });
    }
}
