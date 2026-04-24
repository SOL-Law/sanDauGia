package client.ui.auction;

import com.google.gson.Gson;
import network.Request;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;

public class UploadDialog extends JDialog {

    private JTextField nameField;
    private JTextField priceField;
    private JLabel imagePreview;

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

        styleField(nameField, "Tên sản phẩm");
        styleField(priceField, "Giá khởi điểm");

        JButton chooseBtn = new JButton("Chọn ảnh");
        JButton uploadBtn = new JButton("Upload");

        imagePreview = new JLabel("Chưa chọn ảnh", SwingConstants.CENTER);
        imagePreview.setPreferredSize(new Dimension(200,150));

        chooseBtn.addActionListener(e -> chooseImage());
        uploadBtn.addActionListener(e -> upload(out, gson));

        panel.add(nameField);
        panel.add(priceField);
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

        String name = nameField.getText();
        String price = priceField.getText();

        String payload = String.format(
                "{\"name\":\"%s\",\"price\":%s}",
                name, price
        );

        Request req = new Request("UPLOAD_ITEM", payload);
        out.println(gson.toJson(req));

        JOptionPane.showMessageDialog(this, "Đã gửi sản phẩm!");
        dispose();
    }

    private void styleField(JTextField field, String title) {
        field.setMaximumSize(new Dimension(300,40));
        field.setBorder(BorderFactory.createTitledBorder(title));
    }
}