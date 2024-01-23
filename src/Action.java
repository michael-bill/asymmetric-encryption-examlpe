import java.io.Serializable;

public class Action implements Serializable {
    public Action(int action, byte[] data) {
        this.code = action;
        this.data = data;
    }

    public int code;
    public byte[] data;
}
