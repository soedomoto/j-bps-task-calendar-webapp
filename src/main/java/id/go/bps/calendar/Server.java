package id.go.bps.calendar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.protobuf.Empty;
import com.hubspot.jinjava.Jinjava;
import id.go.bps.request.RequestID;
import id.go.bps.user.Position;
import id.go.bps.user.PositionServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Server extends AbstractVerticle {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    @Override
    public void start() throws Exception {
        ManagedChannel userChannel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext(true)
                .build();

        PositionServiceGrpc.PositionServiceBlockingStub positionStub = PositionServiceGrpc.newBlockingStub(userChannel);

        Router router = Router.router(vertx);

        router.get("/api/position/list").handler(requestHandler -> {
            Iterator<Position> positions = positionStub.list(Empty.newBuilder().build());

            requestHandler.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(positions));
        });

        router.get("/api/position/:id").handler(requestHandler -> {
            String id = requestHandler.request().getParam("id");

            Position position = null;
            try {
                position = positionStub.get(RequestID.newBuilder().setId(id).build());
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
            String form = requestHandler.request().toString();

            positionStub.add(Position.newBuilder()
//                    .setId(form.get("id"))
                    .build());
        });

        router.route("/static/*").handler(StaticHandler.create("static"));

        Map<String, Object> options = new HashMap<String, Object>() {{
            put("baseUrl", "/");
            put("calendar", new HashMap<String, Object>() {{
                put("allEvent", "/api/event/list");
                put("addEvent", "/api/event/add");
                put("editEvent", "/api/event/edit");
                put("delEvent", "/api/event/delete");
                put("searchEvent", "/api/event/search");

                put("allUser", "/api/user/list");
                put("addUser", "/api/user/add");
                put("editUser", "/api/user/edit");
                put("delUser", "/api/user/delete");
                put("searchUser", "/api/user/search");
                put("searchUserRaw", "/api/user/search/raw");
                put("login", "/api/user/login");
                put("logout", "/api/user/logout");

                put("allRank", "/api/rank/list");
                put("addRank", "/api/rank/add");
                put("editRank", "/api/rank/edit");
                put("deleteRank", "/api/rank/delete");
                put("searchRank", "/api/rank/search");

                put("allPosition", "/api/position/list");
                put("addPosition", "/api/position/add");
                put("editPosition", "/api/position/edit");
                put("deletePosition", "/api/position/delete");
                put("searchPosition", "/api/position/search");

                put("allUnit", "/api/unit/list");
                put("addUnit", "/api/unit/add");
                put("editUnit", "/api/unit/edit");
                put("deleteUnit", "/api/unit/delete");

                put("allTask", "/api/task/list/raw");
                put("addTask", "/api/task/add");
                put("editTask", "/api/task/edit");
                put("resizeTask", "/api/task/edit");
                put("moveTask", "/api/task/edit");
                put("deleteTask", "/api/task/delete");

                put("allSetting", "/api/setting");
                put("saveSetting", "/api/setting/save");

                put("minTime", "7:30");
                put("monthNamesShort", new String[] {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                        "Jul", "Ags", "Sep", "Okt", "Nov", "Des"});
                put("allDayText", "Sepanjang Hari");
                put("dayNames", new String[] {"Ahad", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"});
                put("themeName", "redmond");
                put("monthNames", new String[] {"Januari", "Februari", "Maret", "April", "Mei", "Juni",
                        "Juli", "Agustus", "September", "Oktober", "November", "Desember"});
                put("dayNamesShort", new String[] {"Ahad", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab"});
                put("editable", true);
                put("slotMinutes", 30);
                put("header", new HashMap<String, Object>() {{
                    put("center", "title");
                    put("left", "prev,next today");
                    put("right", "month");
                }});
                put("timeFormat", "H(:mm)");
                put("firstDay", "1");
                put("cronPeriod", "60");
                put("defaultView", "month");
                put("theme", false);
                put("axisFormat", "HH(:mm)");
                put("buttonText", new HashMap<String, Object>() {{
                    put("today", "hari ini");
                    put("month", "bulan");
                    put("day", "hari");
                    put("week", "minggu");
                }});
                put("firstHour", 8);
                put("maxTime", "21:00");
                put("selectable", false);
            }});
        }};

        Map<String, Object> variables = new HashMap<>();
        variables.put("options", new ObjectMapper().writeValueAsString(options));

        Handler<RoutingContext> calendarHandler = new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext routingContext) {
                try {
                    String template = Resources.toString(Resources.getResource("html/calendar.html"), Charsets.UTF_8);
                    String renderedTemplate = new Jinjava().render(template, variables);

                    routingContext.response()
                            .putHeader("content-type", "text/html")
                            .end(renderedTemplate);
                } catch (IOException e) {
                    logger.severe(e.getMessage());
                }

            }
        };

        router.get("/").handler(calendarHandler);
        router.get("/calendar").handler(calendarHandler);

        router.get("/user").handler(new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext routingContext) {
                try {
                    String template = Resources.toString(Resources.getResource("html/user.html"), Charsets.UTF_8);
                    String renderedTemplate = new Jinjava().render(template, variables);

                    routingContext.response()
                            .putHeader("content-type", "text/html")
                            .end(renderedTemplate);
                } catch (IOException e) {
                    logger.severe(e.getMessage());
                }

            }
        });

        router.get("/event").handler(new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext routingContext) {
                try {
                    String template = Resources.toString(Resources.getResource("html/event.html"), Charsets.UTF_8);
                    String renderedTemplate = new Jinjava().render(template, variables);

                    routingContext.response()
                            .putHeader("content-type", "text/html")
                            .end(renderedTemplate);
                } catch (IOException e) {
                    logger.severe(e.getMessage());
                }

            }
        });

        vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest httpServerRequest) {
                router.accept(httpServerRequest);
            }
        }).listen(8080);
    }

    public static void main(String[] args) {
        String clz = Server.class.getName();
        VertxOptions options = new VertxOptions().setClustered(false);

        Consumer<Vertx> runner = new Consumer<Vertx>() {
            @Override
            public void accept(Vertx vertx) {
                try {
                    vertx.deployVerticle(clz);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        };

        runner.accept(Vertx.vertx(options));
    }
}
