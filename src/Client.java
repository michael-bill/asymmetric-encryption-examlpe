import javax.crypto.Cipher;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;

public class Client {

    private static String name;
    private static final int port = 1703;
    private static final int sendPublicKeyAction = 1;
    private static final int getOpponentPublicKeyAction = 2;
    private static final int sendMessageToOpponentAction = 3;

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("localhost", port)) {
            System.out.println("Установлено соединение с сервером.");
            KeyPair keyPair = getNewKeys();
            System.out.println("Мы сгенерировали 2 ключа.");
            System.out.println();
            System.out.println("Открытый: " + Utils.BLUE + Utils.bytesToHex(keyPair.getPublic().getEncoded()) + Utils.RESET);
            System.out.println("Закрытый: " + Utils.RED + Utils.bytesToHex(keyPair.getPrivate().getEncoded()) + Utils.RESET);
            System.out.println();
            sendPublicKeyAction(keyPair.getPublic(), socket);
            System.out.println("Наш открытый ключ отправлен на сервер.");

            System.out.println("Ожидаю открытый ключ от оппонента...");
            PublicKey opponentPublicKey = getOpponentPublicKeyAction(socket);
            System.out.print("Открытый ключ от оппонента получен: ");
            System.out.println(Utils.BLUE + Utils.bytesToHex(opponentPublicKey.getEncoded()) + Utils.RESET);
            System.out.println();

            boolean printed = false;
            InputStream systemIn = System.in;
            while (true) {
                if (!printed) {
                    System.out.print("Отправить сообщение оппоненту -> ");
                    printed = true;
                }
                if (systemIn.available() > 0) {
                    String messageString = new String(systemIn.readNBytes(systemIn.available()), Charset.defaultCharset());
                    System.out.println("\nШифруем сообщение открытым ключом оппонента.\nОткрытый ключ оппонента:\n" + Utils.BLUE + Utils.bytesToHex(opponentPublicKey.getEncoded()) + Utils.RESET);
                    byte[] message = getEncryptedMessage(opponentPublicKey, messageString.getBytes(StandardCharsets.UTF_8));
                    System.out.println("Зашифрованное сообщение для отправки оппоненту:\n" + Utils.PURPLE + Utils.bytesToHex(message) + Utils.RESET + "\n");
                    sendMessageToOpponentAction(socket, message, opponentPublicKey);
                    printed = false;
                } else if (socket.getInputStream().available() > 0) {
                    byte[] message = socket.getInputStream().readNBytes(socket.getInputStream().available());
                    Action action = (Action) Utils.deserializeObject(message);
                    if (action.code == sendMessageToOpponentAction) {
                        System.out.println("\n\nПолучены зашифрованные данные от оппонента:\n" + Utils.PURPLE + Utils.bytesToHex(action.data) + Utils.RESET);
                        byte[] decryptedMessage = getDecryptedMessage(keyPair.getPrivate(), action.data);
                        String messageString = new String(decryptedMessage, StandardCharsets.UTF_8);
                        System.out.println("Наш закрытый ключ для расшифровки:\n" + Utils.RED + Utils.bytesToHex(keyPair.getPrivate().getEncoded()) + Utils.RESET);
                        System.out.println("Расшифрованое сообщение при помощи нашего закрытого ключа: " + Utils.GREEN + Utils.ITALIC + messageString + Utils.RESET);
                        printed = false;
                    }
                }
                Thread.sleep(10);
            }
        } catch (Exception e) {
            System.out.println("Не удалось соединиться с сервером, попробуйте позже.");
        }
    }

    private static void sendMessageToOpponentAction(Socket socket, byte[] message, PublicKey opponentPublicKey) throws Exception {
        Action action = new Action(sendMessageToOpponentAction, message);
        socket.getOutputStream().write(Utils.serializeObject(action));
    }

    private static PublicKey getOpponentPublicKeyAction(Socket socket) throws Exception {
        PublicKey result = null;
        while (result == null) {
            Action action = new Action(getOpponentPublicKeyAction, null);
            socket.getOutputStream().write(Utils.serializeObject(action));
            if (socket.getInputStream().available() > 0) {
                Action actionResponse = (Action) Utils.deserializeObject(socket.getInputStream().readNBytes(socket.getInputStream().available()));
                if (actionResponse.code == getOpponentPublicKeyAction)
                    result = (PublicKey) Utils.deserializeObject(actionResponse.data);
            }
            Thread.sleep(100);
        }
        return result;
    }

    private static void sendPublicKeyAction(PublicKey publicKey, Socket socket) throws IOException {
        Action action = new Action(sendPublicKeyAction, Utils.serializeObject(publicKey));
        socket.getOutputStream().write(Utils.serializeObject(action));
    }

    private static byte[] getDecryptedMessage(PrivateKey privateKey, byte[] message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(message);
    }

    private static byte[] getEncryptedMessage(PublicKey publicKey, byte[] message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(message);
    }

    private static KeyPair getNewKeys() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }
}
