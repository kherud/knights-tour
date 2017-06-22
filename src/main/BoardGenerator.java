package main;

import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Pair;


public class BoardGenerator {

    private String[] colLabels = Configuration.instance.colLabels;
    private String[] rowLabels = Configuration.instance.rowLabels;
    private int size = 6;
    private int amountOfThreads = 1;
    private String colorBlack = "#d1d1e0";
    private boolean running;
    private long timeNeeded = 0;
    private boolean animationDisabled = false;
    private boolean showSolutions = true;
    private int lastSolutionFoundThreadIndex;
    private ArrayList<TourCalculator> tourCalculators;
    private int totalSolutions = 0;
    private int threadsDone = 0;

    @FXML
    Button startButton;

    @FXML
    Label amountOfFieldsLabel;

    @FXML
    Slider amountOfThreadsSlider;

    @FXML
    Label amountOfThreadsLabel;

    @FXML
    GridPane chessBoard1;

    @FXML
    GridPane chessBoard2;

    @FXML
    GridPane chessBoard3;

    @FXML
    GridPane chessBoard4;

    @FXML
    GridPane chessBoard5;

    @FXML
    Slider amountOfFieldsSlider;

    @FXML
    TextField animationDelayInput;

    @FXML
    Label animationDelayLabel;

    @FXML
    Label animationDelayDescriptionLabel;

    @FXML
    Label inputError;

    @FXML
    Label noSolutionsError1;

    @FXML
    Label noSolutionsError2;

    @FXML
    Label noSolutionsError3;

    @FXML
    Label noSolutionsError4;

    @FXML
    Pane solutionPane;

    @FXML
    Label solutionLabel;

    @FXML
    Label solutionsLabel1;

    @FXML
    Label solutionsLabel2;

    @FXML
    Label solutionsLabel3;

    @FXML
    Label solutionsLabel4;

    @FXML
    Label solutionsLabel5;

    @FXML
    Label solutionsLabel6;

    @FXML
    Label timeNeededLabel;

    @FXML
    public void initialize() {
        tourCalculators = new ArrayList<>();
        generateBoards();
        generateCalculators();
    }

    @FXML
    protected void buttonPressed() {
        if (!running) {
            hideNoSolutionsErrors();
            resetSolutions();
            generateBoards();
            if (!animationDisabled) updateAnimationDelay();
            startButton.setText("Stop");
            running = true;
            timeNeeded = System.currentTimeMillis();
            for (TourCalculator tourCalculator : tourCalculators) {
                Thread th = new Thread(tourCalculator);
                th.start();
            }
        } else {
            stop();
        }
    }

    private void hideNoSolutionsErrors() {
        for (int i = 1; i < 5; i++) {
            showNoSolutionsError(i, false);
        }
    }

    @FXML
    protected void toggleAnimationEnabled() {
        animationDisabled = !animationDisabled;
        animationDelayInput.setDisable(animationDisabled);
        animationDelayLabel.setDisable(animationDisabled);
        animationDelayDescriptionLabel.setDisable(animationDisabled);
        for (TourCalculator tc : tourCalculators) {
            tc.setAnimationDisabled(animationDisabled);
            if (running) setBoard(tc.getChessBoardIndex(), tc.getBoard());
        }
    }

    @FXML
    protected void toggleShowSolutionsEnabled() {
        showSolutions = !showSolutions;
        for (TourCalculator tc : tourCalculators) {
            tc.setShowSolutions(showSolutions);
        }
    }

    @FXML
    protected void updateAmountOfFields() {
        size = (int) amountOfFieldsSlider.getValue();
        amountOfFieldsLabel.setText(String.valueOf(size));
        uiChangeProcedure();
    }

    private void generateBoards() {
        generateBoard(chessBoard1, 1);
        generateBoard(chessBoard2, 2);
        generateBoard(chessBoard3, 3);
        generateBoard(chessBoard4, 4);
    }

    @FXML
    protected void updateAmountOfThreads() {
        int value = (int) amountOfThreadsSlider.getValue();
        amountOfThreads = value;
        amountOfThreadsLabel.setText(String.valueOf(value));
        uiChangeProcedure();
    }

