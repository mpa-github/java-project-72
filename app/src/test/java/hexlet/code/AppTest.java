package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    private static Javalin app;
    private static String rootAppUrl;
    private static Database database;
    private static final String URLS_PATH = "/urls";
    private static final String VALID_URL = "https://www.example1.com";
    private static final String VALID_URL_WITH_PORT = "https://www.example2.com:8080";
    private static final String URL_ALREADY_IN_DB = "https://www.google.com";
    private static final String INVALID_URL = "com";

    @BeforeAll
    public static void startTestApp() {
        app = App.getApp();
        app.start(0);

        int currentPort = app.port();
        rootAppUrl = "http://localhost:" + currentPort;
        database = DB.getDefault();
    }

    @AfterAll
    public static void stopTestApp() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        database.script().run("/truncate-url.sql");
        database.script().run("/seed-test-db.sql");
    }

    @Test
    void testCreateNewUrl() {
        HttpResponse<String> response = Unirest
            .post(rootAppUrl + URLS_PATH)
            .field("url", VALID_URL)
            .asString();

        int expectedHttpCode = 302;
        assertEquals(expectedHttpCode, response.getStatus());
        assertEquals(URLS_PATH, response.getHeaders().getFirst("Location"));

        HttpResponse<String> responseAfterRedirect = Unirest
            .get(rootAppUrl + URLS_PATH)
            .asString();

        int expectedHttpCodeAfterRedirect = 200;
        String htmlPageContent = responseAfterRedirect.getBody();

        assertEquals(expectedHttpCodeAfterRedirect, responseAfterRedirect.getStatus());
        assertThat(htmlPageContent).contains(VALID_URL);
        assertThat(htmlPageContent).contains("Страница успешно добавлена");

        Url actualUrl = new QUrl()
            .name.equalTo(VALID_URL)
            .findOne();

        assertNotNull(actualUrl);
        assertEquals(VALID_URL, actualUrl.getName());
    }

    @Test
    void testCreateSameUrl() {
        HttpResponse<String> response = Unirest
            .post(rootAppUrl + URLS_PATH)
            .field("url", URL_ALREADY_IN_DB)
            .asString();

        int expectedHttpCode = 302;
        assertEquals(expectedHttpCode, response.getStatus());
        assertEquals(URLS_PATH, response.getHeaders().getFirst("Location"));

        HttpResponse<String> responseAfterRedirect = Unirest
            .get(rootAppUrl + URLS_PATH)
            .asString();

        int expectedHttpCodeAfterRedirect = 200;
        String htmlPageContent = responseAfterRedirect.getBody();

        assertEquals(expectedHttpCodeAfterRedirect, responseAfterRedirect.getStatus());
        assertThat(htmlPageContent).contains(URL_ALREADY_IN_DB);
        assertThat(htmlPageContent).contains("Страница уже существует");
    }

    @Test
    void testUrlShowPage() {
        HttpResponse<String> response = Unirest
            .get(rootAppUrl + URLS_PATH + "/1")
            .asString();

        String htmlPageContent = response.getBody();

        int expectedHttpCode = 200;
        assertEquals(expectedHttpCode, response.getStatus());
        assertThat(htmlPageContent).contains(URL_ALREADY_IN_DB);
        assertThat(htmlPageContent).contains("Запустить проверку");
    }

    @Test
    void testGetAllUrls() {
        HttpResponse<String> actualResponse = Unirest
            .get(rootAppUrl + URLS_PATH)
            .asString();

        String htmlPageContent = actualResponse.getBody();

        assertThat(htmlPageContent).contains("www.google.com");
        assertThat(htmlPageContent).contains("ya.ru");
    }

    @Test
    void testRootPage() {
        HttpResponse<String> actualResponse = Unirest
            .get(rootAppUrl)
            .asString();

        int expectedHttpStatus = 200;
        String htmlPageContent = actualResponse.getBody();

        assertEquals(expectedHttpStatus, actualResponse.getStatus());
        assertThat(htmlPageContent).contains("Бесплатно проверяйте сайты на SEO пригодность");
    }
}
