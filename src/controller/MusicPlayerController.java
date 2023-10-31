package controller;

import model.Song;
import view.MusicPlayerView;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class MusicPlayerController {
    private MusicPlayerView view;
    private List<Song> songs;
    private Song currentSong;
    private Clip audioClip;
    private boolean isPlaying;
    private boolean isPaused;
    private boolean timeStampIsChanging;
    private boolean timeStampIsChangingFromKey;
    private boolean selectingSongFromKey;
    private long clipTimePosition;
    private long currentClipTimePosition;
    private Thread clipUpdate;
    private boolean isLoopingList;
    private boolean isLoopingSong;
    private boolean ctrlPressed = false;
    private long skipMicroSeconds = 1000 * 1000 * 5; // 5 seconds

    public void init() {
        view = new MusicPlayerView();
        view.addSelectListener(new SelectSongListener());
        view.addPlayListener(new PlayButtonListener());
        view.addPauseListener(new PauseButtonListener());
        view.addStopListener(new StopButtonListener());
        view.addTimestampSliderChangeListener(new TimeStampListener());
        view.addLoopOptionListener(new loopOptionListener());

        // Check Current Files in Resources
        readResourcesDirectory();
        songs = loadSongsFromDatabase();
        List<String> songTitles = getSongTitles(songs);
        view.setSongList(songTitles);

        view.setSelectedLoopIndex(1);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_CONTROL: {
                        ctrlPressed = true;
                        break;
                    }
                    case KeyEvent.VK_SPACE: {
                        if (isPlaying) {
                            pauseSong();
                        } else if (view.getSelectedSongIndex()>=0) {
                            int selectedIndex = view.getSelectedSongIndex();
                            playSong(selectedIndex);
                        } else if (view.getMaxSongIndex()>=0){
                            view.setSelectedSongIndex(0);
                        }
                        break;
                    }
                    case KeyEvent.VK_LEFT: {
                        if (audioClip==null) break;
                        if (ctrlPressed) {
                            selectingSongFromKey = true;
                            int selectedIndex = view.getSelectedSongIndex();
                            int maxSelectionIndex = view.getMaxSongIndex();
                            if (maxSelectionIndex>=0) {
                                if (selectedIndex>0){
                                    int newSelectedIndex = --selectedIndex;
                                    view.setSelectedSongIndex(newSelectedIndex);
                                } else {
                                    view.setSelectedSongIndex(maxSelectionIndex);
                                }
                            }
                        } else {
                            timeStampIsChangingFromKey = true;
                            long maxPosition = audioClip.getMicrosecondLength();
                            long prevPosition = Math.max(view.getTimestampSliderValue() - skipMicroSeconds, 0);
                            currentClipTimePosition = prevPosition;
                            String timeStampTime = microMilliToMinuteTime(currentClipTimePosition);
                            String endTimeStampTime = "-"+microMilliToMinuteTime(maxPosition - currentClipTimePosition);
                            view.setTimestampLabelValue(timeStampTime, endTimeStampTime);
                            view.setTimestampSliderValue(Math.toIntExact(currentClipTimePosition));
                        }
                        break;
                    }
                    case KeyEvent.VK_RIGHT: {
                        if (audioClip==null) break;
                        if (ctrlPressed) {
                            selectingSongFromKey = true;
                            int selectedIndex = view.getSelectedSongIndex();
                            int maxSelectionIndex = view.getMaxSongIndex();
                            if (maxSelectionIndex>=0) {
                                if (selectedIndex<maxSelectionIndex){
                                    int newSelectedIndex = ++selectedIndex;
                                    view.setSelectedSongIndex(newSelectedIndex);
                                } else {
                                    int newSelectedIndex = 0;
                                    view.setSelectedSongIndex(newSelectedIndex);
                                }
                            }
                        } else {
                            timeStampIsChangingFromKey = true;
                            long maxPosition = audioClip.getMicrosecondLength();
                            long nextPosition = Math.min(view.getTimestampSliderValue() + skipMicroSeconds, maxPosition);
                            currentClipTimePosition = nextPosition;
                            String timeStampTime = microMilliToMinuteTime(currentClipTimePosition);
                            String endTimeStampTime = "-"+microMilliToMinuteTime(maxPosition - currentClipTimePosition);
                            view.setTimestampLabelValue(timeStampTime, endTimeStampTime);
                            view.setTimestampSliderValue(Math.toIntExact(currentClipTimePosition));
                        }
                        break;
                    }
                    case KeyEvent.VK_UP: {
                        if (!ctrlPressed) break;
                        int maxLoopIndex = view.getMaxLoopIndex();
                        int selectedLoopIndex = view.getSelectedLoopIndex();
                        if (maxLoopIndex>=0) {
                            if (selectedLoopIndex>0){
                                int newSelectedIndex = --selectedLoopIndex;
                                view.setSelectedLoopIndex(newSelectedIndex);
                            } else {
                                view.setSelectedLoopIndex(maxLoopIndex);
                            }
                        }
                        break;
                    }
                    case KeyEvent.VK_DOWN: {
                        if (!ctrlPressed) break;
                        int maxLoopIndex = view.getMaxLoopIndex();
                        int selectedLoopIndex = view.getSelectedLoopIndex();
                        if (maxLoopIndex>=0) {
                            if (selectedLoopIndex<maxLoopIndex){
                                int newSelectedIndex = ++selectedLoopIndex;
                                view.setSelectedLoopIndex(newSelectedIndex);
                            } else {
                                int newSelectedIndex = 0;
                                view.setSelectedLoopIndex(newSelectedIndex);
                            }
                        }
                    }
                }
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                if (ctrlPressed) {
                    if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                        ctrlPressed = false;
                    } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        int selectedIndex = view.getSelectedSongIndex();
                        if (selectedIndex >= 0) {
                            playSong(selectedIndex);
                        }
                        selectingSongFromKey = false;
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        int selectedIndex = view.getSelectedSongIndex();
                        if (selectedIndex >= 0) {
                            playSong(selectedIndex);
                        }
                        selectingSongFromKey = false;
                    }
                } else if (!ctrlPressed) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        if (audioClip != null && timeStampIsChangingFromKey) {
                            timeStampIsChangingFromKey = false;
                            audioClip.setMicrosecondPosition(currentClipTimePosition);
                        } else {
                            timeStampIsChangingFromKey = false;
                        }
                    }
                }
            }
            return true;
        });
    }

    private void readResourcesDirectory() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resourcesURL = classLoader.getResource("");
            File resourcesDirectory = new File(resourcesURL.toURI());
            File musicDirectory = new File(resourcesDirectory, "music");
            File songsDirectory = new File(musicDirectory, "songs");
            File imagesDirectory = new File(musicDirectory, "images");
            File lyricsDirectory = new File(musicDirectory, "lyrics");
            File[] songFiles = songsDirectory.listFiles();
            File[] imageFiles = imagesDirectory.listFiles();
            File[] lyricFiles = lyricsDirectory.listFiles();
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("musicplayerdb");
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("SELECT s FROM Song s");

            List<Song> songs = query.getResultList();
            Set<String> audioNames = new HashSet<>();
            Set<String> imageNames = new HashSet<>();
            for (Song song : songs) {
                File imageFile = new File(song.getImagePath());
                File songFile = new File(song.getAudioPath());
                if (!isValidImage(imageFile) || !isValidAudio(songFile)) {
                    try {
                        em.getTransaction().begin();
                        Song songToRemove = em.find(Song.class, song.getId());
                        if (songToRemove != null) {
                            em.remove(songToRemove);
                        }
                        em.getTransaction().commit();
                    } catch (Exception e) {
                        if (em.getTransaction().isActive()) {
                            em.getTransaction().rollback();
                        }
                    }
                } else {
                    audioNames.add(getFilenameInPathWithoutExtension(song.getAudioPath()));
                    imageNames.add(getFilenameInPathWithoutExtension(song.getImagePath()));
                }
            }
            for (File songFile : songFiles) {
                String songFilename = getFilenameWithoutExtension(songFile.getName());
                if(audioNames.contains(songFilename)) continue;
                for (File lyricFile : lyricFiles) {
                    String lyricFilename = getFilenameWithoutExtension(lyricFile.getName());
                    for (File imageFile : imageFiles) {
                        String imageFilename = getFilenameWithoutExtension(imageFile.getName());
                        if(imageNames.contains(imageFilename)) continue;
                        if (songFilename.equals(lyricFilename) && songFilename.equals(imageFilename)) {
                            String songFilePath = songFile.getPath();
                            String imageFilePath = imageFile.getPath();
                            if (isValidImage(imageFile) &&
                                isValidAudio(songFile) &&
                                isValidLyricsFile(lyricFile)
                            ) {
                                try {
                                    em.getTransaction().begin();
                                    Song newSong = new Song();
                                    newSong.setTitle(songFilename);
                                    Path lyricFilePath = Paths.get(lyricFile.getAbsolutePath());
                                    String lyricsContent = new String(Files.readAllBytes(lyricFilePath));
                                    newSong.setLyrics(lyricsContent);
                                    newSong.setAudioPath(songFilePath);
                                    newSong.setImagePath(imageFilePath);
                                    em.persist(newSong);
                                    em.getTransaction().commit();
                                } catch (Exception e) {
                                    if (em.getTransaction().isActive()) {
                                        em.getTransaction().rollback();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            em.close();
            emf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean isValidLyricsFile(File file) {
        return file.exists() && file.isFile() && file.canRead();
    }
    private boolean isValidImage(File file) {
        try {
            return ImageIO.read(file) != null;
        } catch (IOException e) {
            return false;
        }
    }
    private boolean isValidAudio(File file) {
        try {
            return AudioSystem.getAudioFileFormat(file) != null;
        } catch (UnsupportedAudioFileException | IOException e) {
            return false;
        }
    }
    private String getFilenameWithoutExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        return lastIndex > 0 ? filename.substring(0, lastIndex) : filename;
    }
    private String getFilenameInPathWithoutExtension(String filePath) {
        int lastSeparatorIndex = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        int lastIndex = filePath.lastIndexOf('.');
        if (lastIndex > lastSeparatorIndex) {
            return filePath.substring(lastSeparatorIndex + 1, lastIndex);
        } else {
            return filePath.substring(lastSeparatorIndex + 1);
        }
    }

    private List<Song> loadSongsFromDatabase() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("musicplayerdb");
        EntityManager em = emf.createEntityManager();
        Query query = em.createQuery("SELECT s FROM Song s");
        List<Song> song = query.getResultList();
        em.close();
        emf.close();
        return song;
    }

    private List<String> getSongTitles(List<Song> songs) {
        List<String> songTitles = new ArrayList<>();
        for (Song song : songs) {
            songTitles.add(song.getTitle());
        }
        return songTitles;
    }

    private void playSong(int selectedIndex) {
        if (selectedIndex<0) { return; }
        try {
            Song selectedSong = songs.get(selectedIndex);
            if (currentSong == null || !currentSong.equals(selectedSong)) {
                if (currentSong != null && audioClip!=null) {
                    // Reset Listener
                    audioClip.removeLineListener(this::onAudioClipStopped);
                    // Reset Current Song
                    audioClip.stop();
                    audioClip.setMicrosecondPosition(0);
                }
                // Add in the new Song
                view.displaySongDetails(selectedSong);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(selectedSong.getAudioPath()));
                audioClip = AudioSystem.getClip();
                // Listener that allows method to return once the sound ended
                audioClip.addLineListener(this::onAudioClipStopped);
                audioClip.open(audioStream);
                view.setTimestampSliderVisibility(true);
                view.setTimestampSliderMaxValue(Math.toIntExact(audioClip.getMicrosecondLength()));
                if (clipUpdate instanceof Thread) {
                    clipUpdate.interrupt();
                }
                clipUpdate = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true) {
                            try {
                                if (!timeStampIsChanging && !timeStampIsChangingFromKey && audioClip!=null) {
                                    currentClipTimePosition = audioClip.getMicrosecondPosition();
                                    String timeStampTime = microMilliToMinuteTime(currentClipTimePosition);
                                    String endTimeStampTime = "-"+microMilliToMinuteTime(audioClip.getMicrosecondLength() - currentClipTimePosition);
                                    view.setTimestampLabelValue(timeStampTime, endTimeStampTime);
                                    view.setTimestampSliderValue(Math.toIntExact(currentClipTimePosition));
                                }
                                Thread.sleep(1000);
                            } catch(InterruptedException ignored) {
                                return;
                            }
                        }
                    }
                });
                clipUpdate.start();
                audioClip.setMicrosecondPosition(0);
                currentSong = selectedSong;
                isPlaying = true;
                audioClip.start();
                view.setPlayButtonVisibility(false);
                view.setStopButtonVisibility(true);
                view.setImageVisibility(true);
                view.setPauseButtonVisibility(true);
            } else if (!isPlaying && audioClip!=null) {
                isPlaying = true;
                isPaused = false;
                audioClip.start();
                view.setPlayButtonVisibility(false);
                view.setStopButtonVisibility(true);
                view.setImageVisibility(true);
                view.setPauseButtonVisibility(true);
            }
        } catch (Exception ex) {
            view.displayErrorMessage("Error playing the song.");
            ex.printStackTrace();
        }
    }
    private void onAudioClipStopped(LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP && !isPaused) {
            int selectedIndex = view.getSelectedSongIndex();
            Song selectedSong = songs.get(selectedIndex);
            if (currentSong==null || !currentSong.equals(selectedSong) || audioClip==null) { return; }
            isPlaying = false;
            isPaused = false;
            audioClip.stop();
            clipTimePosition = 0;
            currentClipTimePosition = clipTimePosition;
            audioClip.setMicrosecondPosition(clipTimePosition);
            timeStampIsChanging = false;
            view.setPlayButtonVisibility(true);
            view.setPauseButtonVisibility(false);
            if (isLoopingSong) {
                playSong(selectedIndex);
            } else if (isLoopingList) {
                int maxSelectionIndex = view.getMaxSongIndex();
                if (maxSelectionIndex>=0) {
                    if (selectedIndex<maxSelectionIndex){
                        int newSelectedIndex = ++selectedIndex;
                        view.setSelectedSongIndex(newSelectedIndex);
                    } else {
                        int newSelectedIndex = 0;
                        view.setSelectedSongIndex(newSelectedIndex);
                    }
                }
            }
        }
    }

    private void pauseSong() {
        if (currentSong != null && isPlaying && audioClip!=null) {
            timeStampIsChanging = false;
            isPlaying = false;
            isPaused = true;
            clipTimePosition = audioClip.getMicrosecondPosition();
            currentClipTimePosition = clipTimePosition;
            audioClip.stop();
            view.setPlayButtonVisibility(true);
            view.setPauseButtonVisibility(false);
        } else {
            view.displayErrorMessage("No song is currently playing.");
        }
    }

    private void stopSong() {
        if (currentSong == null || audioClip==null) { return; }
        isPlaying = false;
        isPaused = false;
        currentSong = null;
        audioClip.removeLineListener(this::onAudioClipStopped);
        audioClip.stop();
        clipTimePosition = 0;
        currentClipTimePosition = clipTimePosition;
        audioClip.setMicrosecondPosition(clipTimePosition);
        view.removeSelectedSongIndex();
        view.displaySongDetails(null);
        view.setTimestampSliderVisibility(false);
        view.setPlayButtonVisibility(false);
        view.setPauseButtonVisibility(false);
        view.setStopButtonVisibility(false);
        view.setImageVisibility(false);
    }

    public String microMilliToMinuteTime(long position) {
        long totalMilliSeconds = position / 1000;
        long totalSeconds = totalMilliSeconds / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    class SelectSongListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting() || selectingSongFromKey) return;
            int selectedIndex = view.getSelectedSongIndex();
            if (selectedIndex >= 0) {
                playSong(selectedIndex);
            }
        }
    }
    class PlayButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = view.getSelectedSongIndex();
            if (selectedIndex >= 0) {
                playSong(selectedIndex);
            } else {
                view.displayErrorMessage("Please select a song to play.");
            }
        }
    }
    class PauseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            pauseSong();
        }
    }
    class StopButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            stopSong();
        }
    }
    class TimeStampListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (audioClip==null) { return; }
            int position = view.getTimestampSliderValue();
            String timeStampTime = microMilliToMinuteTime(position);
            String endTimeStampTime = "-"+microMilliToMinuteTime(audioClip.getMicrosecondLength() - position);
            view.setTimestampLabelValue(timeStampTime, endTimeStampTime);
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting()) {
                timeStampIsChanging = true;
                return;
            } else if (timeStampIsChangingFromKey) {
                return;
            } else {
                timeStampIsChanging = false;
            }
            if (Math.abs(currentClipTimePosition - position)<1) { return; }
            long value = view.getTimestampSliderValue();
            if (value >= 0 && audioClip!=null) {
                audioClip.setMicrosecondPosition(value);
            }
        }
    }
    class loopOptionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JComboBox<String> source = (JComboBox<String>) e.getSource();
            String selectedOption = (String) source.getSelectedItem();
            switch (selectedOption) {
                case "Loop List": {
                    isLoopingSong = false;
                    isLoopingList = true;
                    break;
                }
                case "Loop Song": {
                    isLoopingSong = true;
                    isLoopingList = false;
                    break;
                }
                default: {
                    isLoopingSong = false;
                    isLoopingList = false;
                }
            }
        }
    }
}
