import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.InflaterInputStream;

public class GitTree {
    public static void listTree(String treeSha) {
        File treeFile = new File(".git/objects/" + treeSha.substring(0, 2) + "/" + treeSha.substring(2));
        if (!treeFile.exists()) {
            System.out.println("Tree Object not found: " + treeSha);
            return;
        }
        try (FileInputStream fis = new FileInputStream(treeFile);
             InflaterInputStream iis = new InflaterInputStream(fis);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            iis.transferTo(baos);
            byte[] decompData = baos.toByteArray();
            List<String> entries = GitUtils.parseTreeEntries(decompData);
            entries.forEach(System.out::println);
        } catch (IOException e) {
            throw new RuntimeException("Error Reading Tree File", e);
        }
    }

    public static String writeTree(Path dir) throws Exception {
        List<byte[]> entries = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (entry.getFileName().toString().equals(".git")) {
                    continue;
                }
                String sha1 = Files.isDirectory(entry) ? writeTree(entry) : GitBlob.createBlob(entry.toString());
                String mode = Files.isDirectory(entry) ? "40000" : "100644";
                byte[] entryBytes = GitUtils.concatenate((mode + " " + entry.getFileName() + "\0").getBytes(), GitUtils.hexStringToBytes(sha1));
                entries.add(entryBytes);
            }
        }
        byte[] treeContent = GitUtils.buildTreeContent(entries);
        return GitUtils.storeObject(treeContent, "tree");
    }
}
