package code.model.utils;

import code.model.GameMap;
import code.model.Cell;
import code.model.celltype.Army;
import code.model.celltype.Empty;
import code.model.celltype.Wall;
import code.libs.json.JSONArray;
import code.libs.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Tool pour export un objet Map en JSON
 */
public class GameMapExporter {
    
    /**
     * Exporte la map vers le dossier appdata/maps
     */
    public static void exportGameMap(GameMap gameMap, String fileName) throws IOException {
        File directory = new File("appdata/maps");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        File file = new File(directory, fileName + ".json");
        
        Cell[][] cellMap = gameMap.getCellMap();
        int height = cellMap.length;
        int width = cellMap[0].length;
        
        JSONObject json_file = new JSONObject();
        json_file.put("width", width);
        json_file.put("height", height);
        List<Integer> teamIds = gameMap.getTeamIds();
        JSONArray teamIdsArray = new JSONArray();
        for (Integer id : teamIds) {
            teamIdsArray.put(id);
        }
        json_file.put("teamIds", teamIdsArray);
        
        JSONArray cells = new JSONArray();
        
        for (int y = 0; y < height; y++) {
            JSONArray row = new JSONArray();
            
            for (int x = 0; x < width; x++) {
                Cell cell = cellMap[y][x];
                JSONObject cellObj = new JSONObject();
                
                if (cell instanceof Empty) {
                    cellObj.put("type", "empty");
                } else if (cell instanceof Wall) {
                    cellObj.put("type", "wall");
                } else if (cell instanceof Army) {
                    Army army = (Army) cell;
                    cellObj.put("type", "army");
                    cellObj.put("teamId", army.getTeamId());
                } else {
                    cellObj.put("type", "UNDEF");
                }
                
                row.put(cellObj);
            }
            
            cells.put(row);
        }
        
        json_file.put("cells", cells);
        
        // Écrire dans le fichier
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json_file.toString(2)); // Le indentFactor : 2, ajoute deux espaces a chaque niveau de profondeur de crochets/parenthèses. ça "tabule".
        }
    }
}