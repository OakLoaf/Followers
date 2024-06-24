package org.lushplugins.followers.utils;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebUtils {
    private static final Pattern FILENAME_PATTERN = Pattern.compile("filename=\"(.+)\"");

    public static File downloadFile(URL downloadUrl, File output) throws IOException {
        return downloadFile(downloadUrl, output, "filename.jar");
    }

    public static File downloadFile(URL downloadUrl, File output, String defaultFileName) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
        connection.addRequestProperty("User-Agent", "OakLoaf/FollowerPets/2.0.0");

        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Response code was " + connection.getResponseCode());
        }

        ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());

        String fieldValue = connection.getHeaderField("Content-Disposition");
        String fileName;
        if (fieldValue != null && fieldValue.contains("filename=\"")) {
            Matcher matcher = FILENAME_PATTERN.matcher(fieldValue);
            if (matcher.find()) {
                fileName = matcher.group();
            } else {
                fileName = defaultFileName;
            }
        } else {
            fileName = defaultFileName;
        }

        Bukkit.getLogger().info("Saving '" + fileName + "' to '" + output.getAbsolutePath() + "'");
        File fileOutput = new File(output, fileName);
        FileOutputStream fos = new FileOutputStream(fileOutput);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();

        return fileOutput;
    }
}
