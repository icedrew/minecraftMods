package com.ololee.quake.utils;

import com.ololee.quake.exceptions.FileCanNotReadException;
import com.ololee.quake.exceptions.FileCanNotWriteException;
import net.minecraft.util.math.vector.Vector3d;

import java.io.*;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class HomeHelper {
    public static final String homeConfigFilePath = "home.conf";

    public static String readFile() throws FileNotFoundException {
        File file = new File(homeConfigFilePath);
        if (!file.exists()) {
            throw new FileNotFoundException(file.getName().concat("is not exist."));
        }
        if (!file.canRead())
            throw new FileCanNotReadException(file.getName().concat("can not be read."));
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String content = bufferedReader.lines().collect(Collectors.joining());
        try {
            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static void writeFile(String content) throws Exception {
        File file = new File(homeConfigFilePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!file.canWrite())
            throw new FileCanNotWriteException(file.getName().concat("can not be written."));
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(content);
        bufferedWriter.flush();
        bufferedWriter.close();
        fileWriter.close();
    }

    public static String seralizePlayersHomeData(HashMap<String, Vector3d> playerHomeMap) {
        Set<String> names = playerHomeMap.keySet();
        StringBuilder builder = new StringBuilder();
        for (String name : names) {
            Vector3d vector3d = playerHomeMap.get(name);
            builder.append(name).append(" : (")
                    .append(vector3d.x).append(",")
                    .append(vector3d.y).append(",")
                    .append(vector3d.z).append(")")
                    .append("\n");
        }
        return builder.toString();
    }

    public static HashMap<String, Vector3d> constructPlayerHomeData(String content) {
        HashMap<String, Vector3d> result = new HashMap<>();
        if (content != null && !content.equals("")) {
            String[] lines = content.split("\\n");
            if (lines != null) {
                for (String line : lines) {
                    if (line != null && !line.equals("")) {
                        String[] item = line.split(":");
                        String[] positions = item[1].trim().replace("(", "").replace(")", "").split(",");
                        if (positions != null && positions.length == 3) {
                            double x = Double.parseDouble(positions[0]);
                            double y = Double.parseDouble(positions[1]);
                            double z = Double.parseDouble(positions[2]);
                            result.put(item[0].trim(), new Vector3d(x, y, z));
                        }
                    }
                }
            }
        }
        return result;
    }

}
