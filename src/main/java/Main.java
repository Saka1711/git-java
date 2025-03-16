import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.err.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    final String command = args[0];

    switch (command) {
      case "init" -> {
        initializeGit();
      }
      case "cat-file" -> {
        if (args.length != 3 || !args[1].equals("-p")) {
          System.out.println("Usage: cat-file -p <hash>");
          return;
        }
        String blobSha = args[2];
        readBlob(blobSha);
      }
      case "hash-object" -> {
        if(args.length != 3 || !args[1].equals("-w")){
          System.out.println("Usage: hash-object -w <file>");
          return;
        }
        try {
          String sha1Hash = createGitBlob(args[2]);
          System.out.println(sha1Hash);
        } catch (Exception e) {
          throw new RuntimeException("Error creating Git Blob", e);
        }
      }
      default -> System.out.println("Unknown command: " + command);
    }
  }

  private static void initializeGit() {
    final File root = new File(".git");
    new File(root, "objects").mkdirs();
    new File(root, "refs").mkdirs();
    final File head = new File(root, "HEAD");

    try {
      head.createNewFile();
      Files.write(head.toPath(), "ref: refs/heads/main\n".getBytes());
      System.out.println("Initialized git directory");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void readBlob(String blobSha) {
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

  private static String createGitBlob(String filePath) throws Exception{
    byte[] content = Files.readAllBytes(Paths.get(filePath));
    String header = "blob " + content.length + "\0";
    byte[] blob = concatenate(header.getBytes(), content);
    
    MessageDigest md = MessageDigest.getInstance("SHA-1");
    byte[] sha1Bytes = md.digest(blob);
    String sha1Hash = bytesToHex(sha1Bytes);

    String dir = ".git/objects/" + sha1Hash.substring(0, 2);
    String file = sha1Hash.substring(2);

    Files.createDirectories(Paths.get(dir));
    try(OutputStream os = new FileOutputStream(dir + "/" + file);
        DeflaterOutputStream dos = new DeflaterOutputStream(os)
    ){
      dos.write(blob);
    }
    return sha1Hash;
  }

  private static byte[] concatenate(byte[] a, byte[] b) {
    byte[] result = new byte[a.length + b.length];
    System.arraycopy(a, 0, result, 0, a.length);
    System.arraycopy(b, 0, result, a.length, b.length);
    return result;
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}