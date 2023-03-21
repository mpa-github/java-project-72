package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import hexlet.code.utils.UrlUtils;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class UrlController implements CrudHandler {

    @Override // <--- POST "/urls"
    public void create(@NotNull Context ctx) {
        String urlFullString = ctx.formParam("url");
        String url;
        try {
            url = UrlUtils.getMainUrlPart(urlFullString);
        } catch (MalformedURLException ex) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.sessionAttribute("incorrectUrl", urlFullString);
            //ctx.attribute("incorrectUrl", urlFullString);
            ctx.redirect("/"); // ---> GET "/"
            //ctx.render("index.html");
            return;
        }

        Url existedUrl = new QUrl()
            .name.equalTo(url)
            .findOne();
        if (existedUrl == null) {
            Url newUrl = new Url(url);
            newUrl.save();
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "info");
        } else {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect("/urls"); // ---> GET "/urls"
    }

    @Override
    public void delete(@NotNull Context ctx, @NotNull String id) {

    }

    @Override // <--- GET "/urls"
    public void getAll(@NotNull Context ctx) {
        List<Url> urls = new QUrl()
            .orderBy()
            .id.asc()
            .findList();

        ctx.attribute("urls", urls);
        ctx.render("urls/index.html");
    }

    @Override // <--- GET /urls/{id}
    public void getOne(@NotNull Context ctx, @NotNull String id) {
        Url existedUrl = new QUrl()
            .id.equalTo(Integer.parseInt(id))
            .findOne();

        LocalDateTime dateTime = LocalDateTime.ofInstant(existedUrl.getCreatedAt(), ZoneId.systemDefault());
        ctx.attribute("url", existedUrl);
        ctx.attribute("createdAt", dateTime);
        ctx.render("urls/show.html");
    }

    @Override
    public void update(@NotNull Context ctx, @NotNull String id) {

    }
}
