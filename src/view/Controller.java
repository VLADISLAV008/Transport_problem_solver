package view;

import calculation.Solver;
import constants.Messages;
import entities.Answer;
import exceptions.AppException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {
    @FXML
    public Label fileName;
    public GridPane transportMatrix;
    public Label totalCost;

    private Stage stage;
    private Solver solver;

    @FXML
    public void loadFile(ActionEvent actionEvent) {
        final FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                solver = getSolver(file);
                fileName.setText("Uploaded file:\n" + file.getName());
            } catch (FileNotFoundException e) {
                showError(new AppException(Messages.FILE_NOT_LOADED, e));
                e.printStackTrace();
            } catch (Exception e) {
                showError(new AppException(Messages.INVALID_FORMAT, e));
                e.printStackTrace();
            }
        }
    }

    private Solver getSolver(File file) throws FileNotFoundException {
        ArrayList<ArrayList<Double>> data = new ArrayList<>();
        Scanner s = new Scanner(file);
        while (s.hasNextLine()) {
            ArrayList<Double> list = parsingData(s.nextLine());
            data.add(list);
        }

        int len = data.size();
        int numConsumption = data.get(len - 1).size();
        Solver solver = new Solver(len - 1, numConsumption);

        double[][] rates = solver.getRates();
        for (int i = 0; i < len - 1; i++) {
            for (int j = 0; j < numConsumption; j++) {
                rates[i][j] = data.get(i).get(j);
            }
            solver.getProductionCapacity().add(data.get(i).get(numConsumption));
        }
        solver.getPowerConsumption().addAll(data.get(len - 1));
        return solver;
    }

    ArrayList<Double> parsingData(String data) {
        ArrayList<Double> result = new ArrayList<>();

        String regex = "\\b(\\d+?(,\\d+?)?)\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);

        while (matcher.find()) {
            String number = matcher.group(1);
            result.add(Double.parseDouble(number));
        }
        return result;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void solveProblem(ActionEvent actionEvent) {
        if (solver == null) {
            showError(new AppException(Messages.FILE_NOT_UPLOADED, new Exception()));
            return;
        }
        Answer answer = solver.getAnswer();
        String cost = Double.toString(answer.getTotalCosts());
        int length = cost.length();
        if (cost.charAt(length - 1) == '0') {
            cost = cost.substring(0, length - 2);
        }
        totalCost.setText(cost);

        double[][] matrix = answer.getPlan();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                Label label = new Label();
                String value = Double.toString(matrix[i][j]);
                length = value.length();
                if (value.charAt(length - 1) == '0') {
                    value = value.substring(0, length - 2);
                }
                label.setText(value);
                GridPane.setHalignment(label, HPos.CENTER);
                transportMatrix.add(label, j, i);
            }
        }
    }

    private void showError(AppException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    public void helpAction() {
        informationDialog(Messages.HELP_MESSAGE);
    }

    public void infoProgramme() {
        informationDialog(Messages.INFO_PROGRAMME);
    }

    public void informationDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(600);
        alert.setTitle("Description");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }

    public void deleteFile(ActionEvent actionEvent) {
        fileName.setText("File not uploaded");
        solver = null;
    }
}
