import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class FileHandler {

    public static BigInteger getId(String path) {
        BigInteger id = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte adressToHash[] = path.getBytes();

            id = new BigInteger(1, digest.digest(adressToHash)).mod(BigInteger.valueOf(2).pow(ChordNodeInfo.getM()));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public static byte[] readFile(String path) {
        Path filePath = Paths.get(path);
        byte[] file = null;
        try {
            file = Files.readAllBytes(filePath);
        } catch (IOException ex) {
            //System.out.println(MyUtils.ANSI_YELLOW + "Searched: " + path + ". Not found." + MyUtils.ANSI_RESET);
            //ex.printStackTrace();
        }

        return file;
    }

    public static boolean saveFile(String fileName, byte[] file) {
        try {
            Files.write(new File("./storage/" + fileName).toPath(), file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean restoreFile(String fileName, byte[] file) {
        try {
            Files.write(new File("./restored/" + fileName).toPath(), file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static HashMap<String, byte[]> getStoredFiles(String operation, BigInteger id) {

        HashMap<String, byte[]> files = new HashMap<String, byte[]>();
        File folder = new File("./storage");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {

                String filename = listOfFiles[i].getName();
                BigInteger fileId = new BigInteger(filename.split("-", 0)[0]);

                switch (operation){
                    case ">":
                        if(fileId.compareTo(id) > 0){
                            byte[] file = FileHandler.readFile(listOfFiles[i].getPath());
                            files.put(filename, file);
                        }
                        break;
                    case ">=":
                        if(fileId.compareTo(id) >= 0){
                            byte[] file = FileHandler.readFile(listOfFiles[i].getPath());
                            files.put(filename, file);
                        }
                        break;
                    case "<":
                        if(fileId.compareTo(id) < 0){
                            byte[] file = FileHandler.readFile(listOfFiles[i].getPath());
                            files.put(filename, file);
                        }
                        break;
                    case "<=":
                        if(fileId.compareTo(id) <= 0){
                            byte[] file = FileHandler.readFile(listOfFiles[i].getPath());
                            files.put(filename, file);
                        }
                        break;
                    case "==":
                        if(fileId.compareTo(id) == 0){
                            byte[] file = FileHandler.readFile(listOfFiles[i].getPath());
                            files.put(filename, file);
                        }
                        break;
                }
            }
        }

        return files;
    }


    public static void deleteFilesFromStorage(HashMap<String, byte[]> files) {
        for (Map.Entry<String, byte[]> file : files.entrySet()) 
            if(!FileHandler.deleteFileFromStorage(file.getKey())) 
                System.out.println("ERROR: Failed to delete file " + file.getKey()); 
    }

    public static boolean deleteFileFromStorage(String filename) {
        File file = new File("./storage/" + filename);
        return file.delete();
    }

    private static boolean fileExists(String fileId, String folderName) {

        File folder = new File(folderName);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String filename = listOfFiles[i].getName();
                String id = filename.split("-", 0)[0];
                if(fileId.equals(id))
                    return true;
            }
        }
        return false;
    }

    public static boolean fileExistsInStorage(String fileId) {
        return FileHandler.fileExists(fileId, "./storage");
    }

    public static boolean fileExistsInRestored(String fileId) {
        return FileHandler.fileExists(fileId, "./restored");
    }

    public static String getFileName(String path) {
        String fileName = "";

        String[] aux = path.split("/", 0);
        fileName = aux[aux.length - 1];

        return fileName;
    }
}