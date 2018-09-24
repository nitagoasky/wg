package minimini.cc.wg.http;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaotian on 2018/9/23.
 */

public class Answer {

    public static List<String> get(String key) {
        try
        {
            List<String> answer = new ArrayList<>();
            Document document = Jsoup.connect("http://www.51bc.net/cy/serach.php?f_type=DianGu&f_type2=&f_key=" + key).get();
            Elements elements = document.body().select("u");
            for (Element element: elements){
                answer.add(element.html());
            }
            return answer;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
