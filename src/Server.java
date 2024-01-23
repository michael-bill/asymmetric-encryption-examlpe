import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;

public class Server {

    private static final Socket[] sockets = new Socket[2];
    private static final PublicKey[] publicKeys = new PublicKey[2];
    private static final int sendPublicKeyAction = 1;
    private static final int getOpponentPublicKeyAction = 2;
    private static final int sendMessageToOpponentAction = 3;


    public static void main(String[] args) {
        int port = 1703;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            for (int i = 0; i < 2; i++) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\nПодключился клиент " + (i + 1) + ".");
                sockets[i] = clientSocket;
                ClientHandler clientHandler = new ClientHandler(clientSocket, i);
                new Thread(clientHandler).start();
            }
            System.out.println("\nОба клиента подключились, сервер готов пересылать сообщения.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class ClientHandler implements Runnable {

        private final Socket clientSocket;
        private PublicKey publicKey;
        int index;

        public ClientHandler(Socket clientSocket, int index) {
            this.clientSocket = clientSocket;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (clientSocket.getInputStream().available() > 0) {
                        byte[] message = clientSocket.getInputStream().readNBytes(clientSocket.getInputStream().available());
                        Action action = (Action) Utils.deserializeObject(message);
                        if (action.code == sendPublicKeyAction) {
                            publicKey = (PublicKey) Utils.deserializeObject(action.data);
                            publicKeys[index] = publicKey;
                            System.out.println("\nПолучен и сохранен на сервер открытый ключ от клиента " + (index + 1) +
                                    "\nОткрытый ключ: " + Utils.BLUE + Utils.bytesToHex(publicKey.getEncoded()) + Utils.RESET);
                        } else if (action.code == getOpponentPublicKeyAction && publicKeys[Math.abs(index - 1)] != null) {
                            Action actionResponse = new Action(getOpponentPublicKeyAction, Utils.serializeObject(publicKeys[Math.abs(index - 1)]));
                            clientSocket.getOutputStream().write(Utils.serializeObject(actionResponse));
                        } else if (action.code == sendMessageToOpponentAction) {
                            sockets[Math.abs(index - 1)].getOutputStream().write(message);
                            System.out.println("\nПеренаправлено зашифрованное сообщение от клиента " + (index + 1) + " к клиенту " +
                                    (Math.abs(index - 1) + 1) + ".\nЗашифрованное сообщение: " + Utils.PURPLE + Utils.bytesToHex(action.data) + Utils.RESET);
                        }
                        clientSocket.getInputStream().read(new byte[1], 0, 0);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }
}
