package hexlet.code.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public final class HtmlParser {

    private final Document document;

    public HtmlParser(String htmlPageContent) {
        this.document = Jsoup.parse(htmlPageContent);
    }

    public String getTitleContent() {
        return document.title();
    }

    public String getFirstH1TagContent() {
        Element h1Element = document.selectFirst("h1");
        if (h1Element == null) {
            return null;
        }
        String h1Content = h1Element.text();
        return h1Content.isEmpty() ? null : h1Content;
    }

    public String getMetaDescriptionContent() {
        Element metaElement = document.selectFirst("meta[name='description']");
        if (metaElement == null) {
            return null;
        }
        String descriptionContent = metaElement.attr("content");
        return descriptionContent.isEmpty() ? null : descriptionContent;
    }
}
