import java.nio.file.Paths;

public class Main {
  public static void main(String[] args) throws Exception {
      switch (args[0]) {
          case "init" -> GitRepository.initializeGit();
          case "cat-file" -> GitBlob.readBlob(args[2]);
          case "hash-object" -> System.out.println(GitBlob.createBlob(args[2]));
          case "ls-tree" -> GitTree.listTree(args[2]);
          case "write-tree" -> System.out.println(GitTree.writeTree(Paths.get(".")));
          default -> System.out.println("Unknown command: " + args[0]);
      }
  }
}