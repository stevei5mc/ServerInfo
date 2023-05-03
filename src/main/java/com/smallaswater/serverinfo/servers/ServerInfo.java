package com.smallaswater.serverinfo.servers;

import cn.nukkit.utils.BinaryStream;
import com.smallaswater.serverinfo.ServerInfoMainClass;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author SmallasWater
 * Create on 2021/7/14 8:16
 * Package com.smallaswater.serverinfo.servers
 */
@Data
public class ServerInfo {

    private String name;

    private String group;

    private String ip;

    private int port;

    private int player;

    private int maxPlayer;


    public ServerInfo(String name, String group, String ip, int port) {
        this.name = name;
        this.group = group;
        this.ip = ip;
        this.port = port;
    }

    @Deprecated
    public String getCallback() {
        return name;
    }

    @Deprecated
    public void setCallback(String name) {
        this.name = name;
    }

    public void update(byte[] data) {
        if (data.length == 0) {
            player = -1;
            maxPlayer = -1;
            return;
        }
        BinaryStream binaryStream = new BinaryStream(data);

        //TODO 对读取到的数据进行检查
        getSubData(binaryStream); //splitnum
        binaryStream.getByte(); //128
        binaryStream.getByte(); //0x00

        LinkedHashMap<String, String> kvData = new LinkedHashMap<>();
        for (int i = 0; i < 12; i++) {
            kvData.put(getSubData(binaryStream), getSubData(binaryStream));
        }

        binaryStream.getByte(); //0x00
        binaryStream.getByte(); //0x01
        getSubData(binaryStream);//player_
        binaryStream.getByte(); //0x00

        ArrayList<String> players = new ArrayList<>();
        while (true) {
            String playerName = getSubData(binaryStream);
            if ("".equals(playerName)) {
                break;
            }
            players.add(playerName);
        }
    }

    @NotNull
    private static String getSubData(BinaryStream binaryStream) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte now;
        while (true) {
            now = (byte) binaryStream.getByte();
            if (now == 0x00) { //0x00为每一段数据的结束字符
                break;
            }
            stream.write(now);
        }
        if (stream.size() == 0) {
            return "";
        }
        return new String(stream.toByteArray(), StandardCharsets.UTF_8);
    }

    public void update(String[] data) {
        if (data.length > 0) {
            player = Integer.parseInt(data[4]);
            maxPlayer = Integer.parseInt(data[5]);
        } else {
            player = -1;
            maxPlayer = -1;
        }
    }

    /**
     * @return 服务器是否在线
     */
    public boolean onLine() {
        return player != -1;
    }

    public String toButtonText() {
        if (onLine()) {
            return ServerInfoMainClass.getInstance().getLanguage().getString("button-text-online", "")
                    .replace("{server}", name).replace("{player}", player + "").replace("{maxplayer}", maxPlayer + "");
//            return callback+"\n&e"+player+" &7/&6 "+maxPlayer+"  &a在线";
        } else {
            return ServerInfoMainClass.getInstance().getLanguage().getString("button-text-offline", "")
                    .replace("{server}", name);
        }
    }


}
