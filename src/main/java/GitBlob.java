import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class GitBlob {
    public static String createBlob(String filePath) throws Exception {
        byte[] content = Files.readAllBytes(Paths.get(filePath));
        String header = "blob " + content.length + "\0";
        byte[] blob = GitUtils.concatenate(header.getBytes(), content);

        String sha1Hash = GitUtils.computeSHA1(blob);
        String dir = ".git/objects/" + sha1Hash.substring(0, 2);
        String file = sha1Hash.substring(2);

        Files.createDirectories(Paths.get(dir));
        try (OutputStream os = new FileOutputStream(dir + "/" + file);
             DeflaterOutputStream dos = new DeflaterOutputStream(os)) {
            dos.write(blob);
        }
        return sha1Hash;
    }

    public static void readBlob(String blobSha) {
        if (blobSha.length() != 40) {
            System.out.println("Invalid hash: " + blobSha);
            return;
        }
        File blobFile = new File(".git/objects/" + blobSha.substring(0, 2) + "/" + blobSha.substring(2));
        if (!blobFile.exists()) {
            System.out.println("Object not found: " + blobSha);
            return;
        }
        try (FileInputStream fis = new FileInputStream(blobFile);
             InflaterInputStream iis = new InflaterInputStream(fis);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            iis.transferTo(baos);
            String decompData = baos.toString();
            System.out.print(decompData.substring(decompData.indexOf('\0') + 1));
        } catch (IOException e) {
            throw new RuntimeException("Error Reading Blob File", e);
        }
    }
}
