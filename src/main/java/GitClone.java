
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.*;

public class GitClone {
    public static void cloneRepository(String repoUrl, String targetDir) throws Exception {
        System.out.println("Cloning " + repoUrl + " into " + targetDir);
        
        Files.createDirectories(Paths.get(targetDir, ".git"));
        fetchGitPack(repoUrl, targetDir);
    }

    private static void fetchGitPack(String repoUrl, String targetDir) throws Exception {
        String packUrl = repoUrl + "/infoo/refs?service=git-upload-pack";
        HttpURLConnection conn = (HttpURLConnection) URI.create(packUrl).toURL().openConnection();
        conn.setRequestProperty("User-Agent", "GitClone/1.0");
        conn.setRequestProperty("Accept", "application/x-git-upload-pack-advertisement");

        try (InputStream inputStream = conn.getInputStream()) {
            Path packFile = Paths.get(targetDir, ".git", "packfile");
            Files.copy(inputStream, packFile, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Packfile downloaded to " + packFile);
        }
    }
}