    @FXML
    protected void closeSolutionPane() {
        solutionPane.setVisible(false);
        tourCalculators.get(lastSolutionFoundThreadIndex).solutionSeen();
    }

    private void uiChangeProcedure() {
        stop();
        resetSolutions();
        hideNoSolutionsErrors();
        generateBoards();
        generateCalculators();
    }

    @FXML
    protected void updateAnimationDelay() {
        String input = animationDelayInput.getText();
        int value = 500;
        try {
            value = Math.abs(Integer.valueOf(input));
            showInputError(false);
        } catch (NumberFormatException e) {
            showInputError(true);
        }
        if (value == 0) value = 1;
        for (TourCalculator tc : tourCalculators) {
            tc.setDelay(value);
        }
    }

    private void showInputError(boolean visible) {
        inputError.setVisible(visible);
    }

    private void resetSolutions() {
        totalSolutions = 0;
        solutionsLabel1.setText("0");
        solutionsLabel2.setText("0");
        solutionsLabel3.setText("0");
        solutionsLabel4.setText("0");
        solutionsLabel5.setText("0");
        solutionsLabel6.setText("0");
        timeNeededLabel.setText("");
    }

    private void generateCalculators() {
        tourCalculators = new ArrayList<>();
        SolutionManager solutionManager = new SolutionManager();
        for (int i = 1; i <= amountOfThreads; i++) {
            TourCalculator tc = new TourCalculator(size, i, animationDisabled, showSolutions, this, solutionManager);
            tourCalculators.add(tc);
            tc.assignFieldsToAnalyze(amountOfThreads);
            tc.showFieldsToAnalyze();
        }
    }

    private void stop() {
        running = false;
        startButton.setText("Start");
        for (TourCalculator tc : tourCalculators) {
            tc.setIsRunning(false);
        }
        TourRecorder.instance.shutdown();
    }

