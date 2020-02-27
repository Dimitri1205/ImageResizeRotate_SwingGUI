package com.company;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;

public class Main {

    public  String IMAGE_FILE_NAME = "blast_500x500.jpg";

    public JFrame createSimpleSwingWindow() {
        JFrame frame = new JFrame("Swing test");
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel("This is a Swing test application");
        JButton button = new JButton("Click!");

        frame.add(label);
        frame.add(button);

        frame.pack();
        frame.setVisible(true);
        return frame;
    }

    public JFrame createSwingWindow() {
        JFrame frame = new JFrame("test Swing application");
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int shouldClose = JOptionPane.showConfirmDialog(frame, "Are you sure you want to close the window",
                        "Confirm Close", JOptionPane.YES_NO_OPTION);
                if (shouldClose == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });

        JLabel label = new JLabel("This is a Swing test application");
        JButton button = new JButton("Click!");

        ImageIcon icon = new ImageIcon(IMAGE_FILE_NAME);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));



        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        JLabel pctSizeLabel = new JLabel("Percent size: ");
        JTextField pctSizeField = new JTextField();
        JLabel rotationLabel = new JLabel("Degree rotation: ");
        JTextField rotationField = new JTextField();


        button.addActionListener(event -> adjustImage(pctSizeField, rotationField, iconLabel));

        panel.add(pctSizeLabel);
        panel.add(pctSizeField);
        panel.add(rotationLabel);
        panel.add(rotationField);

        frame.add(label);
        frame.add(iconLabel);
        frame.add(panel);
//        frame.add(button);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        buttonPane.add(button);

        JButton reloadButton = new JButton("Reload Image");
        reloadButton.addActionListener(event -> reloadImage(iconLabel));
        buttonPane.add(reloadButton);

        JButton saveImage = new JButton("save Image");
        saveImage.addActionListener(event -> saveImagetoFile(iconLabel));
        buttonPane.add(saveImage);

        frame.add(buttonPane);

        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        frame.pack();
        frame.setVisible(true);
        return frame;
    }

    private void adjustImage(JTextField sizeField, JTextField degreeField, JLabel imagalabel) {
        Icon icon = imagalabel.getIcon();
        ImageIcon imageIcon = (ImageIcon) icon;
        Image image = imageIcon.getImage();

        BufferedImage bufferedImage = convertToBufferedImage(image);

        bufferedImage = resizeImage(bufferedImage, getIntFromTextFields(sizeField, 100));
        bufferedImage = rotateImgeOnCenter(bufferedImage, getIntFromTextFields(degreeField, 0));

        imagalabel.setIcon(new ImageIcon(bufferedImage));
    }

    private BufferedImage convertToBufferedImage(Image image) {
        if(image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = bufferedImage.getGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        return bufferedImage;
    }

    private BufferedImage resizeImage(BufferedImage image, int percent) {
        double scale = percent / 100.0;

        AffineTransform resize = AffineTransform.getScaleInstance(scale, scale);
        AffineTransformOp op = new AffineTransformOp(resize, AffineTransformOp.TYPE_BICUBIC);

        return op.filter(image, null);
    }

    private BufferedImage rotateImage(BufferedImage bufferedImage, int degrees) {
        double radians = Math.toRadians(degrees);

        AffineTransform rotate = AffineTransform.getRotateInstance(radians);
        AffineTransformOp op = new AffineTransformOp(rotate, AffineTransformOp.TYPE_BILINEAR);

        return op.filter(bufferedImage, null);
    }

    private BufferedImage rotateImgeOnCenter(BufferedImage bufferedImage, int degrees) {
        double radians = Math.toRadians(degrees);
        AffineTransform rotate = AffineTransform.getRotateInstance(radians);

        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();

        Rectangle newSize = rotate.createTransformedShape(new Rectangle(width, height)).getBounds();

        rotate = new AffineTransform();
        rotate.translate(newSize.width*0.5, newSize.height*0.5);
        rotate.rotate(radians);
        rotate.translate(-width*0.5, -height*0.5);

        AffineTransformOp op = new AffineTransformOp(rotate, AffineTransformOp.TYPE_BILINEAR);

        return op.filter(bufferedImage, null);
    }

    private int getIntFromTextFields(JTextField pctField, int defaultValue) {
        try {
            int number = Integer.parseInt(pctField.getText());
            return number;
        }catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void saveImage (Image image, File file) throws IOException {
        BufferedImage bufferedImage = convertToBufferedImage(image);
        ImageIO.write(bufferedImage, "png", file);
    }

    private void saveJPGImage (Image image, File file) throws IOException {
        BufferedImage bufferedImage = convertToBufferedImage(image);

        BufferedImage rgbImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        ColorConvertOp toRGB = new ColorConvertOp(null);
        toRGB.filter(bufferedImage, rgbImage);

        ImageIO.write(rgbImage, "jpg", file);
    }

    private boolean warnAboutReload = true;
    private void reloadImage(JLabel imageLabel) {
        if(warnAboutReload) {
            JLabel message = new JLabel("Warning, this will reload an image");
            JCheckBox noReloadWarn = new JCheckBox("Don't tell me again");
            JComponent[] controls = new JComponent[]{
                    message, noReloadWarn
            };
            JOptionPane.showMessageDialog(imageLabel.getParent(), controls, "Image will reload", JOptionPane.PLAIN_MESSAGE);
            warnAboutReload = !noReloadWarn.isSelected();
        }
        imageLabel.setIcon(new ImageIcon(IMAGE_FILE_NAME));
    }

    private void saveImagetoFile(JLabel imageLabel) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Image", "png");
        chooser.setFileFilter(filter);
        int returnValue = chooser.showSaveDialog(imageLabel.getParent());

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                ImageIcon icon = (ImageIcon) imageLabel.getIcon();
                saveImage(icon.getImage(), file);

                JOptionPane.showMessageDialog(imageLabel.getParent(), "File saved to: " + file, "File saved successfully", JOptionPane.PLAIN_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(imageLabel.getParent(), "Unable to save file: " + e.getMessage(), "Error", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }



    public static void main(String[] args) {

//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                Main test = new Main();
//                test.createSwingWindow();
//            }
//        });

        SwingUtilities.invokeLater(() -> {
            Main test = new Main();
            test.createSwingWindow();
        });

    }


}
