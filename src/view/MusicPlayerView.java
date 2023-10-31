package view;

import model.Song;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class MusicPlayerView extends JFrame {
    protected boolean workaround;
    private JList<String> songList;
    public JTextArea lyricsArea;
    private JLabel imageLabel;
    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JComboBox<String> loopOptionsComboBox;
    private JPanel timeStampPanel;
    private JLabel timeStampLabel;
    private JLabel endTimeStampLabel;
    private JSlider timestampSlider; // Add the timestamp slider
    Font customFont = new JComboBox<>().getFont();

    public MusicPlayerView() {
        setTitle("Music Player");
        setBackground(Color.black);
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.black);
        songList = new JList<>();
        songList.setForeground(Color.WHITE);
        songList.setBackground(Color.black);
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        songList.setBorder(BorderFactory.createCompoundBorder(songList.getBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        songList.setFont(customFont);
        songList.setSelectionBackground(new Color(30, 215, 96));
        songList.setSelectionForeground(Color.black);

        lyricsArea = new JTextArea();
        lyricsArea.setBackground(Color.black);
        lyricsArea.setForeground(Color.WHITE);
        lyricsArea.setEditable(false);
        lyricsArea.setBorder(BorderFactory.createCompoundBorder(lyricsArea.getBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        lyricsArea.setFont(customFont);
        lyricsArea.setSelectionColor(new Color(30, 215, 96));
        lyricsArea.setSelectedTextColor(Color.black);

        imageLabel = new JLabel();

        playButton = new JButton("▶");
        pauseButton = new JButton("⏸");
        stopButton = new JButton("■");
        pauseButton.setFont(pauseButton.getFont().deriveFont(Font.BOLD, 25));
        stopButton.setFont(stopButton.getFont().deriveFont(Font.BOLD, 20));
        playButton.setFont(playButton.getFont().deriveFont(Font.BOLD, 20));

        String[] options = {"Select Loop", "Loop List", "Loop Song"};
        loopOptionsComboBox = new JComboBox<>(options);
        loopOptionsComboBox.setBackground(Color.black);
        loopOptionsComboBox.setForeground(Color.white);
        loopOptionsComboBox.setUI(new BasicComboBoxUI(){
            private final Color BACKGROUND_COLOR = Color.BLACK;
            private final Color FOREGROUND_COLOR = Color.WHITE;
            @Override
            protected JButton createArrowButton() {
                JButton arrowButton = new JButton();
                arrowButton.setVisible(false);
                return arrowButton;
            }
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2d = (Graphics2D) g;
                Rectangle t = this.comboBox.getVisibleRect();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(BACKGROUND_COLOR);
                g2d.fillRect(t.x, t.y, t.width, t.height);
            }
            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                bounds = comboBox.getVisibleRect();

                g2d.setFont(comboBox.getFont());
                g2d.setColor(FOREGROUND_COLOR);

                Object selectedItem = this.comboBox.getSelectedItem();
                if (selectedItem != null) {
                    String text = selectedItem.toString();
                    int textX = bounds.width - g2d.getFontMetrics().stringWidth(text);
                    int textY = bounds.y + (bounds.height - g2d.getFontMetrics().getHeight()) / 2 + g2d.getFontMetrics().getAscent();
                    g2d.drawString(text, textX, textY);
                }
            }
        });
        loopOptionsComboBox.setRenderer(new DefaultListCellRenderer() {
            private final Color BACKGROUND_COLOR = Color.BLACK;
            private final Color FOREGROUND_COLOR = Color.WHITE;
            private final Color HIGHLIGHT_COLOR = new Color(30, 215, 96);
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(HIGHLIGHT_COLOR);
                    setForeground(BACKGROUND_COLOR);
                } else {
                    setBackground(BACKGROUND_COLOR);
                    setForeground(FOREGROUND_COLOR);
                }
                return this;
            }
        });
        ((JLabel)loopOptionsComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);

        timestampSlider = new JSlider(JSlider.HORIZONTAL, 0, 0);
        timestampSlider.setPreferredSize(new Dimension(900, 25));
        timestampSlider.setPaintTicks(false);
        timestampSlider.setUI(new BasicSliderUI(timestampSlider) {
            /*Sets the size of the Handler, should be the same as the OVAL size*/
            @Override
            protected Dimension getThumbSize() {
                return new Dimension(12,12);
            }
            @Override
            public void paintFocus(Graphics g) {}
            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                int arc = 4; // Adjust this value to control the roundness of the corners
                int trackHeight = 3;
                int fillTop = 10;
                int fillBottom = fillTop + trackHeight;
                int fillRight = xPositionForValue(timestampSlider.getValue());
                int fillFull = xPositionForValue(timestampSlider.getMaximum());
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(70, 70, 70));
                g2d.fillRoundRect(1, fillTop, fillFull, fillBottom - fillTop, arc, arc);
                g2d.setColor(new Color(30, 215, 96));
                g2d.fillRoundRect(1, fillTop, fillRight, fillBottom - fillTop, arc, arc);
            }
            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Rectangle t = thumbRect;
                g2d.setColor(Color.WHITE);
                g2d.fillOval(t.x, t.y, 12, 12);
            }
            @Override
            protected void scrollDueToClickInTrack(int direction) {
                int value = slider.getValue();
                if (slider.getOrientation() == JSlider.HORIZONTAL) {
                    value = this.valueForXPosition(slider.getMousePosition().x);
                } else if (slider.getOrientation() == JSlider.VERTICAL) {
                    value = this.valueForYPosition(slider.getMousePosition().y);
                }
                slider.setValue(value);
            }
        });
        timeStampLabel = new JLabel("0:00");
        endTimeStampLabel = new JLabel("0:00");

        playButton.setVisible(false);
        pauseButton.setVisible(false);
        stopButton.setVisible(false);
        imageLabel.setVisible(false);
        timestampSlider.setMinimum(0);

        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBackground(Color.black);
        timeStampPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        timeStampPanel.setBackground(Color.black);
        timeStampPanel.setVisible(false);
        JPanel optionPanel = new JPanel(new BorderLayout());
        optionPanel.setBackground(Color.black);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        buttonPanel.setBackground(Color.black);

        playButton.setBackground(Color.black);
        pauseButton.setBackground(Color.black);
        stopButton.setBackground(Color.black);
        playButton.setForeground(Color.white);
        pauseButton.setForeground(Color.white);
        stopButton.setForeground(Color.white);
        playButton.setBorder(BorderFactory.createEmptyBorder());
        pauseButton.setBorder(new EmptyBorder(-7, -4, -7, -4));
        stopButton.setBorder(BorderFactory.createEmptyBorder());
        pauseButton.setRolloverEnabled(false);
        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(stopButton);

        timeStampPanel.add(timeStampLabel);
        timeStampLabel.setBackground(Color.black);
        timeStampLabel.setForeground(Color.white);
        timeStampPanel.add(timestampSlider);
        timestampSlider.setBackground(Color.black);
        timeStampPanel.add(endTimeStampLabel);
        endTimeStampLabel.setBackground(Color.black);
        endTimeStampLabel.setForeground(Color.white);
        controlPanel.add(timeStampPanel, BorderLayout.NORTH);
        JPanel emptyPanel = new JPanel(new FlowLayout());
        emptyPanel.setBackground(Color.black);
        emptyPanel.setPreferredSize(new Dimension(150, 0));
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.add(new JLabel(""));
        optionPanel.add(emptyPanel, BorderLayout.WEST);
        optionPanel.add(loopOptionsComboBox, BorderLayout.EAST);
        optionPanel.add(buttonPanel, BorderLayout.CENTER);
        controlPanel.add(optionPanel, BorderLayout.SOUTH);
        JScrollPane songListPane = new JScrollPane(songList);;
        songListPane.getVerticalScrollBar().setUI(new customScrollBar());
        songListPane.getHorizontalScrollBar().setUI(new customScrollBar());
        songListPane.setBackground(Color.BLACK);
        JScrollPane lyricsPane = new JScrollPane(lyricsArea);
        lyricsPane.getVerticalScrollBar().setUI(new customScrollBar());
        lyricsPane.getHorizontalScrollBar().setUI(new customScrollBar());
        lyricsPane.setSize(350,350);
        imageLabel.setSize(350, 350);
        lyricsPane.setBackground(Color.BLACK);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, songListPane, lyricsPane);
        splitPane.setBackground(Color.BLACK);
        splitPane.setDividerLocation(200);
        splitPane.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void setBorder(Border border) {}
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(Color.BLACK);
                        g.fillRect(0, 0, getSize().width, getSize().height);
                    }
                };
            }
        });

        Border outerBorder = BorderFactory.createEmptyBorder();
        Border innerBorder = BorderFactory.createLineBorder(new Color(70,70,70), 2);
        songListPane.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
        lyricsPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,10,0,0), innerBorder));
        imageLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,20,0,0), innerBorder));
        splitPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,20,0,20), BorderFactory.createEmptyBorder()));
        timeStampPanel.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));
        loopOptionsComboBox.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        optionPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,20));
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 15, 10));
        contentPanel.add(splitPane, BorderLayout.CENTER);
        contentPanel.add(controlPanel, BorderLayout.SOUTH);
        contentPanel.add(imageLabel, BorderLayout.WEST);

        add(contentPanel);
        pack();
        workaround=true;
        setMinimumSize(new Dimension(1300, 580));
        workaround=false;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    @Override
    public void setSize(int width, int height) {
        if (workaround) {
            return;
        }
        super.setSize(width, height);
    }
    @Override
    public void setPreferredSize(Dimension dimension) {
        if (workaround) {
            return;
        }
        super.setPreferredSize(dimension);
    }

    public void setSongList(List<String> songTitles) {
        songList.setListData(songTitles.toArray(new String[0]));
    }

    public void displaySongDetails(Song song) {
        if (song != null) {
            int caretPosition = lyricsArea.getCaretPosition();
            lyricsArea.setText(song.getLyrics());
            lyricsArea.setCaretPosition(caretPosition);
            ImageIcon imageIcon = new ImageIcon(song.getImagePath());
            Image image = imageIcon.getImage().getScaledInstance(350, 350,  java.awt.Image.SCALE_SMOOTH);
            imageIcon = new ImageIcon(image);
            imageLabel.setIcon(imageIcon);
        } else {
            lyricsArea.setText("");
            imageLabel.setIcon(null);
        }
    }
    public void setSelectedSongIndex(int index) {
        songList.setSelectedIndex(index);
    }
    public int getSelectedSongIndex() {
        return songList.getSelectedIndex();
    }
    public int getMaxSongIndex() {
        return songList.getModel().getSize()-1;
    }
    public int getSelectedLoopIndex() { return loopOptionsComboBox.getSelectedIndex(); }
    public int getMaxLoopIndex() { return loopOptionsComboBox.getItemCount()-1; }
    public void setSelectedLoopIndex(int index) {
        loopOptionsComboBox.setSelectedIndex(index);
    }
    public void removeSelectedSongIndex() {
        songList.clearSelection();
    }
    public void setPlayButtonVisibility(boolean isVisible) {
        playButton.setVisible(isVisible);
    }
    public void setPauseButtonVisibility(boolean isVisible) {
        pauseButton.setVisible(isVisible);
    }
    public void setStopButtonVisibility(boolean isVisible) {
        stopButton.setVisible(isVisible);
    }
    public void setImageVisibility(boolean isVisible) {
        imageLabel.setVisible(isVisible);
    }
    public void setTimestampSliderVisibility(boolean isVisible) {
        timeStampPanel.setVisible(isVisible);
    }
    public void setTimestampSliderMaxValue(int maxValue) {
        timestampSlider.setMaximum(maxValue);
    }
    public void setTimestampLabelValue(String timestampLabelValue, String endTimestampLabelValue) {
        timeStampLabel.setText(timestampLabelValue);
        endTimeStampLabel.setText(endTimestampLabelValue);
    }
    public void setTimestampSliderValue(int value) {
        timestampSlider.setValue(value);
    }
    public int getTimestampSliderValue() {
        return timestampSlider.getValue();
    }
    public void addTimestampSliderChangeListener(ChangeListener listener) {
        timestampSlider.addChangeListener(listener);
    }
    public void addSelectListener(ListSelectionListener listener) {
        songList.addListSelectionListener(listener);
    }
    public void addPlayListener(ActionListener listener) {
        playButton.addActionListener(listener);
    }
    public void addPauseListener(ActionListener listener) {
        pauseButton.addActionListener(listener);
    }
    public void addStopListener(ActionListener listener) {
        stopButton.addActionListener(listener);
    }
    public void addLoopOptionListener(ActionListener listener) {
        loopOptionsComboBox.addActionListener(listener);
    }

    public void displayErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    class customScrollBar extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            scrollBarWidth = 12;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2d = (Graphics2D) g;
            Rectangle t = trackRect;
            g2d.setColor(Color.BLACK);
            g2d.fillRect(t.x, t.y, t.width, t.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2d = (Graphics2D) g;
            int arc = 10;
            Rectangle t = thumbRect;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(70,70,70));
            g2d.fillRoundRect(t.x+3, t.y+3, t.width-6, t.height-6, arc, arc);
        }
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return new JButton() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(0, 0);
                }
            };
        }
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return new JButton() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(0, 0);
                }
            };
        }
    }
}
