import java.io.*;
import java.net.*;
import java.security.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

class Backup {

    int MAX_CHUNK_SIZE = 64000;
    private byte[] file;
    private BasicFileAttributes attributes;
    private byte[][] chunks;
    private String fileId;
    private String filePath = "./pdfTest/1.pdf";

    private void readFile() {
        Path file = Paths.get(this.filePath);
        try {
            this.file = Files.readAllBytes(file);
            this.attributes = Files.readAttributes(file, BasicFileAttributes.class);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void createChunks() {
        int chunksN = (this.file.length / MAX_CHUNK_SIZE) + 1;
        this.chunks = new byte[chunksN][MAX_CHUNK_SIZE];

        for (int i = 0; i < chunksN; i++) {
            for (int j = 0; j < MAX_CHUNK_SIZE; j++) {
                if (j + ((i + 1) * MAX_CHUNK_SIZE) >= this.file.length)
                    break;
                this.chunks[i][j] = this.file[j + ((i + 1) * MAX_CHUNK_SIZE)];
            }
        }

    }

    private void createHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            if (this.attributes.isOther())
                outputStream.write("true".getBytes());
            if (this.attributes.isRegularFile())
                outputStream.write("true".getBytes());
            if (this.attributes.isSymbolicLink())
                outputStream.write("true".getBytes());
            outputStream.write(this.attributes.creationTime().toString().getBytes());
            outputStream.write(this.attributes.lastModifiedTime().toString().getBytes());

            outputStream.write(this.file);

            byte fileToHash[] = outputStream.toByteArray();
            this.fileId = digest.digest(fileToHash).toString();
        } catch (NoSuchAlgorithmException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public Backup() {
        this.readFile();
        this.createChunks();
        this.createHash();
    }

}