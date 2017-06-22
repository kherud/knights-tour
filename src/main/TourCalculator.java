package main;

import javafx.application.Platform;
import javafx.util.Pair;

import javax.security.auth.login.*;
import java.util.ArrayList;

public class TourCalculator implements Runnable {

    private byte[][] board;

    private static final Object syncObject = new Object();
    private int size;
    private ArrayList<Pair<Integer, Integer>> fieldsToAnalyse;
    private int delay = 500;
    private int chessBoardIndex;
    private boolean running = false;
    private boolean animationEnabled;
    private boolean showSolutions;
    private int amountOfSolutions = 0;
    private Pair<Integer, Integer> currentField;
    private BoardGenerator boardGenerator;
    private SolutionManager solutionManager;

    public TourCalculator(int size) {
        board = new byte[size][size];
        this.size = size;
        fieldsToAnalyse = new ArrayList<>();
    }

    public TourCalculator(int size, int boardIndex, boolean animationDisabled, boolean showSolutions, BoardGenerator boardGenerator, SolutionManager solutionManager) {
        this(size);
        chessBoardIndex = boardIndex;
        this.boardGenerator = boardGenerator;
        animationEnabled = !animationDisabled;
        this.showSolutions = showSolutions;
        this.solutionManager = solutionManager;
    }

    public void run(){
        new Thread(this::calculateSolutions).start();
    }

    public void calculateSolutions() {
        running = true;
        for (Pair<Integer, Integer> field : fieldsToAnalyse){
            currentField = field;
            doNextField(field.getKey(), field.getValue(), (byte) 1);
        }
        if (amountOfSolutions == 0) {
            Platform.runLater(() -> boardGenerator.showNoSolutionsError(chessBoardIndex, fieldsToAnalyse, true));
        }
        Platform.runLater(() -> boardGenerator.incThreadsDone());
    }

    public void solutionSeen(){
        synchronized (syncObject){
            syncObject.notifyAll();
        }
    }

    private void doNextField(int row, int col, byte currentStep) {
        if (!running) return;
        board[row][col] = currentStep;
        if (animationEnabled){
            Platform.runLater(() -> boardGenerator.setField(chessBoardIndex, row, col, currentStep));
            sleep();
        }
        for (int i = -1; i < 2; i += 2) {
            for (int j = -1; j < 2; j += 2) {
                int nextRow = row + i * 2;
                int nextCol = col + j;
                doMoveIfValid(nextRow, nextCol, currentStep);
                nextRow = row + i;
                nextCol = col + j * 2;
                doMoveIfValid(nextRow, nextCol, currentStep);
            }
        }
        board[row][col] = 0;
        if (animationEnabled) Platform.runLater(() -> boardGenerator.resetField(chessBoardIndex, row, col));
    }

    private void checkIfSolved(int nextRow, int nextCol, byte currentStep) {
        if (currentStep == size * size && nextRow == currentField.getKey() && nextCol == currentField.getValue()) {
            if (!solutionManager.containsSolution(normalizeTour())) {
                handleSolution();
            }
        }
    }

    private void handleSolution(){
        amountOfSolutions++;
        Platform.runLater(() -> boardGenerator.updateSolutionCounter(chessBoardIndex, amountOfSolutions));
        if (!showSolutions) return;
        synchronized (syncObject) {
            Platform.runLater(() -> boardGenerator.showSolution(board, chessBoardIndex));
            try {
                syncObject.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sleep() {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doMoveIfValid(int nextRow, int nextCol, byte currentStep) {
        checkIfSolved(nextRow, nextCol, currentStep);
        if (inBounds(nextRow) && inBounds(nextCol) && fieldNotVisited(nextRow, nextCol)) {
            doNextField(nextRow, nextCol, (byte) (currentStep + 1));
        }
    }

    private boolean fieldNotVisited(int row, int col) {
        return board[row][col] == 0;
    }

    private boolean inBounds(int index) {
        return index < size && index >= 0;
    }

    private void printBoard() {
        //System.out.print("\033[H\033[2J");
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] < 10) System.out.print(" ");
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public void assignFieldsToAnalyze(int amountOfThreads){
        if (size == 0 || chessBoardIndex == 0 ) return;
        int amountOfFields = size * size;
        int amountOfFieldsToAnalyse = amountOfFields / amountOfThreads;
        int firstField = (chessBoardIndex - 1) * amountOfFieldsToAnalyse;
        for (int i = 0; i < amountOfFieldsToAnalyse; i++){
            int row = (firstField + i) / size;
            int col = (firstField + i) % size;
            fieldsToAnalyse.add(new Pair<>(row, col));
        }
        if (amountOfThreads == chessBoardIndex && amountOfFields % amountOfThreads != 0){
            fieldsToAnalyse.add(new Pair<>(size - 1, size - 1));
        }
    }

    public void showFieldsToAnalyze(){
        boardGenerator.showFieldsToAnalyze(chessBoardIndex, fieldsToAnalyse);
    }

    public void setAnimationDisabled(boolean animationDisabled) {
        this.animationEnabled = !animationDisabled;
    }

    public void setShowSolutions(boolean showSolutions) {
        this.showSolutions = showSolutions;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setIsRunning(boolean isRunning){
        running = isRunning;
    }

    private String normalizeTour(){
        int startValue = board[0][0];
        StringBuilder tour = new StringBuilder();
        int amountOfFields = size  * size;
        for (int i = 0; i < amountOfFields; i++){
            byte nextInOrder = (byte) ((startValue + i) % (amountOfFields + 1));
            if (startValue + i > amountOfFields) nextInOrder++;
            tour.append(getFieldForOrder(nextInOrder));
            tour.append(", ");
        }
        return tour.toString();
    }

    private String getFieldForOrder(byte place){
        for (int i = 0; i < size; i++){
            for (int j = 0; j < size; j++){
                if (board[i][j] == place){
                    String rowName = Configuration.instance.rowLabels[j];
                    String colName = Configuration.instance.colLabels[i - + (Configuration.instance.colLabels.length - size)];
                    return rowName + colName;
                }
            }
        }
        return " ";
    }

    public byte[][] getBoard(){
        return board;
    }

    public int getChessBoardIndex(){
        return chessBoardIndex;
    }
}
