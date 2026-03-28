package server.dao;

import model.user.Bidder;
import model.user.User;

import java.util.ArrayList;
import java.util.List;

public class UserDao {
    // Tạm thời dùng List lưu trên RAM. Sau này nhóm bạn thay ruột các hàm này bằng lệnh SQL nhé!
    private static List<User> database = new ArrayList<>();

    // Hàm đăng ký
    public static boolean register(String username, String password) {
        // 1. Kiểm tra xem username đã có ai dùng chưa
        for (User u : database) {
            if (u.getUsername().equals(username)) {
                return false; // Trùng tên, từ chối!
            }
        }
        // 2. Tạo ID tự tăng và lưu vào danh sách
        int newId = database.size() + 1;
        database.add(new Bidder(newId, username, password, "default@email.com"));
        System.out.println("🗄️ [DATABASE]: Đã lưu tài khoản mới: " + username);
        return true;
    }

    // Hàm đăng nhập
    public static boolean login(String username, String password) {
        for (User u : database) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                return true; // Đúng thông tin
            }
        }
        return false; // Sai tên hoặc mật khẩu
    }
}