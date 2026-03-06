package code.model.utils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SliderFactory {    
    private static final Color BG_COLOR = new Color(232, 233, 235);
    private static final Color TEXT_COLOR = new Color(0, 0, 0);
    private static final Color THUMB_COLOR = new Color(0,0,0);
    
    public static JSlider createStyledSlider(int min, int max, int val, String title, double divisor) {
        JSlider slider = new JSlider(min, max, val);
        
        slider.setBackground(BG_COLOR);
        slider.setForeground(THUMB_COLOR);
        
        String labelText = title + " : " + (val / divisor);
        TitledBorder border = BorderFactory.createTitledBorder(labelText);
        border.setTitleColor(TEXT_COLOR);
        slider.setBorder(border);
        
        slider.addChangeListener(e -> {
            String updatedLabel = title + " : " + (slider.getValue() / divisor);
            TitledBorder updatedBorder = BorderFactory.createTitledBorder(updatedLabel);
            updatedBorder.setTitleColor(TEXT_COLOR);
            slider.setBorder(updatedBorder);
        });
        
        return slider;
    }
}