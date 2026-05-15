package client.ui.auction;

import com.google.gson.Gson;
import network.Request;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;

import java.util.Base64;
import java.nio.file.Files;
import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class UploadDialog extends JDialog {

    private JTextField nameField;
    private JTextField priceField;
    private JLabel imagePreview;
    private JTextField timeField;

    private File selectedFile;

    public UploadDialog(JFrame parent, PrintWriter out, Gson gson) {
        super(parent, "Đăng sản phẩm", true);

        setSize(400, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        nameField = new JTextField();
        priceField = new JTextField();
        timeField = new JTextField();

        styleField(nameField, "Tên sản phẩm");
        styleField(priceField, "Giá khởi điểm");
        styleField(timeField, "Thời gian đấu giá (giây)");

        JButton chooseBtn = new JButton("Chọn ảnh");
        JButton uploadBtn = new JButton("Upload");

        imagePreview = new JLabel("Chưa chọn ảnh", SwingConstants.CENTER);
        imagePreview.setPreferredSize(new Dimension(200,150));

        chooseBtn.addActionListener(e -> chooseImage());
        uploadBtn.addActionListener(e -> upload(out, gson));

        panel.add(nameField);
        panel.add(priceField);
        panel.add(timeField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(chooseBtn);
        panel.add(imagePreview);
        panel.add(Box.createVerticalStrut(10));
        panel.add(uploadBtn);

        add(panel, BorderLayout.CENTER);
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();

            ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(200,150,Image.SCALE_SMOOTH);
            imagePreview.setIcon(new ImageIcon(img));
            imagePreview.setText("");
        }
    }

    private void upload(PrintWriter out, Gson gson) {

        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Chọn ảnh!");
            return;
        }

        String name = nameField.getText().trim();
        String price = priceField.getText().trim();
        String time = timeField.getText().trim();

        try {
            String base64Image = compressImageToBase64(selectedFile);

            JsonObject payloadObj = new JsonObject();
            payloadObj.addProperty("name", name);
            payloadObj.addProperty("price", price);
            payloadObj.addProperty("image", base64Image);

            // Nếu người dùng không nhập gì, mặc định là 60s
            int duration = time.isEmpty() ? 60 : Integer.parseInt(time);
            payloadObj.addProperty("time", duration);

            Request req = new Request("UPLOAD_ITEM", gson.toJson(payloadObj));
            String jsonMessage = gson.toJson(req);

            // 🔥 1. ĐÓNG CỬA SỔ NGAY LẬP TỨC ĐỂ GIẢI PHÓNG GIAO DIỆN CHÍNH
            dispose();

            // 🔥 2. GỬI DỮ LIỆU ĐI BẰNG LUỒNG RIÊNG
            new Thread(() -> {
                out.println(jsonMessage);
            }).start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xử lý ảnh!");
            ex.printStackTrace();
        }
    }

    private void styleField(JTextField field, String title) {
        field.setMaximumSize(new Dimension(300,40));
        field.setBorder(BorderFactory.createTitledBorder(title));
    }
    // NÉN ẢNH

    private String compressImageToBase64(File file) {
        try {
            // 1. Đọc ảnh gốc siêu nặng
            BufferedImage originalImage = ImageIO.read(file);

            // 2. Tạo một bức tranh trắng tinh kích thước nhỏ xíu (100x100)
            int targetWidth = 100;
            int targetHeight = 100;
            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

            // 3. Vẽ lại ảnh gốc thu nhỏ vào bức tranh trắng
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();

            // 4. Biến bức tranh thu nhỏ thành mảng Byte (ép chất lượng xuống file JPG)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            // 5. Mã hóa thành Base64 (Chuỗi lúc này chỉ còn khoảng 10.000 ký tự thay vì 7 triệu!)
            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (Exception e) {
            System.out.println("Lỗi nén ảnh: " + e.getMessage());
            return ""; // Nếu lỗi thì trả về chuỗi rỗng
        }
    }
}