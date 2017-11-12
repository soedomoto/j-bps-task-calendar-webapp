package id.go.bps.calendar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import id.go.bps.calendar.handler.*;
import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.vertx.AtomixClusterManager;
import io.atomix.vertx.ClusterSerializableSerializer;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class Server extends AbstractVerticle {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final Integer port;

    public Server(Integer port) {
        this.port = port;
    }

    @Override
    public void start(Future<Void> future) throws Exception {
        Router router = Router.router(vertx);

        new EventHandler(router, "localhost", 50051).setup();
        new PositionHandler(router, "localhost", 50051).setup();
        new RankHandler(router, "localhost", 50051).setup();
        new SettingHandler(router, "localhost", 50051).setup();
        new TaskHandler(router, "localhost", 50051).setup();
        new UnitHandler(router, "localhost", 50051).setup();
        new UserHandler(router, "localhost", 50051).setup();

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
        }).listen(port, result -> {
            if (result.succeeded()) {
                future.complete();
            } else {
                future.fail(result.cause());
            }
        });
    }

    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("h", "help", false, "print this message.");
        options.addOption("p", "ports", true, "Set server port range with " +
                "format \"start[-end]\". Default: 8080.");
        options.addOption("P", "registry-port", true, "Set service registry server " +
                "port. Default: 8801.");
        options.addOption("j", "join", true, "Join to cluster registry server with format " +
                "host:port. Default: blank.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);
        HelpFormatter formatter = new HelpFormatter();

        if(cmd.hasOption("help")) {
            formatter.printHelp( "bps-task-calendar-server", options);
            return;
        }

        String portRange = cmd.getOptionValue("p", "8080");
        String[] startEnd = portRange.split("-");
        String joinHost = null;
        Integer serviceRegistryPort = null, startPort = null, endPort = null, joinPort = null;
        try {
            serviceRegistryPort = Integer.valueOf(cmd.getOptionValue("P", "8801"));
            String joinURI = cmd.getOptionValue("j");
            if (joinURI != null && ! joinURI.equalsIgnoreCase("")) {
                String[] joinHP = joinURI.split(":");
                if (joinHP.length == 2) {
                    joinHost = joinHP[0];
                    joinPort = Integer.parseInt(joinHP[1]);
                } else {
                    logger.severe("Invalid join URI format !!!");
                    return;
                }
            }

            if (startEnd.length == 2) {
                startPort = Integer.valueOf(startEnd[0]);
                endPort = Integer.valueOf(startEnd[1]);
            } else {
                startPort = Integer.valueOf(startEnd[0]);
            }
        } catch (NumberFormatException e) {
            logger.severe(e.getMessage());
            return;
        }

        // Run single atomix server
        if (serviceRegistryPort == null) {
            logger.severe("No service registry server port defined !!!");
            return;
        }

        AtomixReplica replica = AtomixReplica.builder(new Address("0.0.0.0", serviceRegistryPort))
                .withTransport(new NettyTransport())
                .withStorage(Storage.builder()
                        .withDirectory(System.getProperty("user.dir") + "/logs/" + UUID.randomUUID().toString())
                        .build())
                .build();

        CompletableFuture<AtomixReplica> future;
        if (joinHost != null && joinPort != null) {
            future = replica.join(new Address(joinHost, joinPort));
        } else {
            future = replica.bootstrap();
        }

        // Run multiple GRPC servers
        List<Integer> ports = new ArrayList<Integer>();
        if (startPort != null) {
            if (endPort != null) {
                for(int p=startPort; p<=endPort; p++) {
                    ports.add(p);
                }
            } else {
                ports.add(startPort);
            }
        }

        if (ports.size() == 0) {
            logger.severe("No port(s) defined !!!");
            return;
        }

        future.thenRun(new Runnable() {
            @Override
            public void run() {
                VertxOptions options = new VertxOptions().setClusterManager(new AtomixClusterManager(replica));
                Vertx.clusteredVertx(options, new Handler<AsyncResult<Vertx>>() {
                    @Override
                    public void handle(AsyncResult<Vertx> res) {
                        if (res.succeeded()) {
                            Vertx vertx = res.result();
                            for(Integer port : ports) {
                                vertx.deployVerticle(new Server(port));
                            }
                        }
                    }
                });
            }
        });
    }
}
