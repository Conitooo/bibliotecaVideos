package hellofx;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Controller implements Initializable {

    @FXML
    private MediaView mediaView;

    @FXML
    private Text videoTitle;

    @FXML
    private Button playButton, pauseButton, resetButton, defaultV, fbutton, sbutton, tbutton;

    @FXML
    private Slider slider, volumeSlider, speedSlider;

    @FXML
    private ListView<String> videoList;

    private Media media;
    private MediaPlayer mediaPlayer;
    private Stage mainWindow;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            File directory = new File("src/hellofx");
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".mp4") || name.endsWith(".avi"));

            if (files != null) {
                for (File file : files) {
                    videoList.getItems().add(file.toURI().toString());
                }
            }

            videoList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    loadVideo(newValue);
                }
            });

            volumeSlider.setValue(50);
            volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(newValue.doubleValue() / 100);
                }
            });

            speedSlider.setValue(1.0);
            speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (mediaPlayer != null) {
                    mediaPlayer.setRate(newValue.doubleValue());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }

    @FXML
    public void playMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    @FXML
    public void pauseMedia() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
        }
    }

    @FXML
    public void sliderPressed(MouseEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.seconds(slider.getValue()));
        }
    }

    @FXML
    public void resetMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.play();
        }
    }

    @FXML
    public void changeVolume(MouseEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volumeSlider.getValue() / 100);
        }
    }

    @FXML
    public void changeSpeed(MouseEvent event) {
        if (mediaPlayer != null) {
            double speed = speedSlider.getValue();
            mediaPlayer.setRate(speed);
        }
    }

    @FXML
    public void defaultV(MouseEvent e) {
        if (mediaPlayer != null) {
            speedSlider.setValue(1.0);
            mediaPlayer.setRate(1.0);
        }
    }

    @FXML
    private void setAspectRatio4_3() {
        mediaView.setPreserveRatio(true);
        if (mediaView.getParent() instanceof Pane) {
            Pane parentPane = (Pane) mediaView.getParent();
            mediaView.fitWidthProperty().unbind();
            mediaView.fitHeightProperty().unbind();
            mediaView.fitWidthProperty().bind(parentPane.widthProperty());
            mediaView.fitHeightProperty().bind(parentPane.widthProperty().multiply(3.0 / 4));
        }
    }

    @FXML
    private void setAspectRatio16_9() {
        mediaView.setPreserveRatio(true);
        if (mediaView.getParent() instanceof Pane) {
            Pane parentPane = (Pane) mediaView.getParent();
            mediaView.fitWidthProperty().unbind();
            mediaView.fitHeightProperty().unbind();
            mediaView.fitWidthProperty().bind(parentPane.widthProperty());
            mediaView.fitHeightProperty().bind(parentPane.widthProperty().multiply(9.0 / 16));
        }
    }

    @FXML
    private void setAspectRatioFill() {
        mediaView.setPreserveRatio(false);
        if (mediaView.getParent() instanceof Pane) {
            Pane parentPane = (Pane) mediaView.getParent();
            mediaView.fitWidthProperty().unbind();
            mediaView.fitHeightProperty().unbind();
            mediaView.fitWidthProperty().bind(parentPane.widthProperty());
            mediaView.fitHeightProperty().bind(parentPane.heightProperty());
        }
    }

    private void loadVideo(String filePath) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        media = new Media(filePath);
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);

        mediaPlayer.currentTimeProperty().addListener((observableValue, oldValue, newValue) -> {
            slider.setValue(newValue.toSeconds());
        });

        mediaPlayer.setOnReady(() -> {
            Duration totalDuration = media.getDuration();
            slider.setMax(totalDuration.toSeconds());
            videoTitle.setText(new File(filePath).getName().toString().replace(".mp4", ""));
        });

        mediaPlayer.play();
    }
}