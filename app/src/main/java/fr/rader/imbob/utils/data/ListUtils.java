package fr.rader.imbob.utils.data;

import java.util.List;

public class ListUtils {

    public static byte[] toByteArray(List<Byte> data) {
        byte[] bytes = new byte[data.size()];
        
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = data.get(i);
        }

        return bytes;
    }
}
