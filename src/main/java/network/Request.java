package network;
public class Request {
    private String action;   // Lệnh cần làm (VD: "LOGIN", "PLACE_BID")
    private String payload;  // Dữ liệu mang theo (chuỗi JSON của User, Item...)

    public Request(String action, String payload) {
        this.action = action;
        this.payload = payload;
    }

    public String getAction() { return action; }
    public String getPayload() { return payload; }
}