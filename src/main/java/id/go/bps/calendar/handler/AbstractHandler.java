package id.go.bps.calendar.handler;

import io.vertx.ext.web.Router;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class AbstractHandler {
    protected final Router router;

    public AbstractHandler(Router router) {
        this.router = router;
    }

    public void setup() {
        throw new NotImplementedException();
    }
}
