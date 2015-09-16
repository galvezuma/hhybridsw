package hhybridsw;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
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
public class HHybridSW {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        long initTime = System.currentTimeMillis();
        Options options = new Options(args);
        if (! options.isHelp()) {
            HashMap<String, LauncherTuple> algorithms = loadExecutions(options);
            List<Launcher> launchers = new ArrayList<>();
            for(String key: algorithms.keySet()) {
                LauncherTuple lt = algorithms.get(key);
                Launcher launcher = (Launcher) lt.launcherClass.newInstance();
                launcher.setKey(key);
                launcher.setLine(lt.commandLine);
                launchers.add(launcher);
                launcher.start();
            }
            for(Launcher l: launchers)
                l.join();
            showResults(launchers, System.out);
            System.out.printf("Total Execution time: %.3f sec.\n", (System.currentTimeMillis()-initTime)/1000.0);
        }
    }

    private static HashMap<String, LauncherTuple> loadExecutions(Options options) throws Exception  {
        HashMap<String, LauncherTuple> ret = new HashMap<>();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("./executions.xml"));
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("algorithm");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String use = eElement.getAttribute("use");
                if (use.toLowerCase().equals("yes")) {
                    String id = eElement.getAttribute("id");
                    String name = eElement.getAttribute("name");
                    String path = eElement.getAttribute("path");
                    String lc = eElement.getAttribute("launcherClass");
                    String executionLine = eElement.getTextContent();
                    // This adds the path to the program name (id) and removes blank lines and spaces
                    executionLine = executionLine.replaceFirst(name, path+name).replaceAll("\\n|\\r", "").replaceFirst(" *", "");
                    // This replaces every option with the particular program option and its value
                    executionLine = options.replaceLetters(executionLine);
                    LauncherTuple lt = new LauncherTuple();
                    lt.commandLine = executionLine;
                    lt.launcherClass = Class.forName(lc);
                    ret.put(id, lt);
                }
            }
        }
        return ret;
    }    

    private static void showResults(List<Launcher> launchers, PrintStream out) {
        Set<HitSet> result = new HashSet<>();
        launchers.stream().forEach((l) -> {
            try { HitSet.union(result, l.getHits()); } catch(Exception ex) { ex.printStackTrace(); }
        });
        result.stream().forEach((hs) -> {
            out.println(hs);
        });
        launchers.stream().forEach((l) -> {
            out.printf("%s \tGigaCUPS: %.3f \tExecution_time: %.3f sec.\n", l.getKey(), l.getGigaCUPS(), l.getTimeTaken());
        });
    }
    
    private static class LauncherTuple {
        public Class launcherClass;
        public String commandLine;
    }
    
}
