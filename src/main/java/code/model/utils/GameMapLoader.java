package code.model.utils;

import code.model.GameMap;
import code.model.Cell;
import code.model.Coord;
import code.model.celltype.Army;
import code.model.celltype.Empty;
import code.model.celltype.Wall;
import code.libs.json.JSONArray;
import code.libs.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class GameMapLoader {
    
    /**
     * Load une map depuis un json dans appdata/maps
     */
    public static GameMap loadMap(String fileName) throws IOException {
        File file = new File("appdata/maps", fileName);
        
        if (!file.exists()) {
            throw new IOException("Le fichier " + fileName + " n'existe pas");
        }
        
        String content = new String(Files.readAllBytes(file.toPath()));
        JSONObject json = new JSONObject(content);
        
        int width = json.getInt("width");
        int height = json.getInt("height");
        JSONArray teamIdsArray = json.getJSONArray("teamIds");
        List<Integer> teamIds = new ArrayList<>();
        for (int i = 0; i < teamIdsArray.length(); i++) {
            teamIds.add(teamIdsArray.getInt(i));
        }
        
        Cell[][] cellMap = new Cell[height][width];
        JSONArray cells = json.getJSONArray("cells");
        
        for (int y = 0; y < height; y++) {
            JSONArray row = cells.getJSONArray(y);
            
            for (int x = 0; x < width; x++) {
                JSONObject cellObj = row.getJSONObject(x);
                String type = cellObj.getString("type");
                
                switch (type) {
                    case "empty":
                        cellMap[y][x] = new Empty();
                        break;
                    case "wall":
                        cellMap[y][x] = new Wall();
                        break;
                    case "army":
                        int teamId = cellObj.getInt("teamId");
                        cellMap[y][x] = new Army(teamId, new Coord(x, y));
                        break;
                    default:
                        cellMap[y][x] = new Empty();
                        break;
                }
            }
        }
        
        GameMap returnedGameMap = new GameMap(width, height);
        returnedGameMap.setCellMap(cellMap);
        returnedGameMap.setTeamIds(teamIds);
        
        return returnedGameMap;
    }
}