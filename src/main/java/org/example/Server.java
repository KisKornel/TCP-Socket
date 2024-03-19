package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Server {
    private ServerSocket providerSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String message;
    private String uploadFileName;
    private String CLIENT_FILE_NAME = "files/";
    private final Random random = new Random();

    Server() {
    }

    public static void main(String[] args) {
        Server server = new Server();
        while (true) {
            server.run();
        }
    }

    void run() {
        try {
            // 1. szerver socket létrehozása
            providerSocket = new ServerSocket(8081);
            // 2. kapcsolódásra várakozás
            Socket connection = providerSocket.accept();
            // 3. Input és Output streamek megadása
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            // 4. socket kommunikáció
            do {
                try {
                    message = (String) in.readObject();
                    System.out.println("client>" + message);

                    if (message.equals("Listing")) {
                        Set<String> fileList = getListFileName();
                        sendMessage("File list: " + fileList);
                    }

                    if (message.startsWith("u")) {
                        String[] fileName = message.split("\\s");
                        uploadFileName = fileName[1];
                        sendMessage("Uploading");
                    }

                    if (message.startsWith("d")) {
                        String[] fileName = message.split("\\s");
                        String file = readFile(fileName[1]);
                        sendMessage("Download file: " + file);
                    }

                    if (message.startsWith("Uploading")) {
                        String[] file = message.split("Uploading\\s");
                        writeNewFile(uploadFileName, file[1]);
                        sendMessage("Successful");
                    }

                    if (message.equals("Successful")) {
                        sendMessage("Successful");
                    }

                    if (message.equals("bye")) {
                        sendMessage("bye");
                    }
                } catch (ClassNotFoundException classnot) {
                    System.err.println("Data received in unknown format");
                }
            } while (!message.equals("bye"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            // 4: kapcsolat lezárása
            try {
                in.close();
                out.close();
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void sendMessage(String msg) {
        try {
            out.writeObject(msg);
            out.flush();
            System.out.println("server>" + msg);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private Set<String> getListFileName() throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get("files"))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        }
    }

    private String readFile(String fileName) throws IOException {
        Path path = Paths.get(CLIENT_FILE_NAME + fileName);
        return Files.readAllLines(path).getFirst();
    }

    public void writeNewFile(String fileName, String content) throws IOException {
        String randomNum = String.valueOf(random.nextInt(1000));
        FileWriter fileWriter = new FileWriter(CLIENT_FILE_NAME + randomNum + fileName );
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(content);
        printWriter.close();
    }
}