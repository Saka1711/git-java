import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;

public class GitUtils {
    public static String computeSHA1(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return bytesToHex(md.digest(data));
    }

    public static byte[] concatenate(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays)
            length += array.length;
        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static List<String> parseTreeEntries(byte[] data) {
        List<String> entries = new ArrayList<>();
        int i = 0;
        while (i < data.length) {
            int spaceIndex = i;
            while (data[spaceIndex] != ' ')
                spaceIndex++;
            String mode = new String(data, i, spaceIndex - i);
            int nullIndex = spaceIndex + 1;
            while (data[nullIndex] != '\0')
                nullIndex++;
            String filename = new String(data, spaceIndex + 1, nullIndex - spaceIndex - 1);
            byte[] shaBytes = new byte[20];
            System.arraycopy(data, nullIndex + 1, shaBytes, 0, 20);
            String sha = bytesToHex(shaBytes);

            entries.add(mode + " " + filename + " " + sha);
            i = nullIndex + 21;
        }
        return entries;
    }

    public static byte[] buildTreeContent(List<byte[]> entries) {
        int totalLength = entries.stream().mapToInt(e -> e.length).sum();
        byte[] treeHeader = ("tree " + totalLength + "\0").getBytes();
        for (byte[] entry : entries) {
            treeHeader = concatenate(treeHeader, entry);
        }
        return treeHeader;
    }

    public static String storeObject(byte[] content, String type) throws Exception {
        String header = type + " " + content.length + "\0";
        byte[] fullContent = concatenate(header.getBytes(StandardCharsets.UTF_8), content);

        String sha1Hash = computeSHA1(fullContent);
        String dir = ".git/objects/" + sha1Hash.substring(0, 2);
        String file = sha1Hash.substring(2);
        Files.createDirectories(Paths.get(dir));

        try (OutputStream os = new FileOutputStream(dir + "/" + file);
                DeflaterOutputStream dos = new DeflaterOutputStream(os)) {
            dos.write(fullContent);
        }

        return sha1Hash;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static byte[] hexStringToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
