package view;

import calculation.Solver;
import entities.Answer;
import exceptions.AppException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utilities.I18N;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {
    @FXML
    public Label fileName;
    public GridPane transportMatrix;
    public Label totalCost;
    public Button buttonSolve;
    public Label labelTotalCost;
    public Label labelMatrixText;
    public Button buttonLoad;
    public Label labelSolve;
    public Menu menuFile;
    public Menu menuLanguage;
    public Menu menuHelp;
    public MenuItem exit;
    public MenuItem menuLoadFile;
    public MenuItem help;
    public MenuItem about;
    public RadioMenuItem russian;
    public RadioMenuItem english;

    private Stage stage;
    private Solver solver;
    File file;

    public void bind() {
        labelTotalCost.textProperty().bind(I18N.createStringBinding("label.cost"));
        buttonSolve.textProperty().bind(I18N.createStringBinding("button.solve"));
        labelMatrixText.textProperty().bind(I18N.createStringBinding("label.matrix"));
        buttonLoad.textProperty().bind(I18N.createStringBinding("button.load"));
        labelSolve.textProperty().bind(I18N.createStringBinding("label.solve"));
        menuFile.textProperty().bind(I18N.createStringBinding("menu.file"));
        menuLanguage.textProperty().bind(I18N.createStringBinding("menu.language"));
        menuHelp.textProperty().bind(I18N.createStringBinding("menu.help"));
        exit.textProperty().bind(I18N.createStringBinding("exit"));
        menuLoadFile.textProperty().bind(I18N.createStringBinding("button.load"));
        help.textProperty().bind(I18N.createStringBinding("menu.help"));
        about.textProperty().bind(I18N.createStringBinding("about"));

        russian.textProperty().bind(I18N.createStringBinding("russian"));
        english.textProperty().bind(I18N.createStringBinding("english"));
    }

    @FXML
    public void loadFile(ActionEvent actionEvent) {
        final FileChooser fileChooser = new FileChooser();
        file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                solver = getSolver(file);
                fileName.setText(I18N.get("uploadedFile") + "\n" + file.getName());
            } catch (FileNotFoundException e) {
                showError(new AppException(I18N.get("FILE_NOT_LOADED"), e));
                e.printStackTrace();
            } catch (Exception e) {
                showError(new AppException(I18N.get("INVALID_FORMAT") + I18N.get("INPUT_FILE_FORMAT"), e));
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
            showError(new AppException(I18N.get("FILE_NOT_UPLOADED"), new Exception()));
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
                label.setFont(Font.font(15));
                GridPane.setHalignment(label, HPos.CENTER);
                transportMatrix.add(label, j, i);
            }
        }
    }

    private void showError(AppException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(600);
        alert.setTitle(I18N.get("error"));
        alert.setHeaderText(null);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    public void helpAction() {
        informationDialog(I18N.get("HELP_MESSAGE") + I18N.get("INPUT_FILE_FORMAT"), I18N.get("menu.help"));
    }

    public void infoProgramme() {
        informationDialog(I18N.get("INFO_PROGRAMME"), I18N.get("about"));
    }

    public void informationDialog(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(600);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }

    public void deleteFile(ActionEvent actionEvent) {
        fileName.setText(I18N.get("label.fileName"));
        solver = null;
        file = null;
    }

    public void translateToRussian(ActionEvent actionEvent) {
        I18N.setLocale(new Locale("ru", ""));
        if (file == null) {
            fileName.setText(I18N.get("label.fileName"));
        } else {
            fileName.setText(I18N.get("uploadedFile") + "\n" + file.getName());
        }
    }

    public void translateToEnglish(ActionEvent actionEvent) {
        I18N.setLocale(Locale.ENGLISH);
        if (file == null) {
            fileName.setText(I18N.get("label.fileName"));
        } else {
            fileName.setText(I18N.get("uploadedFile") + "\n" + file.getName());
        }
    }
}
