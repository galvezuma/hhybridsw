package hhetersw;

import hhetersw.utils.AlgorithmTuple;
import hhetersw.utils.FastaSplitter;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author galvez
 */
public class HHeterSW {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        long initTime = System.currentTimeMillis();
        Options options = new Options(args);
        if (! options.isHelp()) {
            HashMap<String, AlgorithmTuple> algorithms = loadExecutions(options);
            List<Launcher> launchers = new ArrayList<>();
            // Check if the split option is activated
            if (options.getSplitDatabase() != -1){
                for(int rounds = options.getSplitDatabase() ; rounds > 0; rounds--){
                    FastaSplitter.process(options.getDatabase(), algorithms.values()); // Split with percentages
                    prepareExecuteAndWaitLaunchers(algorithms, launchers); // Execute and obtain times
                    // Calculate new percentages
                    // First, we calculate the speed in pct/second
                    double sumSpeeds = 0.0;
                    double[] speeds = new double[launchers.size()];
                    Object[] algs = algorithms.values().toArray();
                    for(int i=0; i<algs.length; i++){
                        speeds[i] = ((AlgorithmTuple)algs[i]).splitPercentage / (launchers.get(i).getTimeTaken()+0.1);
                        sumSpeeds += speeds[i];
                    }
                    // The best time should be 100(speed_1+speed_2+speed_3...)
                    double bestTime = 100/sumSpeeds;
                    // Secondly (finally) we calculate new percentages
                    System.out.println("New best percentages:");
                    for(int i=0; i<algs.length; i++){
                        ((AlgorithmTuple)algs[i]).splitPercentage = speeds[i]*bestTime;
                        System.out.printf(": %.3f ", ((AlgorithmTuple)algs[i]).splitPercentage);
                    }
                    System.out.println("\n-----------");
                }
            } else { // No split option. This is the standard situation
                // Load and execute the launchers and show the results
                prepareExecuteAndWaitLaunchers(algorithms, launchers);
                showResults(launchers, System.out);
            }
            System.out.printf("Total Execution time: %.3f sec.\n", (System.currentTimeMillis()-initTime)/1000.0);
        }
    }

    private static void prepareExecuteAndWaitLaunchers(HashMap<String, AlgorithmTuple> algorithms, List<Launcher> launchers) throws Exception {
        for(String key: algorithms.keySet()) {
            AlgorithmTuple lt = algorithms.get(key);
            Launcher launcher = (Launcher) lt.launcherClass.newInstance();
            launcher.setKey(key);
            launcher.setLine(lt.commandLine);
            launchers.add(launcher);
            launcher.start();
        }
        for(Launcher l: launchers)
            l.join();
    }

    private static HashMap<String, AlgorithmTuple> loadExecutions(Options options) throws Exception  {
        Double totalPct = 0.0;
        HashMap<String, AlgorithmTuple> ret = new LinkedHashMap<>(); // This hash maintains the insertionn order
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("./executions.xml"));
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("algorithm");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String use = eElement.getAttribute("use");
                if (use == null || use.toLowerCase().equals("yes")) {
                    String id = eElement.getAttribute("id");
                    String name = eElement.getAttribute("name");
                    String path = eElement.getAttribute("path");
                    String lc = eElement.getAttribute("launcherClass");
                    String pct = eElement.getAttribute("pct");
                    String executionLine = eElement.getElementsByTagName("execution").item(0).getTextContent();
                    // This adds the path to the program name (id) and removes blank lines and spaces
                    executionLine = executionLine.replaceFirst(name, path+name).replaceAll("\\n|\\r", "").replaceFirst(" *", "");
                    // This replaces every option with the particular program option and its value
                    executionLine = options.replaceLetters(executionLine);
                    // These load the command lines to create and destroy the database required by the algorithm
                    String createLine = ((Element)eElement.getElementsByTagName("database").item(0)).getElementsByTagName("create").item(0).getTextContent();
                    createLine = createLine.replaceAll("\\n|\\r", "").replaceFirst(" *", "");
                    createLine = options.replaceLetters(createLine);
                    String destroyLine = ((Element)eElement.getElementsByTagName("database").item(0)).getElementsByTagName("destroy").item(0).getTextContent();
                    destroyLine = destroyLine.replaceAll("\\n|\\r", "").replaceFirst(" *", "");
                    destroyLine = options.replaceLetters(destroyLine);
                    // System.out.println(createLine);
                    // System.out.println(destroyLine);
                    // System.out.println(executionLine);
                    // -----
                    AlgorithmTuple lt = new AlgorithmTuple();
                    lt.commandLine = executionLine;
                    lt.databaseCreateCommandLine = createLine;
                    lt.databaseDestroyCommandLine = destroyLine;
                    lt.launcherClass = Class.forName(lc);
                    lt.splitPercentage = -1;
                    ret.put(id, lt);
                    // Check if the split option is activated
                    if (options.getSplitDatabase() != -1){
                        if (pct == null) throw new Exception(id + " algorithm must contain a percentage");
                        lt.splitPercentage = Double.parseDouble(pct);
                        totalPct += lt.splitPercentage;
                    }
                }
            }
        }
        // Check if the split option percentages sum 100%
        if (options.getSplitDatabase() != -1 && totalPct != 100.0)
            throw new Exception("The sum of percentages should be 100%: "+totalPct+"%");
        return ret;
    }    

    private static void showResults(List<Launcher> launchers, PrintStream out) {
        out.println("Results:\n--------");
        Set<HitSet> result = new HashSet<>();
        launchers.stream().forEach((l) -> {
            try { HitSet.union(result, l.getHits()); } catch(Exception ex) { ex.printStackTrace(); }
        });
        result.stream().forEach((hs) -> {
            out.println(hs);
        });
        double totalGigaCUPS = 0.0, totalTimeAlgorithm = 0.0;
        for (Launcher l: launchers) {
            out.printf("%s \tGigaCUPS: %.3f \tAlgorithm time: %.3f \tExecution_time: %.3f sec.\n", l.getKey(), l.getGigaCUPS(), l.getTimeAlgorithm(), l.getTimeTaken());
            double tempGigaCUP = totalGigaCUPS * totalTimeAlgorithm + l.getGigaCUPS() * l.getTimeAlgorithm();
            totalTimeAlgorithm = Double.max(totalTimeAlgorithm, l.getTimeAlgorithm());
            totalGigaCUPS = tempGigaCUP / totalTimeAlgorithm;
        }
        out.printf("Final GigaCUPS: %.3f \t Final Algorithm time: %.3f\n", totalGigaCUPS, totalTimeAlgorithm);
    }
    
}
