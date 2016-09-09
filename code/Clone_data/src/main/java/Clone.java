import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Clone {

    static final Logger LOG = LoggerFactory.getLogger(Clone.class);
    static int MAX_REPOS_TO_CLONE = 12;
    static int MAX_CSVS = 19;

    public static void main(String[] args) throws Exception {
        String name = "GH_groups/s{n}-Table 1.csv";
        for (int j = 1; j <= MAX_CSVS; j++) {
            LOG.info("Processing group " + j);
            String csvName = name.replace("{n}", j + "");
            processCSV(j, csvName);
        }
    }

    public static void processCSV(final Integer groupId, final String CSV_FILENAME) throws Exception {
        ICsvMapReader mapReader = null;
        try {
            mapReader = new CsvMapReader(new FileReader(CSV_FILENAME), CsvPreference.STANDARD_PREFERENCE);

            final String[] header = mapReader.getHeader(true);

            //Read the CSV into a list of clone urls
            Map<String, String> customerMap;
            List<String> cloneUrls = new ArrayList<>();
            while ((customerMap = mapReader.read(header)) != null) {
                String gitUrl = customerMap.get("clone_url");
                cloneUrls.add(gitUrl);
            }

            //Take random entries from the list of cloneUrls unless MAX_REPOS_TO_CLONE are cloned
            int count = 0;
            while (count < MAX_REPOS_TO_CLONE) {
                int random = randomInt(0, cloneUrls.size() - 1);
                if (cloneRepo(groupId, cloneUrls.get(random))) {
                    count++;
                }
            }
        } finally {
            if (mapReader != null) {
                mapReader.close();
            }
        }
    }


    public static int randomInt(int min, int max) {
        Random rn = new Random();
        int result = rn.nextInt(max - min + 1) + min;
        return result;
    }

    public static boolean cloneRepo(Integer groupId, String cloneUrl) throws IOException {
        String dirName = groupId + "";

        LOG.info("Cloning Repo : " + cloneUrl);
        DefaultExecutor executor = new DefaultExecutor();

        try {
            File workingDir = new File(dirName);
            if (!workingDir.exists() || !workingDir.isDirectory()) {
                workingDir.mkdir();
            }
            CommandLine cmdLine = CommandLine.parse("git clone " + cloneUrl);

            executor.setWorkingDirectory(workingDir);
            int exitValue = executor.execute(cmdLine);
            if (exitValue == 0) {
                LOG.info("Repo Cloned Successfully : " + cloneUrl);
                return true;
            } else {
                LOG.error("Cloning Repo Failed : " + cloneUrl);
            }
        } catch (ExecuteException ex) {
            if (ex.getMessage().contains("128")) {      //project directory already exists
                LOG.info("Repo already cloned");
            } else {
                throw ex;
            }
        }
        return false;
    }
}
