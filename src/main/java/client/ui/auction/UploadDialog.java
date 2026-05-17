package client.ui.auction;

import com.google.gson.Gson;
import network.Request;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import com.google.gson.JsonObject;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class UploadDialog extends JDialog {
    private JTextField nameField;
    private JTextField priceField;
    private JLabel imagePreview;
    private JTextField timeField;
    private JComboBox<String> categoryCombo;
    private File selectedFile;
    private String sellerName; // Lưu tên người đang đăng nhập

    public UploadDialog(JFrame parent, PrintWriter out, Gson gson, String sellerName) {
        super(parent, "Đăng sản phẩm", true);
        this.sellerName = sellerName; // Nhận tên người bán từ ngoài truyền vào

        setSize(400, 450); // Cho to ra xíu để nhét thêm Category
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

        // 1. Thêm Hộp chọn Danh mục
        String[] cats = {"Nghệ thuật", "Điện tử", "Xe cộ", "Khác"};
        categoryCombo = new JComboBox<>(cats);
        categoryCombo.setMaximumSize(new Dimension(300, 40));
        categoryCombo.setBorder(BorderFactory.createTitledBorder("Danh mục"));

        JButton chooseBtn = new JButton("Chọn ảnh");
        JButton uploadBtn = new JButton("Upload");

        imagePreview = new JLabel("Chưa chọn ảnh", SwingConstants.CENTER);
        imagePreview.setPreferredSize(new Dimension(200, 150));

        chooseBtn.addActionListener(e -> chooseImage());
        uploadBtn.addActionListener(e -> upload(out, gson)); // Gọi hàm upload ở dưới

        panel.add(nameField);
        panel.add(priceField);
        panel.add(categoryCombo); // Nhét cái Danh mục vào giao diện
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
            Image img = icon.getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH);
            imagePreview.setIcon(new ImageIcon(img));
            imagePreview.setText("");
        }
    }

    // 2. Gói đầy đủ JSON gửi lên Server
    private void upload(PrintWriter out, Gson gson) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Chọn ảnh!");
            return;
        }
        String name = nameField.getText().trim();
        String price = priceField.getText().trim();
        String time = timeField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();

        try {
            String base64Image = compressImageToBase64(selectedFile);

            JsonObject payloadObj = new JsonObject();
            payloadObj.addProperty("name", name);
            payloadObj.addProperty("price", price);
            payloadObj.addProperty("image", base64Image);
            payloadObj.addProperty("username", sellerName); // Thêm Người Bán
            payloadObj.addProperty("category", category);   // Thêm Danh Mục

            int duration = time.isEmpty() ? 60 : Integer.parseInt(time);
            payloadObj.addProperty("time", duration);

            Request req = new Request("UPLOAD_ITEM", gson.toJson(payloadObj));
            String jsonMessage = gson.toJson(req);

            dispose();
            new Thread(() -> out.println(jsonMessage)).start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xử lý ảnh!");
            ex.printStackTrace();
        }
    }

    private void styleField(JTextField field, String title) {
        field.setMaximumSize(new Dimension(300, 40));
        field.setBorder(BorderFactory.createTitledBorder(title));
    }

    private String compressImageToBase64(File file) {
        try {
            BufferedImage originalImage = ImageIO.read(file);
            int targetWidth = 100;
            int targetHeight = 100;
            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            return "";
        }
    }
}