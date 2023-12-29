package application;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.concurrent.ForkJoinPool;

import exercise1.CountSetTask;
import exercise1_1.WordsFinder;
import exercise2.BusChannels;
import exercise2.MapMessageCodec;
import exercise2.VerticleWordsFinder;
import exercise3.ReactiveWordsFinder;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Controller implements Initializable {
	
	private static final int K = 10; // K = numero di parole più frequenti (da ricercare)
	private static final int N = 3; // N = numero minimo di caratteri delle parole cercate
	private static final long T = 100000; // un file di testo di grandi dimensioni va diviso in più parti, ogni parte è costituita da un minimo di T caratteri

	private long activityStartTime;
	private String selectedExercise;	
    private Vertx vertx;
    private MessageConsumer<Map<String, Integer>> messageConsumer;
    private CountSetTask masterTask;
    private WordsFinder wordsFinder;
	private ReactiveWordsFinder reactiveWordsFinder;
	
	@FXML private Button search;
	@FXML private Button delete;
	@FXML private Button start;	
	@FXML private Button stop;
	@FXML private ListView<String> listViewFiles;
	@FXML private TableView<Entry<String, Integer>> tableView;
	@FXML private TableColumn<Entry<String, Integer>, String> colWord;
	@FXML private TableColumn<Entry<String, Integer>, String> colOcc;
	@FXML private Label numFile;
	@FXML private Label timeToComplete;
	@FXML private Label status;
	@FXML private ChoiceBox<String> solution;
	@FXML private String ex1; 
	@FXML private String ex1_1;
    @FXML private String ex2; 
    @FXML private String ex3; 

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Text Files", "*.txt"), 
				new FileChooser.ExtensionFilter("Java Files", "*.java")
		);
		
		search.setOnAction(event -> {
			List<File> fileChooserList = fileChooser.showOpenMultipleDialog(new Stage());
			ObservableList<String> fileList = listViewFiles.getItems();
			if (fileChooserList != null) {
				for (File file : fileChooserList) {
					String selectedFile = file.getAbsolutePath();
					if (!fileList.contains(selectedFile)) {
						fileList.add(selectedFile);
						numFile.setText("Num files: " + listViewFiles.getItems().size());
						if (selectedExercise == ex1 && masterTask != null) {
							masterTask.addFile(selectedFile);
						} else if (selectedExercise == ex1_1 && wordsFinder != null) {
							wordsFinder.addFile(selectedFile);
						} else if (selectedExercise == ex2) {
							vertx.eventBus().send(BusChannels.ADD_FILE, selectedFile);
						} else if (selectedExercise == ex3 && reactiveWordsFinder != null) {
							reactiveWordsFinder.addFile(selectedFile);
						} 
					}
				}
			}			
		});
		
		start.setOnAction(event -> {
			prepareGuiForComputation();
			status.setText("Stato della computazione: COMPUTING");
			selectedExercise = solution.getValue();
			activityStartTime = System.currentTimeMillis();
			if (selectedExercise == ex1) {
				exercise1();
			} else if (selectedExercise == ex1_1) {
				exercise1_1();
			} else if (selectedExercise == ex2) {
				exercise2();
			} else if (selectedExercise == ex3) {
				exercise3();
			}
		});	
		
		stop.setDisable(true);
		stop.setOnAction(event -> {
            stop.setDisable(true);
            start.setDisable(false);
            status.setText("Stato della computazione: STOP");
            if (masterTask != null && !masterTask.isCancelled()) {
                masterTask.cancel(true);
            }
            if (wordsFinder != null) {
            	wordsFinder.stopComputation();
            }
            if (reactiveWordsFinder != null) {
                reactiveWordsFinder.stopComputation();
            }
            if (vertx != null) {
            	vertx.close();	
            }
            if (messageConsumer != null && messageConsumer.isRegistered()) {
            	messageConsumer.unregister();
            }
        });

		delete.setOnAction(event -> {
			int selectedIdx = listViewFiles.getSelectionModel().getSelectedIndex();
			if (selectedIdx != -1) {
				int newSelectedIdx = (selectedIdx == listViewFiles.getItems().size() - 1) ? (selectedIdx - 1) : selectedIdx;
				String selectedFile = listViewFiles.getItems().get(selectedIdx);
				if (selectedExercise == ex1 && masterTask != null) {
					masterTask.removeFile(selectedFile);
				} else if (selectedExercise == ex1_1 && wordsFinder != null) {
					wordsFinder.removeFile(selectedFile);
				} else if (selectedExercise == ex2) {
					vertx.eventBus().send(BusChannels.REMOVE_FILE, selectedFile);
				} else if (selectedExercise == ex3 && reactiveWordsFinder != null) {
					reactiveWordsFinder.removeFile(selectedFile);
				}				
				listViewFiles.getItems().remove(selectedIdx);
				numFile.setText("Num files: " + listViewFiles.getItems().size());
				listViewFiles.getSelectionModel().select(newSelectedIdx);
			}
		});
		
		colWord.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getKey()));
		colOcc.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().toString()));	
		tableView.setEditable(false);
		tableView.getSelectionModel().setCellSelectionEnabled(true);
	}

	private ObservableList<Entry<String, Integer>> generateDataInMap(Map<String, Integer> data) {
		return FXCollections.observableArrayList(data.entrySet());
	}

	public void exercise1() {
		ForkJoinPool forkJoinPool = new ForkJoinPool();
		masterTask = new CountSetTask(result -> {				
			if (result == null) { completeComputation(); }
			else { updateGui(result); }	
		}, listViewFiles.getItems(), K, N, T);
		forkJoinPool.execute(masterTask);	
	}
	
	public void exercise1_1() {
		wordsFinder = new WordsFinder(result -> {
			if (result == null) { completeComputation(); } 
			else { updateGui(result); }
		}, listViewFiles.getItems(), K, N, T);
		wordsFinder.start();
	}
	
	public void exercise2() {
		vertx = Vertx.vertx();	
        vertx.eventBus().registerCodec(new MapMessageCodec());
		messageConsumer = vertx.eventBus().consumer(BusChannels.SHOW_OUTPUT, message -> updateGui(message.body()));
		vertx.deployVerticle(new VerticleWordsFinder(listViewFiles.getItems(), K, N));
	}
	
	public void exercise3() {
		reactiveWordsFinder = new ReactiveWordsFinder(result -> {		
			if (result == null) { completeComputation(); } 
			else { updateGui(result); }		
		}, K, N);
		reactiveWordsFinder.initializeStream(listViewFiles.getItems());
 	}
	
	public void prepareGuiForComputation() {
		tableView.getItems().clear();
        timeToComplete.setText("");
        numFile.setText("Num files: " + listViewFiles.getItems().size());
        status.setText("Stato della computazione: COMPUTING");
        start.setDisable(true);
        stop.setDisable(false);
    }
	
	public void updateGui(Map<String, Integer> res) {
		Platform.runLater(() -> {
			tableView.setItems(generateDataInMap(res));
			final long doneTime = System.currentTimeMillis();
            timeToComplete.setText("Tempo: " + (doneTime - activityStartTime) + " ms");
		});
	}
	
	public void completeComputation() {
        Platform.runLater(() -> {
        	status.setText("Stato della computazione: COMPLETED");
            start.setDisable(false);
            stop.setDisable(true);
        });
    }
	
}
