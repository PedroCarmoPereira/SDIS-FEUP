import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.nio.file.Files;
import java.util.Arrays;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import java.io.File;

class Backup {

    int MAX_CHUNK_SIZE = 64000;
    private byte[] pdf;
    private byte[][] chunks;
    private String fileId;
    private File pdfFile;
    private String filePath = "./pdfTest/1.pdf";

    private void readFile() {
        Path pdfPath = Paths.get(this.filePath);
        this.pdfFile = new File(this.filePath);
        try {
            this.pdf = Files.readAllBytes(pdfPath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void createChunks() {
        int chunksN = (this.pdf.length / MAX_CHUNK_SIZE) + 1;
        this.chunks = new byte[chunksN][MAX_CHUNK_SIZE];

        for (int i = 0; i < chunksN; i++) {
            for (int j = 0; j < MAX_CHUNK_SIZE; j++) {
                if (j + ((i + 1) * MAX_CHUNK_SIZE) >= this.pdf.length)
                    break;
                this.chunks[i][j] = this.pdf[j + ((i + 1) * MAX_CHUNK_SIZE)];
            }
        }

    }

    private void createHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            PDDocument pdfDocument = PDDocument.load(this.pdfFile);
            PDDocumentInformation info = pdfDocument.getDocumentInformation();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            if (String.valueOf(pdfDocument.getNumberOfPages()) != null)
                outputStream.write(String.valueOf(pdfDocument.getNumberOfPages()).getBytes());
            if (info != null) {
                if (info.getTitle() != null)
                    outputStream.write(info.getTitle().getBytes());
                if (info.getAuthor() != null)
                    outputStream.write(info.getAuthor().getBytes());
                if (info.getSubject() != null)
                    outputStream.write(info.getSubject().getBytes());
                if (info.getKeywords() != null)
                    outputStream.write(info.getKeywords().getBytes());
                if (info.getCreator() != null)
                    outputStream.write(info.getCreator().getBytes());
                if (info.getProducer() != null)
                    outputStream.write(info.getProducer().getBytes());
                if (info.getCreationDate() != null)
                    outputStream.write(info.getCreationDate().toString().getBytes());
                if (info.getModificationDate() != null)
                    outputStream.write(info.getModificationDate().toString().getBytes());
                if (info.getTrapped() != null)
                    outputStream.write(info.getTrapped().getBytes());
            }
            outputStream.write(this.pdf);

            byte pdfToHash[] = outputStream.toByteArray();

            this.fileId = digest.digest(pdfToHash).toString();
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