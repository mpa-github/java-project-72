package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    private static Javalin app;
    private static String rootAppUrl;
    private static Database database;
    private static MockWebServer mockWebServer;
    private static final String NEW_VALID_URL = "https://www.example.com";
    private static final String NEW_VALID_URL_WITH_PORT = "https://www.example.com:8080";
    private static final String GOOGLE_URL_IN_DB = "https://www.google.com";
    private static final String YANDEX_URL_IN_DB = "https://ya.ru";
    private static final String INVALID_URL = "com";
    private static final String TEST_RESOURCES_PATH = "./src/test/resources/";

    @BeforeAll
    public static void startTestApp() throws IOException {
        app = App.getApp();
        app.start(0);

        int currentPort = app.port();
        rootAppUrl = "http://localhost:" + currentPort;
        database = DB.getDefault();

        mockWebServer = new MockWebServer();
        MockResponse mockResponse = new MockResponse()
            .setBody(readMockHtmlFile("mock-index.html"))
            .setResponseCode(201);
        mockWebServer.enqueue(mockResponse);
        mockWebServer.start();
    }

    @AfterAll
    public static void stopTestApp() throws IOException {
        mockWebServer.shutdown();
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        database.script().run("/truncate-tables.sql");
        database.script().run("/seed-test-db.sql");
    }

    @Test
    void testCreateNewUrl() {
        HttpResponse<String> response = Unirest
            .post(rootAppUrl + "/urls")
            .field("url", NEW_VALID_URL)
            .asString();

        int expectedHttpCode = 302;
        assertEquals(expectedHttpCode, response.getStatus());
        assertEquals("/urls", response.getHeaders().getFirst("Location"));

        HttpResponse<String> responseAfterRedirect = Unirest
            .get(rootAppUrl + "/urls")
            .asString();

        int expectedHttpCodeAfterRedirect = 200;
        String htmlPageContent = responseAfterRedirect.getBody();

        assertEquals(expectedHttpCodeAfterRedirect, responseAfterRedirect.getStatus());
        assertThat(htmlPageContent).contains(NEW_VALID_URL);
        assertThat(htmlPageContent).contains("Страница успешно добавлена");

        Url actualUrl = new QUrl()
            .name.equalTo(NEW_VALID_URL)
            .findOne();

        assertNotNull(actualUrl);
        assertEquals(NEW_VALID_URL, actualUrl.getName());
    }

    @Test
    void testCreateSameUrl() {
        HttpResponse<String> response = Unirest
            .post(rootAppUrl + "/urls")
            .field("url", GOOGLE_URL_IN_DB)
            .asString();

        int expectedHttpCode = 302;
        assertEquals(expectedHttpCode, response.getStatus());
        assertEquals("/urls", response.getHeaders().getFirst("Location"));

        HttpResponse<String> responseAfterRedirect = Unirest
            .get(rootAppUrl + "/urls")
            .asString();

        int expectedHttpCodeAfterRedirect = 200;
        String htmlPageContent = responseAfterRedirect.getBody();

        assertEquals(expectedHttpCodeAfterRedirect, responseAfterRedirect.getStatus());
        assertThat(htmlPageContent).contains(GOOGLE_URL_IN_DB);
        assertThat(htmlPageContent).contains("Страница уже существует");
    }

    @Test
    void testShowUrl() {
        HttpResponse<String> response = Unirest
            .get(rootAppUrl + "/urls/2")
            .asString();

        String htmlPageContent = response.getBody();

        int expectedHttpCode = 200;
        assertEquals(expectedHttpCode, response.getStatus());
        assertThat(htmlPageContent).contains(YANDEX_URL_IN_DB);
        assertThat(htmlPageContent).contains("Запустить проверку");
    }

    @Test
    void testShowAllUrls() {
        HttpResponse<String> actualResponse = Unirest
            .get(rootAppUrl + "/urls")
            .asString();

        String htmlPageContent = actualResponse.getBody();

        assertThat(htmlPageContent).contains(GOOGLE_URL_IN_DB);
        assertThat(htmlPageContent).contains(YANDEX_URL_IN_DB);
    }

    @Test
    void testCreateUrlCheck() {
        String mockUrl = mockWebServer.url("/").toString().replaceAll("/$", "");

        HttpResponse<String> response1 = Unirest
            .post(rootAppUrl + "/urls")
            .field("url", mockUrl)
            .asString();

        HttpResponse<String> response = Unirest
            .post(rootAppUrl + "/urls/3/checks")
            .asString();

        int expectedHttpCode = 302;
        assertEquals(expectedHttpCode, response.getStatus());
        assertEquals("/urls/3", response.getHeaders().getFirst("Location"));

        HttpResponse<String> responseAfterRedirect = Unirest
            .get(rootAppUrl + "/urls/3")
            .asString();

        int expectedHttpCodeAfterRedirect = 200;
        String htmlPageContent = responseAfterRedirect.getBody();

        assertEquals(expectedHttpCodeAfterRedirect, responseAfterRedirect.getStatus());
        assertThat(htmlPageContent).contains("ID");
        assertThat(htmlPageContent).contains("1");
        assertThat(htmlPageContent).contains("Код ответа");
        assertThat(htmlPageContent).contains("201");

        Url actualUrl = new QUrl()
            .name.equalTo(mockUrl)
            .findOne();

        UrlCheck actualUrlCheck = new QUrlCheck()
            .url.equalTo(actualUrl)
            .findOne();

        assertNotNull(actualUrlCheck);
        assertEquals(mockUrl, actualUrlCheck.getUrl().getName());
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

    private static String readMockHtmlFile(String fileName) throws IOException {
        String pathString = TEST_RESOURCES_PATH + fileName;
        Path path = Paths.get(pathString).toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            throw new RuntimeException("The file '%s' does not exist!".formatted(path));
        }
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
