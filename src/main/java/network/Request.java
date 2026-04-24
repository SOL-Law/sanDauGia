package network;

import java.io.Serializable;

public class Request implements Serializable {

    private String type;
    private String payload;

    public Request(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    // Method gốc đang dùng trong project mới
    public String getType() {
        return type;
    }

    // Method gốc đang dùng để lấy dữ liệu
    public String getPayload() {
        return payload;
    }

    // Method bổ sung để tương thích với code cũ đang gọi getAction()
    public String getAction() {
        return type;
    }
}