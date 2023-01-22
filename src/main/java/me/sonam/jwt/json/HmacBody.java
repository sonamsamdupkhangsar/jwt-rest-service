package me.sonam.jwt.json;

public class HmacBody {
    private String algorithm;
    private String data;
    private String key;

    public HmacBody(String algorithm, String data, String key) {
        this.algorithm = algorithm;
        this.data = data;
        this.key = key;
    }

    public HmacBody() {

    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getData() {
        return data;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "HmacBody{" +
                "algorithm='" + algorithm + '\'' +
                ", data='" + data + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
