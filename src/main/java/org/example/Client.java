package org.example;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

public class Client {
    private Socket requestSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String message;
    private final Random random = new Random();
    private String CLIENT_FILE_NAME = "clientFiles/";
    private String chooseFileName;

    Client() {
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    void run() {
        try {
            // 1. socket kapcsolat létrehozása
            requestSocket = new Socket("localhost", 8081);
            // 2. Input and Output streamek
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            // 3: Kommunikáció
            do {
                try (Scanner scanner = new Scanner(System.in)) {
                    sendMessage("Hello szerver");
                    sendMessage("Listing");

                    message = (String) in.readObject();
                    System.out.println("server>" + message);

                    System.out.println("Kérlek add meg, hogy letölteni(d) vagy feltölteni(u) szeretnél: ");
                    String chooseChar = scanner.nextLine();

                    System.out.println("Kérlek add meg a fájl nevét (kiterjesztéssel): ");
                    chooseFileName = scanner.nextLine();

                    sendMessage(chooseChar + ", " + chooseFileName);

                    message = (String) in.readObject();
                    System.out.println("server>" + message);

                    if (message.startsWith("Download file:")) {
                        String[] file = message.split("Download file:\\s");
                        writeNewFile(chooseFileName, file[1]);
                        System.out.println("client> Successful");
                        sendMessage("Successful");
                    }

                    if(message.equals("Uploading")) {
                        String file = readFile(chooseFileName);
                        sendMessage("Uploading " + file);
                    }

                    message = (String) in.readObject();

                    if(message.equals("Successful")) {
                        System.out.println("server>" + message);
                    }

                    sendMessage("bye");
                    message = (String) in.readObject();
                    System.out.println("server>" + message);
                } catch (Exception e) {
                    System.err.println("data received in unknown format");
                }
            } while (!message.equals("bye"));
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            System.out.println(ioException.getMessage());
        } finally {
            // 4: Kapcsolat zárása
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                System.out.println(ioException.getMessage());
            }
        }
    }

    void sendMessage(String msg) {
        try {
            out.writeObject(msg);
            out.flush();
            System.out.println("client>" + msg);
        } catch (IOException ioException) {
            System.out.println(ioException.getMessage());
        }
    }

    private String readFile(String fileName) throws IOException {
        Path path = Paths.get(CLIENT_FILE_NAME + fileName);
        return Files.readAllLines(path).getFirst();
    }

    public void writeNewFile(String fileName, String content) throws IOException {
        String randomNum = String.valueOf(random.nextInt(1000));
        FileWriter fileWriter = new FileWriter(CLIENT_FILE_NAME + randomNum + fileName);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(content);
        printWriter.close();
    }

}