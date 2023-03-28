package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.utils.HtmlUtils;
import hexlet.code.utils.UrlUtils;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

public final class UrlController {

    // <--- GET "/urls"
    public static Handler showAllUrls = ctx -> {
        List<Url> urls = new QUrl()
            //.orderBy() // TODO .orderBy or sort (?)
            //.id.asc()
            .findList();

        urls.sort(Comparator.comparing(Url::getId));
        ctx.attribute("urls", urls);
        ctx.render("urls/index.html");
    };

    // <--- GET /urls/{id}
    public static Handler showUrl = ctx -> {
        long urlId = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url existedUrl = new QUrl()
            .id.equalTo(urlId)
            .findOne();

        // TODO should check this?
        /*if (existedUrl == null) {
            ctx.sessionAttribute("flash", "Страница не найдена");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.render("index.html");
            return;
        }*/

        if (existedUrl.getUrlChecks().size() > 1) {
            existedUrl.getUrlChecks().sort(Comparator.comparing(UrlCheck::getCreatedAt).reversed());
        }
        LocalDateTime dateTime = LocalDateTime.ofInstant(existedUrl.getCreatedAt(), ZoneId.systemDefault());
        ctx.attribute("url", existedUrl);
        ctx.attribute("dateTime", dateTime);
        ctx.render("urls/show.html");
    };

    // <--- POST "/urls"
    public static Handler createUrl = ctx -> {
        String urlFullString = ctx.formParam("url");
        String url;
        try {
            url = UrlUtils.getMainUrlPart(urlFullString);
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.sessionAttribute("incorrectUrl", urlFullString);
            ctx.redirect("/"); // ---> GET "/"
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
    };

    // <--- POST "/urls/{id}/checks"
    public static Handler createUrlCheck = ctx -> {
        long urlId = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url existedUrl = new QUrl()
            .id.equalTo(urlId)
            .findOne();

        // TODO should check this?
        /*if (existedUrl == null) {
            ctx.render("index.html");
            return;
        }*/

        try {
            HttpResponse<String> response = Unirest.get(existedUrl.getName()).asString();
            HtmlUtils htmlParser = new HtmlUtils(response.getBody());
            int httpStatusCode = response.getStatus();
            String title = htmlParser.getTitleContent();
            String h1 = htmlParser.getFirstH1TagContent();
            String description = htmlParser.getMetaDescriptionContent();
            UrlCheck check = new UrlCheck(httpStatusCode, title, h1, description, existedUrl);
            check.save();
        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Некорректный адрес");
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect("/urls/" + urlId); // ---> GET "/urls/{id}"
    };
}
