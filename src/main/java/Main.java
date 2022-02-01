import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Main {
    private static final String REX_STATIONS = "(.*)(class=\\\"num\\\">)([0-9]*)(.[^А-ЯЁа-яё]*)([А-ЯЁа-яё -]+)(.[^\\d]*|)([0-9]*|)([ a-z\\\"=]*|)(переход на станцию «|)([А-ЯЁа-яё -]*|)([А-ЯЁа-яё» \\\">]*|)(</span><span class=\\\"t-icon-metroln ln-|)(\\d|)(\\\" title=\\\"переход на станцию «|)([А-ЯЁа-яё -]*|)([А-ЯЁа-яё» \\\">-]*|)(</span><span class=\\\"t-icon-metroln ln-|)(\\d|)(\\\" title=\\\"переход на станцию «|)([А-ЯЁа-яё -]*|)(.*)";
    private static final String REX_LINES = "(.*line=\")([0-9A-Z]*)(\\\">)([А-ЯЁа-яё0-9 -]+)(<\\/span>)";
    private static List<String[]> listConnections = new ArrayList<>();

    public static void main(String[] args) throws IOException, JSONException {
        Document document = Jsoup.connect("https://www.moscowmap.ru/metro.html#lines").maxBodySize(0).get();
        List<String> listStations = new ArrayList<>();
        List<String> listLines = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();

        document.select("a[data-metrost]").stream().forEach(a -> listStations.add(a.toString()));
        document.select("span[data-line]").stream().forEach(a -> listLines.add(a.toString()));
        JSONObject jsonStations = getJSONStations(getListStations(listStations), getListLines(listLines));
        jsonObject.put("stations" , jsonStations);

        try {
            FileWriter file = new FileWriter("data/stations_msk.json");
            file.write(jsonObject.toString());
            file.flush();
            file.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        //System.out.println(JSONObject.quote(jsonObject.toString()));
    }

    private static JSONObject getJSONStations(List<String[]> station, List<String[]> line) throws JSONException {
        JSONObject jsonStations = new JSONObject();
        int stationCount = 0;
        int lineCount = 0;
        for(int i = 0; i < station.size();i++){
            jsonStations.append(line.get(lineCount)[1], station.get(stationCount)[1]);
            stationCount++;
            if(stationCount < station.size() && station.get(stationCount)[0].equals("1")){
                lineCount++;
            }
        }
        return jsonStations;
    }

    private static List<String[]> getListStations(List<String> str){
        List<String[]> strings = new ArrayList<>();
        for (int i = 0; i < str.size(); i++){
            String[] str1 = Pattern.compile(REX_STATIONS)
                    .matcher(str.get(i))
                    .replaceAll("$3#$5")
                    .split("#");
            strings.add(str1);
        }
        return strings;
    }
    private static List<String[]> getListLines(List<String> str){
        List<String[]> strings = new ArrayList<>();
        for (int i = 0; i < str.size(); i++){
            String str1[] = Pattern.compile(REX_LINES)
                    .matcher(str.get(i))
                    .replaceAll("$2#$4")
                    .split("#");
            strings.add(str1);
        }
        return strings;
    }
}
