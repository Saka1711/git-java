import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
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
}