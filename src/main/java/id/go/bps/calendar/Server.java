package id.go.bps.calendar;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;

import java.util.function.Consumer;

public class Server extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        router.get("/home").handler(routingContext -> {
            routingContext.response().putHeader("content-type", "text/html").end("Hello World!");
        });

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    public static void main(String[] args) {
        String clz = Server.class.getName();
        VertxOptions options = new VertxOptions().setClustered(false);

        Consumer<Vertx> runner = vertx -> {
            try {
                vertx.deployVerticle(clz);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };

        runner.accept(Vertx.vertx(options));
    }
}
