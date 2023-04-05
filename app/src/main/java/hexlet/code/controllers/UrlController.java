package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.utils.HtmlParser;
import hexlet.code.utils.UrlUtils;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

import java.net.MalformedURLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public final class UrlController {

    // <--- GET "/urls?page=value"
    public static Handler showAllUrls = ctx -> {
        int pageNumberToShow = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int rowsPerPage = 10;
        int rowsOffset = (pageNumberToShow - 1) * rowsPerPage;

        PagedList<Url> pagedUrls = new QUrl()
            .setFirstRow(rowsOffset)
            .setMaxRows(rowsPerPage)
            .orderBy().id.asc()
            .findPagedList();

        List<Url> urls = pagedUrls.getList();
        int totalRowsCount = pagedUrls.getTotalCount();
        List<Integer> pageNumbers = getPageNumbers(totalRowsCount, rowsPerPage);
        ctx.attribute("currentPage", pageNumberToShow);
        ctx.attribute("pageNumbers", pageNumbers);
        ctx.attribute("urls", urls);
        ctx.render("urls/index.html");
    };

    // <--- GET /urls/{id}
    public static Handler showUrl = ctx -> {
        long urlId = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url existedUrl = new QUrl()
            .id.equalTo(urlId)
            .findOne();

        if (existedUrl == null) {
            ctx.sessionAttribute("flash", "Страница не найдена");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/"); // ---> GET "/"
            return;
        }

        if (existedUrl.getUrlChecks().size() > 1) {
            existedUrl.getUrlChecks().sort(Comparator.comparing(UrlCheck::getCreatedAt).reversed());
        }
        ctx.attribute("url", existedUrl);
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

        try {
            HttpResponse<String> response = Unirest.get(existedUrl.getName()).asString();
            HtmlParser htmlParser = new HtmlParser(response.getBody());
            int httpStatusCode = response.getStatus();
            String title = htmlParser.getTitleContent();
            String h1 = htmlParser.getFirstH1TagContent();
            String description = htmlParser.getMetaDescriptionContent();
            UrlCheck check = new UrlCheck(httpStatusCode, title, h1, description, existedUrl);
            check.save();
            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");
        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Некорректный адрес");
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect("/urls/" + urlId); // ---> GET "/urls/{id}"
    };

    private static List<Integer> getPageNumbers(int totalItems, int itemsPerPage) {
        int lastPageNumber;
        if (totalItems % itemsPerPage == 0) {
            lastPageNumber = totalItems / itemsPerPage;
        } else {
            lastPageNumber = (totalItems / itemsPerPage) + 1;
        }
        int startInclusive = 1;
        int endExclusive = lastPageNumber + 1;
        return IntStream.range(startInclusive, endExclusive).boxed().toList();
    }
}
