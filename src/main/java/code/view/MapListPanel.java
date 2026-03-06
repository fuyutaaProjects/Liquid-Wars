package code.view;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;
import code.model.GameMap;
import code.model.utils.ButtonFactory;
import code.model.utils.GameMapLoader;

/**
 * Panel qui affiche la liste des maps sauvegardées et permet de les charger
 */
public class MapListPanel extends JPanel {
    private JPanel listContainer;
    private Consumer<GameMap> onMapLoaded;

    public MapListPanel(Consumer<GameMap> onMapLoaded) {
        this.onMapLoaded = onMapLoaded;
        
        setLayout(new BorderLayout());
        
        JLabel listTitle = new JLabel("Maps sauvegardées");
        listTitle.setHorizontalAlignment(SwingConstants.CENTER);
        listTitle.setFont(new Font("Arial", Font.BOLD, 13));
        listTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(listTitle, BorderLayout.NORTH);
        
        // Container pour la liste des maps
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setPreferredSize(new Dimension(330, 200));
        scrollPane.setMaximumSize(new Dimension(330, 200));
        
        add(scrollPane, BorderLayout.CENTER);
        
        refreshMapList();
    }

    /**
     * Recharge la liste des maps disponibles
     */
    public void refreshMapList() {
        listContainer.removeAll();
        
        File mapsDir = new File("appdata/maps");
        if (mapsDir.exists() && mapsDir.isDirectory()) {
            File[] files = mapsDir.listFiles((dir, name) -> name.endsWith(".json"));
            
            if (files != null && files.length > 0) {
                for (File file : files) {
                    JButton mapButton = ButtonFactory.createStylizedRegularButton(file.getName());
                    mapButton.setMaximumSize(new Dimension(310, 30));
                    mapButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                    
                    mapButton.addActionListener(e -> loadMap(file.getName()));
                    
                    listContainer.add(mapButton);
                    listContainer.add(Box.createVerticalStrut(5));
                }
            } else {
                addEmptyLabel();
            }
        } else {
            addEmptyLabel();
        }
        
        listContainer.revalidate();
        listContainer.repaint();
    }

    /**
     * Ajoute un label indiquant qu'il n'y a pas de maps sauvegardées
     */
    private void addEmptyLabel() {
        JLabel emptyLabel = new JLabel("Pas de maps sauvegardées");
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        listContainer.add(emptyLabel);
    }

    /**
     * Charge une map depuis un fichier
     */
    private void loadMap(String fileName) {
        try {
            GameMap loadedMap = GameMapLoader.loadMap(fileName);
            
            if (onMapLoaded != null) {
                onMapLoaded.accept(loadedMap);
            }
            
            System.out.println("Map chargée : " + fileName);
        } catch (Exception ex) {
            System.out.println("Erreur lors du chargement : " + ex.getMessage());
            JOptionPane.showMessageDialog(
                this,
                "Erreur lors du chargement : " + ex.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}