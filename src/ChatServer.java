import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ChatServer {
    int index =0;

    private static Map<String, PrintWriter> clientMap = new HashMap<>();
    private static final int PORT = 8080;
    private static List<PrintWriter> clientWriters = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            String buff;
            System.out.println("Сервер запущен на порту " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новое подключение: " + clientSocket);

                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                clientWriters.add(writer);
                buff = "username" + clientWriters.size();
                System.out.println("Пользователь " + buff + " присоединился к чату");
                clientMap.put(buff,writer);
                Thread clientHandler = new Thread(new ClientHandler(clientSocket, writer));
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter writer;

        public ClientHandler(Socket socket, PrintWriter writer) {
            this.clientSocket = socket;
            this.writer = writer;
        }

        @Override
        public void run() {
            try (Scanner scanner = new Scanner(clientSocket.getInputStream())) {
                while (scanner.hasNextLine()) {
                    String message = scanner.nextLine();
                    broadcast(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void broadcast(String message) {
            // Проверка наличия префикса для личных сообщений
            if (message.startsWith("/pm")) {
                String[] parts = message.split(" ", 3);
                String recipient = parts[1];
                String pmMessage = parts[2];

                // Поиск PrintWriter для получателя и отправка личного сообщения
                if (clientMap.containsKey(recipient)) {
                    clientMap.get(recipient).println("Личное для " + recipient + ": " + pmMessage);
                } else {
                    writer.println("Пользователь " + recipient + " не найден.");
                }
            } else {
                // Обычная рассылка сообщений
                for (PrintWriter clientWriter : clientWriters) {
                    clientWriter.println(message);
                }
            }
        }
    }
}