    public void setField(int boardIndex, int row, int col, int number) {
        try {
            GridPane chessBoard = getChessboard(boardIndex);
            StackPane square = (StackPane) chessBoard.lookup("#field" + row + col);
            Text text = new Text(String.valueOf(number));
            text.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.REGULAR, boardIndex == 5 ? 35 : 25));
            text.setId("assignedValue" + row + col);
            if (boardIndex != 5) {
                String color = ((row + col) % 2 == 0) ? "#f7fff7" : "#D1E1E0";
                square.setStyle("-fx-background-color: " + color + ";");
            }
            GridPane.setHalignment(text, HPos.CENTER);
            GridPane.setValignment(text, VPos.CENTER);
            chessBoard.add(text, col, row);
            text.toFront();
        } catch (NullPointerException e) {
            // System.out.println("Field could not be set, board already reset (thread delay).");
        }
    }

    public void resetField(int boardIndex, int row, int col) {
        try {
            GridPane chessBoard = getChessboard(boardIndex);
            StackPane square = (StackPane) chessBoard.lookup("#field" + row + col);
            Text text = (Text) chessBoard.lookup("#assignedValue" + row + col);
            chessBoard.getChildren().remove(text);
            String color = ((row + col) % 2 == 0) ? "white" : colorBlack;
            square.setStyle("-fx-background-color: " + color + ";");
        } catch (NullPointerException e) {
            // System.out.println("Field could not be reset, board already reset (thread delay).");
        }
    }

    public void showFieldsToAnalyze(int boardIndex, ArrayList<Pair<Integer, Integer>> fields) {
        GridPane chessBoard = getChessboard(boardIndex);
        for (Node field : chessBoard.getChildren()) {
            field.setOpacity(0.2);
        }
        for (Pair<Integer, Integer> field : fields) {
            try {
                chessBoard.lookup("#field" + field.getKey() + field.getValue()).setOpacity(1);
                chessBoard.lookup("#fieldDescription" + field.getKey() + field.getValue()).setOpacity(1);
            } catch (NullPointerException e) {
                // System.out.println("Analyzed field could not be shown, board changed (thread delay)");
            }
        }
    }

    private GridPane getChessboard(int index) {
        switch (index) {
            case 1:
                return chessBoard1;
            case 2:
                return chessBoard2;
            case 3:
                return chessBoard3;
            case 4:
                return chessBoard4;
            case 5:
                return chessBoard5;
            default:
                return chessBoard1;
        }
    }

    public void showNoSolutionsError(int index, boolean visible) {
        switch (index) {
            case 1:
                noSolutionsError1.setVisible(visible);
                break;
            case 2:
                noSolutionsError2.setVisible(visible);
                break;
            case 3:
                noSolutionsError3.setVisible(visible);
                break;
            case 4:
                noSolutionsError4.setVisible(visible);
                break;
        }
    }

    public void showNoSolutionsError(int index, ArrayList<Pair<Integer, Integer>> fieldsAnalyzed, boolean visible) {
        switch (index) {
            case 1:
                noSolutionsError1.setVisible(visible);
                showFieldsToAnalyze(1, fieldsAnalyzed);
                break;
            case 2:
                noSolutionsError2.setVisible(visible);
                showFieldsToAnalyze(2, fieldsAnalyzed);
                break;
            case 3:
                noSolutionsError3.setVisible(visible);
                showFieldsToAnalyze(3, fieldsAnalyzed);
                break;
            case 4:
                noSolutionsError4.setVisible(visible);
                showFieldsToAnalyze(4, fieldsAnalyzed);
        }
    }

    public void showSolution(byte[][] solution, int boardIndex) {
        lastSolutionFoundThreadIndex = boardIndex - 1;
        generateBoard(chessBoard5, 5);
        for (int i = 0; i < solution.length; i++) {
            for (int j = 0; j < solution[i].length; j++) {
                setField(5, i, j, solution[i][j]);
            }
        }
        solutionLabel.setText("Thread " + boardIndex + " found a solution:");
        solutionPane.setVisible(true);
    }

    private void generateBoard(GridPane grid, int boardIndex) {
        grid.getChildren().clear();
        grid.setMinSize(0, 0);
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                StackPane square = new StackPane();
                square.setId("field" + row + col);
                String color = ((row + col) % 2 == 0) ? "white" : colorBlack;
                String textColor = ((row + col) % 2 == 0) ? colorBlack : "white";
                square.setStyle("-fx-background-color: " + color + ";");
                Text text = new Text(rowLabels[col] + colLabels[row + (colLabels.length - size)]);
                text.setId("fieldDescription" + row + col);
                text.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.REGULAR, 15));
                text.setTextAlignment(TextAlignment.CENTER);
                text.setFill(Color.valueOf(textColor));
                GridPane.setHalignment(text, HPos.LEFT);
                GridPane.setValignment(text, VPos.TOP);
                grid.add(square, col, row);
                grid.add(text, col, row);
            }
        }
        if (boardIndex <= amountOfThreads) {
            grid.setStyle("-fx-border-color: #ADD8E6;\n"
                    + "-fx-border-insets: -5;\n"
                    + "-fx-border-width: 5;\n"
                    + "-fx-border-style: solid;\n");
        } else {
            grid.setStyle("-fx-border-color: none");
        }
    }

    public void updateSolutionCounter(int threadIndex, int size) {
        Label label = (threadIndex < 3) ? ((threadIndex < 2) ? solutionsLabel1 : solutionsLabel2) : ((threadIndex < 4) ? solutionsLabel3 : solutionsLabel4);
        label.setText(String.valueOf(size));

        totalSolutions++;
        solutionsLabel5.setText(String.valueOf(totalSolutions));
        solutionsLabel6.setText(String.valueOf(String.valueOf(totalSolutions / 2)));
    }

    public void incThreadsDone() {
        threadsDone++;
        if (threadsDone == amountOfThreads) {
            threadsDone = 0;
            timeNeeded = System.currentTimeMillis() - timeNeeded;
            String time = (timeNeeded > 1000) ? timeNeeded / 1000 + "," + timeNeeded % 100 + " s" : timeNeeded + " ms";
            timeNeededLabel.setText(time);
            stop();
        }
    }

    private void setBoard(int boardIndex, byte[][] board) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (getChessboard(boardIndex).lookup("#assignedValue" + i + j) != null) continue;
                resetField(boardIndex, i, j);
                if (board[i][j] == 0) continue;
                setField(boardIndex, i, j, board[i][j]);
            }
        }
    }
}
