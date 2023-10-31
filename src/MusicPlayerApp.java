import controller.MusicPlayerController;

import javax.swing.*;

public class MusicPlayerApp {
    public static void main(String[] args) {
        UIManager.put("List.focusCellHighlightBorder", BorderFactory.createEmptyBorder());
        MusicPlayerController controller = new MusicPlayerController();
        controller.init();
    }
}


