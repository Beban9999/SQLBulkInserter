package com.example.sqlbulker;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class HelloApplication extends Application {

    private List<HBox> fieldBoxes;
    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Database App");

        // Create labels
        Label dbLabel = new Label("Database Name:");
        Label tableLabel = new Label("Table Name:");
        Label fieldNameLabel = new Label("Field Name:");
        Label fieldTypeLabel = new Label("Field Type:");
        Label numInsertsLabel = new Label("Number of Inserts:");

        // Create text fields
        TextField dbTextField = new TextField();
        TextField tableTextField = new TextField();
        TextField fieldTextField = new TextField();
        TextField numInsertsTextField = new TextField();

        // Create drop-down box for field types
        ComboBox<String> fieldTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "int", "char", "varchar", "date", "long"));

        // Create "ADD" button
        Button addButton = new Button("ADD");

        // Create "Generate" button
        Button generateButton = new Button("Generate");

        // Create horizontal lines
        Separator separator1 = new Separator();
        Separator separator2 = new Separator();
        Separator separator3 = new Separator();

        // Create grid pane
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));

        // Add labels and text fields to the grid pane
        gridPane.add(dbLabel, 0, 0);
        gridPane.add(dbTextField, 1, 0);
        gridPane.add(tableLabel, 0, 1);
        gridPane.add(tableTextField, 1, 1);
        gridPane.add(separator1, 0, 2, 4, 1);
        gridPane.add(fieldNameLabel, 0, 3);
        gridPane.add(fieldTextField, 1, 3);
        gridPane.add(fieldTypeLabel, 2, 3);
        gridPane.add(fieldTypeComboBox, 3, 3);
        gridPane.add(addButton, 4, 3);
        gridPane.add(numInsertsLabel, 0, 4);
        gridPane.add(numInsertsTextField, 1, 4);
        gridPane.add(generateButton, 4, 4);
        gridPane.add(separator2, 0, 5, 4, 1);

        // Create VBox to hold dynamically added field boxes
        VBox fieldsContainer = new VBox(10);
        fieldBoxes = new ArrayList<>(); // Initialize the list of field boxes

        // Add title label for fields
        Label fieldsTitleLabel = new Label("Fields");
        fieldsContainer.getChildren().add(fieldsTitleLabel);

        // Add VBox to the grid pane
        gridPane.add(fieldsContainer, 0, 6, 5, 1);

        // Event handling for the ADD button
        addButton.setOnAction(event -> {
            String fieldName = fieldTextField.getText().trim();
            String fieldType = fieldTypeComboBox.getValue();

            if (!fieldName.isEmpty() && fieldType != null) {
                HBox fieldBox = new HBox(10);

                Label fieldLabel;
                if (fieldType.equals("varchar") || fieldType.equals("char")) {
                    // If it's varchar or char, add a text field for length
                    TextField lengthTextField = new TextField();
                    fieldLabel = new Label(fieldName + " (" + fieldType + "): Length");
                    fieldBox.getChildren().addAll(fieldLabel, lengthTextField);
                } else {
                    fieldLabel = new Label(fieldName + " (" + fieldType + ")");
                    fieldBox.getChildren().add(fieldLabel);
                }

                // Create Default Values button
                Button defaultValuesButton = new Button("Default Values");
                defaultValuesButton.setOnAction(e -> {
                    String defaultValues = showDefaultValuesPopup();
                    fieldLabel.setText(fieldName + " (" + fieldType + "): Default Values");
                    fieldBox.getChildren().add(new Label(defaultValues));
                });

                // Add the Default Values button
                fieldBox.getChildren().add(defaultValuesButton);

                // Add the field box to the list and fields container
                fieldBoxes.add(fieldBox);
                fieldsContainer.getChildren().add(fieldBox);

                // Clear the field name text field
                fieldTextField.clear();
            }
        });

        // Event handling for the Generate button
        generateButton.setOnAction(event -> {
            String dbName = dbTextField.getText().trim();
            String tableName = tableTextField.getText().trim();
            String numInsertsText = numInsertsTextField.getText().trim();

            if (!dbName.isEmpty() && !tableName.isEmpty() && !numInsertsText.isEmpty()) {
                int numInserts = Integer.parseInt(numInsertsText);

                List<String> fieldNames = new ArrayList<>();
                for (HBox fieldBox : fieldBoxes) {
                    Label fieldLabel = (Label) fieldBox.getChildren().get(0);
                    String fieldLabelText = fieldLabel.getText();
                    String fieldName = fieldLabelText.substring(0, fieldLabelText.indexOf(" ("));
                    fieldNames.add(fieldName);
                }

                StringBuilder insertStatement = new StringBuilder();
                insertStatement.append("USE ").append(dbName).append(";\n");
                insertStatement.append("INSERT INTO ").append(tableName).append("\n");
                insertStatement.append("(").append(String.join(", ", fieldNames)).append(")\n");
                insertStatement.append("VALUES\n");

                for (int i = 0; i < numInserts; i++) {
                    insertStatement.append("(");

                    int fieldIndex = 0;
                    int totalFields = fieldBoxes.size();

                    for (HBox fieldBox : fieldBoxes) {
                        Label fieldLabel = (Label) fieldBox.getChildren().get(0);
                        String fieldType = fieldLabel.getText().substring(fieldLabel.getText().indexOf("(") + 1, fieldLabel.getText().indexOf(")"));

                        if (fieldLabel.getText().contains("Default Values")) {
                            // Field has default values, use them instead
                            Label defaultValuesLabel = (Label) fieldBox.getChildren().get(fieldBox.getChildren().size() - 1);
                            String defaultValues = defaultValuesLabel.getText();


                            String[] defaultValuesArray = defaultValues.split(",");
                            // Randomly select one of the default values
                            String defaultValue = defaultValuesArray[new Random().nextInt(defaultValuesArray.length)];


                            if(fieldType.equals("varchar") || fieldType.equals("char") || (fieldType.equals("date")))
                            {
                                insertStatement.append('\''+defaultValue+'\'');
                            }
                            else
                            {
                                insertStatement.append(defaultValue);
                            }

                        } else if (fieldType.equals("varchar") || fieldType.equals("char")) {
                            TextField lengthTextField = (TextField) fieldBox.getChildren().get(1);
                            String length = lengthTextField.getText().trim();
                            if (length.isEmpty()) {
                                length = "30"; // Default length if not specified
                            }
                            insertStatement.append("'").append(getRandomString(Integer.parseInt(length))).append("'");
                        } else if (fieldType.equals("int") || fieldType.equals("long")) {
                            insertStatement.append(getRandomNumber());
                        } else if (fieldType.equals("date")) {
                            insertStatement.append("'").append(getRandomDate()).append("'");
                        } else {
                            insertStatement.append("value"); // Example value
                        }

                        fieldIndex++;
                        if (fieldIndex < totalFields) {
                            insertStatement.append(", ");
                        }
                    }

                    insertStatement.append(")");

                    if (i < numInserts - 1) {
                        insertStatement.append(",\n");
                    } else {
                        insertStatement.append(";\n");
                    }
                }

                String fileName = "db_" + dbName + "_tbl_" + tableName + ".txt";

                try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                    writer.print(insertStatement.toString());
                    System.out.println("TSQL insert statements generated and saved to " + fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Create a scene and set it to the stage
        Scene scene = new Scene(gridPane, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private String getRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = new Random().nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }

        return sb.toString();
    }

    private int getRandomNumber() {
        return new Random().nextInt(100000) + 1;
    }

    private String getRandomDate() {
        LocalDate startDate = LocalDate.of(1970, 1, 1);
        LocalDate endDate = LocalDate.now();
        long startEpochDay = startDate.toEpochDay();
        long endEpochDay = endDate.toEpochDay();

        long randomEpochDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay);
        LocalDate randomDate = LocalDate.ofEpochDay(randomEpochDay);

        return randomDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private String showDefaultValuesPopup() {
        Stage popupStage = new Stage();
        popupStage.setTitle("Default Values");

        // Create a text field for user input
        TextField inputTextField = new TextField();
        inputTextField.setPromptText("Enter default values");

        // Create a button to submit the input
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(event -> popupStage.close());

        // Create a VBox to hold the input components
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(inputTextField, submitButton);

        // Create a scene and set it to the popup stage
        Scene scene = new Scene(vbox);
        popupStage.setScene(scene);
        popupStage.showAndWait();

        // Get the user's input from the text field
        return inputTextField.getText().trim();
    }

    public static void main(String[] args) {
        launch();
    }
}