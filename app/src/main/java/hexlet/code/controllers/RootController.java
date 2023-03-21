package hexlet.code.controllers;

import io.javalin.http.Handler;

public final class RootController {

    public static Handler rootPage = ctx -> {
        ctx.render("index.html");
    };
}
