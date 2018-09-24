package minimini.cc.wg.model;

import java.util.List;

/**
 * Created by gaotian on 2018/9/23.
 */

public class Api {

    private String log_id;

    private String direction;

    private int words_result_num;

    private List<Result> words_result;

    private int language;

    public String getLog_id() {
        return log_id;
    }

    public void setLog_id(String log_id) {
        this.log_id = log_id;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getWords_result_num() {
        return words_result_num;
    }

    public void setWords_result_num(int words_result_num) {
        this.words_result_num = words_result_num;
    }

    public List<Result> getWords_result() {
        return words_result;
    }

    public void setWords_result(List<Result> words_result) {
        this.words_result = words_result;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }
}
