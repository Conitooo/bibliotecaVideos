package hellofx;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Controller implements Initializable {

    // Contenedor central del BorderPane
    @FXML
    private VBox centerVBox;
    
    // MediaView para reproducir video
    @FXML
    private MediaView mediaView;
    
    // ImageView para mostrar la imagen cuando se reproduce audio (.mp3)
    @FXML
    private ImageView audioImageView;
    
    @FXML
    private Text videoTitle;
    
    @FXML
    private Button playButton, pauseButton, resetButton, defaultV;
    
    @FXML
    private Slider slider, volumeSlider, speedSlider;
    
    // ListView que mostrará los archivos (videos y audios)
    @FXML
    private ListView<VideoItem> videoList;
    
    @FXML
    private MenuItem about;
    
    // Panel lateral izquierdo (controles)
    @FXML
    private VBox leftSidebar;
    
    private Media media;
    private MediaPlayer mediaPlayer;
    private Stage mainWindow;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuración del cell factory para el ListView: muestra título y duración.
        videoList.setCellFactory(lv -> new ListCell<VideoItem>() {
            @Override
            protected void updateItem(VideoItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox();
                    hbox.setSpacing(20);
                    Label titleLabel = new Label(item.getTitle());
                    Label durationLabel = new Label(item.getDuration());
                    hbox.getChildren().addAll(titleLabel, durationLabel);
                    setGraphic(hbox);
                }
            }
        });
        
        try {
            // Directorio donde se buscan los archivos
            File directory = new File("src/hellofx/media");
            File[] files = directory.listFiles((dir, name) -> 
                name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mp3")
            );
            
            if (files != null) {
                for (File file : files) {
                    String filePath = file.toURI().toString();
                    // Se elimina la extensión para obtener el título
                    String title = file.getName().replaceAll("\\.(mp4|avi|mp3)$", "");
                    VideoItem item = new VideoItem(filePath, title, "Cargando...");
                    videoList.getItems().add(item);
                    
                    // Se crea un Media y un MediaPlayer temporal para obtener la duración
                    try {
                        Media mediaTemp = new Media(filePath);
                        MediaPlayer tempPlayer = new MediaPlayer(mediaTemp);
                        tempPlayer.setOnError(() -> {
                            System.out.println("Error loading media: " + tempPlayer.getError());
                        });
                        tempPlayer.setOnReady(() -> {
                            Duration dur = mediaTemp.getDuration();
                            String formattedDuration = formatDuration(dur);
                            item.setDuration(formattedDuration);
                            Platform.runLater(() -> videoList.refresh());
                            tempPlayer.dispose();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            // Al seleccionar un archivo en la lista se carga y reproduce
            videoList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadMedia(newVal.getFilePath());
                }
            });
            
            // Configuración del slider de volumen (valor inicial 50%)
            volumeSlider.setValue(50);
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(newVal.doubleValue() / 100);
                }
            });
            
            // Configuración del slider de velocidad (valor inicial 1.0)
            speedSlider.setValue(1.0);
            speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (mediaPlayer != null) {
                    mediaPlayer.setRate(newVal.doubleValue());
                }
            });
            
            // Cargar la imagen para el audio (audio_placeholder.jpg debe estar en el classpath)
            Image image = new Image(getClass().getResourceAsStream("messiah.png"));
            audioImageView.setImage(image);
            audioImageView.setPreserveRatio(true);
            audioImageView.setVisible(false);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Asigna la ventana principal.
     */
    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }
    
    // Métodos de control de reproducción
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
    public void resetMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.play();
        }
    }
    
    @FXML
    public void sliderPressed(MouseEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.seconds(slider.getValue()));
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
            mediaPlayer.setRate(speedSlider.getValue());
        }
    }
    
    @FXML
    public void defaultV(MouseEvent e) {
        if (mediaPlayer != null) {
            speedSlider.setValue(1.0);
            mediaPlayer.setRate(1.0);
        }
    }
    
    /**
     * Configura el MediaView en modo "Grande", vinculándolo al contenedor central.
     */
    @FXML
    private void setMediaViewLarge(MouseEvent event) {
        mediaView.setPreserveRatio(true);
        mediaView.fitWidthProperty().unbind();
        mediaView.fitHeightProperty().unbind();
        if (centerVBox != null) {
            mediaView.fitWidthProperty().bind(centerVBox.widthProperty());
            mediaView.fitHeightProperty().bind(centerVBox.heightProperty());
        }
    }
    
    /**
     * Configura el MediaView en modo "Pequeño" (tamaño fijo).
     */
    @FXML
    private void setMediaViewSmall(MouseEvent event) {
        mediaView.setPreserveRatio(true);
        mediaView.fitWidthProperty().unbind();
        mediaView.fitHeightProperty().unbind();
        mediaView.setFitWidth(300);
        mediaView.setFitHeight(200);
    }
    
    /**
     * Carga y reproduce el medio (video o audio).  
     * Si se trata de un archivo .mp3, se muestra la imagen en lugar del video.
     */
    private void loadMedia(String filePath) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        media = new Media(filePath);
        mediaPlayer = new MediaPlayer(media);
        
        // Actualizar el slider conforme avance la reproducción
        mediaPlayer.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
            slider.setValue(newVal.toSeconds());
        });
        
        mediaPlayer.setOnReady(() -> {
            Duration totalDuration = media.getDuration();
            slider.setMax(totalDuration.toSeconds());
            String titulo = new File(filePath).getName().replaceAll("\\.(mp4|avi|mp3)$", "");
            videoTitle.setText(titulo);
            alertaNuevoVideo(titulo);
        });
        
        // Si es un archivo de audio (.mp3), se muestra la imagen; en caso contrario, se muestra el video.
        if (filePath.toLowerCase().endsWith(".mp3")) {
            mediaView.setVisible(false);
            audioImageView.setVisible(true);
        } else {
            mediaView.setVisible(true);
            audioImageView.setVisible(false);
            mediaView.setMediaPlayer(mediaPlayer);
        }
        
        mediaPlayer.play();
    }
    
    /**
     * Muestra el diálogo "Acerca de".
     */
    @FXML
    private void about(ActionEvent event) {
        Platform.runLater(() -> {
            Alert alerta = new Alert(AlertType.INFORMATION);
            alerta.setTitle("Acerca de");
            alerta.setHeaderText("Reproductor Multimedia");
            alerta.setContentText("Tu nombre aquí");
            alerta.showAndWait();
        });
    }
    
    /**
     * Muestra una alerta cuando se detecta un nuevo medio.
     */
    private void alertaNuevoVideo(String titulo) {
        Platform.runLater(() -> {
            Alert alerta = new Alert(AlertType.INFORMATION);
            alerta.setTitle("Nuevo medio detectado");
            alerta.setHeaderText("En reproducción:");
            alerta.setContentText(titulo);
            alerta.show();
        });
    }
    
    /**
     * Formatea un objeto Duration al formato mm:ss.
     */
    private String formatDuration(Duration duration) {
        int totalSeconds = (int) Math.floor(duration.toSeconds());
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    // Métodos para alternar la visibilidad de los paneles laterales
    @FXML
    private void toggleLeftSidebar(ActionEvent event) {
        CheckMenuItem menuItem = (CheckMenuItem) event.getSource();
        boolean mostrar = menuItem.isSelected();
        leftSidebar.setVisible(mostrar);
        leftSidebar.setManaged(mostrar);
    }
    
    @FXML
    private void toggleRightSidebar(ActionEvent event) {
        CheckMenuItem menuItem = (CheckMenuItem) event.getSource();
        boolean mostrar = menuItem.isSelected();
        videoList.setVisible(mostrar);
        videoList.setManaged(mostrar);
    }
    
    /**
     * Clase auxiliar para representar cada archivo en el ListView.
     */
    public static class VideoItem {
        private final String filePath;
        private final String title;
        private String duration; // Ej.: "03:25"
        
        public VideoItem(String filePath, String title, String duration) {
            this.filePath = filePath;
            this.title = title;
            this.duration = duration;
        }
        
        public String getFilePath() {
            return filePath;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getDuration() {
            return duration;
        }
        
        public void setDuration(String duration) {
            this.duration = duration;
        }
    }
}
