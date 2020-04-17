package nl.guuslieben.cn2tcp.core.util.net;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

public class NetworkUtil {

    public static String getRandomName(int playerNumber) {
        try {
            var names = readJsonFromUrl("http://names.drycodes.com/1");
            var obj = names.get(0);
            return obj.toString();
        } catch (IOException | JSONException e) {
            return "Player #" + playerNumber;
        }
    }

    private static String readAll(Reader rd) throws IOException {
        var sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONArray readJsonFromUrl(String url) throws IOException, JSONException {
        var is = new URL(url).openStream();
        try {
            var rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            var jsonText = readAll(rd);
            var json = new JSONArray(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public static String convertBytes(byte[] in) {
        var charData = new char[in.length];
        for (int i = 0; i < charData.length; i++) {
            charData[i] = (char) (((int) in[i]) & 0xFF);
        }
        return new String(charData).replaceAll("\u0000.*", "");
    }
}
