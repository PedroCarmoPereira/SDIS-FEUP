import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.*;
import java.util.ArrayList;
import java.util.List;

public class FileHash {

    private String path;
    private String fileId;
    private BasicFileAttributes attributes;

    public FileHash(String path, String fileId) {
        this.path = path;
        this.fileId = fileId;
        if (this.path != null)
            try {
                Path p = Paths.get(path);
                this.attributes = Files.readAttributes(p, BasicFileAttributes.class);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
    }

    public FileHash(String path) {
        this.path = path;

        try {
            Path p = Paths.get(path);
            this.attributes = Files.readAttributes(p, BasicFileAttributes.class);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            outputStream.write(this.attributes.creationTime().toString().getBytes());
            outputStream.write(this.attributes.lastModifiedTime().toString().getBytes());

            outputStream.write(path.getBytes());

            byte fileToHash[] = outputStream.toByteArray();
            this.fileId = toHexString(digest.digest(fileToHash));

            /*System.out.println("Created: " + this.fileId);
             this.fileId = this.fileId.replace(' ', 'A');
            this.fileId = this.fileId.replace('\r', 'B');
            this.fileId = this.fileId.replace('\n', 'C'); */
        } catch (NoSuchAlgorithmException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String toHexString(byte[] hash) 
    { 
        // Convert byte array into signum representation  
        BigInteger number = new BigInteger(1, hash);  
  
        // Convert message digest into hex value  
        StringBuilder hexString = new StringBuilder(number.toString(16));  
  
        // Pad with leading zeros 
        while (hexString.length() < 32)  
        {  
            hexString.insert(0, '0');  
        }  
  
        return hexString.toString();  
    } 

    public List<File> getLocalChunks() {
        List<File> chunks = new ArrayList<>();

        if (Peer.getChunkStorage().exists()) {
            for (File fileEntry : Peer.getChunkStorage().listFiles()) {
                if (!fileEntry.isDirectory() && check(fileEntry.getName())) {
                    chunks.add(fileEntry);
                }
            }
        } else {
            System.out.println("Folder p2pstorage doesn't exist");
            return null;
        }

        return chunks;
    }

    private Boolean check(String chunk) {
        if (chunk.charAt(0) == 'C' && chunk.charAt(1) == 'N') {
            int i = 3;
            while (i < chunk.length() - 2) {
                if (chunk.charAt(i) == 'F' && chunk.charAt(i + 1) == 'I' && chunk.charAt(i + 2) == 'D')
                    break;
                i++;
            }
            if (i + 3 >= chunk.length()) {
                System.out.println(MyUtils.ANSI_RED + "Error deleting chunk" + MyUtils.ANSI_RESET);
                return false;
            }
            if (chunk.substring(i + 3, chunk.length()).equals(this.fileId)) {
                System.out.println(MyUtils.ANSI_YELLOW + "\nDeleting chunk: " + chunk + MyUtils.ANSI_RESET);
                return true;
            }
        }
        return false;
    }

    public String getPath() {
        return this.path;
    }

    public String getFileId() {
        return this.fileId;
    }

    public BasicFileAttributes getFileAttributes() {
        return this.attributes;
    }
}