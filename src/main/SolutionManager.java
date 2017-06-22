package main;

import java.util.HashSet;

public class SolutionManager {

    private HashSet<String> solutions;

    public SolutionManager(){
        solutions = new HashSet<>();
        TourRecorder.instance.startup();
        TourRecorder.instance.init();
    }

    public boolean containsSolution(String solution){
        boolean containsSolution = solutions.contains(solution);
        if (!containsSolution) addSolution(solution);
        return containsSolution;
    }

    public void addSolution(String solution){
        if (Configuration.instance.recordRoutes) TourRecorder.instance.insert(solution);
        solutions.add(solution);
    }
}
