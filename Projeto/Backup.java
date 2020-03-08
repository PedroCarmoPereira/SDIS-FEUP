import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.nio.file.Files;
import java.util.Arrays;

class Backup {

    int MAX_CHUNK_SIZE = 64000;
    private byte[] pdf;
    private byte[][] chunks = new byte[1][1]; 

    private void readFile() {
        Path pdfPath = Paths.get("./pdfTest/1.pdf");
        try {
            this.pdf = Files.readAllBytes(pdfPath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void createChunks() {
        int chunksN = (this.pdf.length/MAX_CHUNK_SIZE) + 1;
/*         for(int i = 1; i <= chunksN; i++){
            int j = 0;
            while(j < MAX_CHUNK_SIZE){
                if(j*i > this.pdf.length)
                    break;
                this.chunks[i-1][j] = this.pdf[j*i];
            }
        }
 */
        this.chunks = new byte[chunksN][MAX_CHUNK_SIZE]; 

        for(int i = 0; i < chunksN; i++){
            for(int j = 0; j < MAX_CHUNK_SIZE; j++){
                if(j+((i+1)*MAX_CHUNK_SIZE) >= this.pdf.length)
                    break;
                this.chunks[i][j] = this.pdf[j+((i+1)*MAX_CHUNK_SIZE)];
            }
        }
                
    }

    public Backup() {
        this.readFile();
        this.createChunks();
    }

}