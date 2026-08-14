package com.university.sms.ui;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/** Copies uploaded student photos into a local folder and loads them back as scaled thumbnails. */
final class PhotoUtil {

    private static final Path PHOTOS_DIR = Paths.get("photos");

    private PhotoUtil() {
    }

    /**
     * Copies the given image file into the app's local photos folder under a
     * unique name, so it survives independently of where the user originally
     * picked it from. Returns the path to store in the database.
     */
    static String copyToPhotosFolder(File source) throws IOException {
        Files.createDirectories(PHOTOS_DIR);
        String extension = "";
        int dot = source.getName().lastIndexOf('.');
        if (dot >= 0) {
            extension = source.getName().substring(dot);
        }
        Path destination = PHOTOS_DIR.resolve(UUID.randomUUID() + extension);
        Files.copy(source.toPath(), destination);
        return destination.toString();
    }

    /** Loads a photo scaled to a size x size square icon, or null if there is no usable photo. */
    static ImageIcon loadThumbnail(String photoPath, int size) {
        if (photoPath == null || photoPath.isBlank()) {
            return null;
        }
        File file = new File(photoPath);
        if (!file.exists()) {
            return null;
        }
        ImageIcon original = new ImageIcon(photoPath);
        if (original.getIconWidth() <= 0) {
            return null;
        }
        Image scaled = original.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
