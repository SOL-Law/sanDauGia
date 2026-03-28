package client;

import com.google.gson.Gson;
import network.Request;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8888)) {
            System.out.println("Đã vào phòng đấu giá!");
            Gson gson = new Gson();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // 1. TẠO LUỒNG CHẠY NGẦM ĐỂ LẮNG NGHE SERVER (Observer)
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String fromServer;
                    while ((fromServer = in.readLine()) != null) {
                        // Khi Server phát sóng, dòng này sẽ được in ra
                        System.out.println("\n🔔 [TIN NHẮN REALTIME]: " + fromServer);
                        System.out.print("Nhập số tiền bạn muốn đấu giá: "); // In lại dấu nhắc
                    }
                } catch (Exception e) {
                    System.out.println("Mất kết nối với Server.");
                }
            }).start();

            // 2. LUỒNG CHÍNH ĐỂ NGƯỜI DÙNG NHẬP GIÁ TỪ BÀN PHÍM
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Nhập số tiền bạn muốn đấu giá: ");
                String price = scanner.nextLine();

                // Tạo gói tin PLACE_BID và gửi đi
                Request bidReq = new Request("PLACE_BID", "{\"price\": " + price + "}");
                out.println(gson.toJson(bidReq));
            }

        } catch (Exception e) {
            System.out.println("Không thể kết nối. Hãy bật AuctionServer trước!");
        }
    }
}