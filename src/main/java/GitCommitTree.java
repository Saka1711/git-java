import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class GitCommitTree {
    public static void main(String[] args) throws Exception {
        if (args.length < 6 || !args[args.length - 3].equals("--author")) {
            System.err.println("Usage: commit-tree <tree_sha> [-p <parent_sha>] -m <message> --author <name> <email>");
            System.exit(1);
        }

        String treeSha = args[1];
        String parentSha = "";
        String message;
        String authorName = args[args.length - 2];
        String authorEmail = args[args.length - 1];

        if (args[2].equals("-p")) {
            if (args.length < 8 || !args[4].equals("-m")) {
                System.err.println("Invalid usage of -p. Correct format: commit-tree <tree_sha> [-p <parent_sha>] -m <message> --author <name> <email>");
                System.exit(1);
            }
            parentSha = args[3];
            message = args[5];
        } else {
            message = args[3]; // No parent case
        }

        String commitSha = createCommit(treeSha, parentSha, message, authorName, authorEmail);
        System.out.println(commitSha);
    }

    public static String createCommit(String treeSha, String parentSha, String message, String authorName, String authorEmail) throws Exception {
        long timestamp = Instant.now().getEpochSecond();
        String authorInfo = authorName + " <" + authorEmail + "> " + timestamp + " +0000";

        StringBuilder commitContent = new StringBuilder();
        commitContent.append("tree ").append(treeSha).append("\n");
        if (!parentSha.isEmpty()) {
            commitContent.append("parent ").append(parentSha).append("\n");
        }
        commitContent.append("author ").append(authorInfo).append("\n");
        commitContent.append("committer ").append(authorInfo).append("\n\n");
        commitContent.append(message).append("\n");

        byte[] commitBytes = commitContent.toString().getBytes(StandardCharsets.UTF_8);

        // Store the commit object correctly
        return GitUtils.storeObject(commitBytes, "commit");
    }
}
