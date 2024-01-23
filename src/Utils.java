import java.io.*;

public class Utils {
    public static byte[] serializeObject(Object o) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        byte[] result = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.flush();
            result = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Object deserializeObject(byte[] b) {
        ByteArrayInputStream bis = new ByteArrayInputStream(b);
        ObjectInput in = null;
        Object result = null;
        try {
            in = new ObjectInputStream(bis);
            result = in.readObject();
        } catch (IOException ignored) {}
        catch (ClassNotFoundException e) { e.printStackTrace(); }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ignored) {}
        }
        return result;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    // Цвета текста
    public static final String RESET = "\033[0m";  // Сброс цвета
    public static final String BLACK = "\033[0;30m";   // Черный цвет текста
    public static final String RED = "\033[0;31m";     // Красный цвет текста
    public static final String GREEN = "\033[0;32m";   // Зеленый цвет текста
    public static final String YELLOW = "\033[0;33m";  // Желтый цвет текста
    public static final String BLUE = "\033[0;34m";    // Синий цвет текста
    public static final String PURPLE = "\033[0;35m";  // Пурпурный цвет текста
    public static final String CYAN = "\033[0;36m";    // Голубой цвет текста
    public static final String WHITE = "\033[0;37m";   // Белый цвет текста

    // Стиль текста
    public static final String BOLD = "\033[1m";      // Жирный шрифт
    public static final String ITALIC = "\033[3m";    // Курсивный шрифт
    public static final String UNDERLINE = "\033[4m"; // Подчеркнутый текст
    public static final String BLINK = "\033[5m";     // Мигающий текст
    public static final String REVERSE = "\033[7m";   // Реверсивный текст
}
