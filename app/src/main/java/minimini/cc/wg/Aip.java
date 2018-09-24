package minimini.cc.wg;

import com.baidu.aip.ocr.AipOcr;

/**
 * Created by gaotian on 2018/9/23.
 */

public class Aip {
    //设置APPID/AK/SK
    public static final String APP_ID = "11604122";
    public static final String API_KEY = "UWw67efEuKsd0WyDqiyAqBSj";
    public static final String SECRET_KEY = "UjY4vjqoBb149iWhU1VxXn5PjLDqysvx";

    private AipOcr client;

    public Aip() {
        // 初始化一个AipOcr
        client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);

        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
    }

    public AipOcr getClient() {
        return client;
    }
}
