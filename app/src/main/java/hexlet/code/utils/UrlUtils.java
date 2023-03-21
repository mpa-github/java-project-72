package hexlet.code.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtils {

    public static String getMainUrlPart(String urlFullString) throws MalformedURLException {
        URL url = new URL(urlFullString);
        String protocol = url.getProtocol();
        String domainName = url.getHost();
        int portNumber = url.getPort();
        if (protocol.isEmpty() || domainName.isEmpty()) {
            throw new MalformedURLException("Malformed URL");
        }
        String port = (portNumber == -1) ? "" : ":" + portNumber;
        return "%s://%s%s".formatted(protocol, domainName, port);
    }
}
