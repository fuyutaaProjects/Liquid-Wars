package code.model.utils;

import javax.swing.*;
import java.awt.*;

/** fais des side panels, evite des gros blocs de createVerticalStrut dans le code. */
public class SidePanelFactory {
    
    /** le side panel de MapGenerationTestingMenu */
    public static JPanel createMapGenerationTestingMenuSidePanel(
            JSlider sliderMapWidth,
            JSlider sliderMapHeight,
            JSlider sliderNOfTeams,
            JSlider sliderWallFrequency,
            JSlider sliderInitialPropagationFactor,
            JSlider sliderDecayFactor,
            JButton btnGenerate,
            JButton btnPauseResume,
            JButton btnBackToMainMenu,
            JButton btnExport,
            JPanel mapListPanel
    ) {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(350, 0));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Paramètres pour MapGenerator generate()");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidePanel.add(title);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(sliderMapWidth);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(sliderMapHeight);
        sidePanel.add(Box.createVerticalStrut(30));
        sidePanel.add(sliderNOfTeams);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(sliderWallFrequency);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(sliderInitialPropagationFactor);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(sliderDecayFactor);
        sidePanel.add(Box.createVerticalStrut(30));
        sidePanel.add(btnGenerate);
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(btnPauseResume);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(btnBackToMainMenu);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(btnExport);
        sidePanel.add(Box.createVerticalStrut(30));
        sidePanel.add(mapListPanel);
        
        return sidePanel;
    }



    /** le side panel de GamePlayMenu */
    public static JPanel createGamePlayMenuSidePanel(
            JButton btnBackToLevelSelect,
            JButton btnBackToMainMenu
    ) {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(250, 0));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // pour centrer les éléments. techniquement on aurait pû faire une box qui est ensuite centered selon un layout et règle mais c'est bien plus long
        sidePanel.add(Box.createVerticalStrut(300)); 
        
        sidePanel.add(btnBackToLevelSelect);
        sidePanel.add(Box.createVerticalStrut(20));
        sidePanel.add(btnBackToMainMenu);

        return sidePanel;
    }
}