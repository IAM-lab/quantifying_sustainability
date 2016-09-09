import beans.MetricsBean;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.*;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CSVFiller {

    static final Logger LOG = LoggerFactory.getLogger(CSVFiller.class);
    static List<GithubOutputBean> lstGithubOutputBean = new ArrayList<>();
    static List<MetricsBean> lstMetricsBean = new ArrayList<>();

    static class GithubOutputBean {
        Integer id;
        String name, s_N_Days;

        public void setId(Integer id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setS_N_Days(String s_N_Days) {
            this.s_N_Days = s_N_Days;
        }

        public String getName() {
            return name;
        }

        public String getS_N_Days() {
            return s_N_Days;
        }

        public Integer getId() {
            return id;
        }
    }

    public static GithubOutputBean findByName(String name) {
        GithubOutputBean result = null;
        for (GithubOutputBean bean : lstGithubOutputBean) {
            if (bean.getName().equalsIgnoreCase(name)) {
                result = bean;
                break;
            }
        }
        return result;
    }


    public static void writeMetricsToCSV(File outputFile) throws Exception {
        LOG.info("Writing metrics to file");
        ICsvBeanWriter beanWriter = null;
        try {
            beanWriter = new CsvBeanWriter(new FileWriter(outputFile),
                    CsvPreference.STANDARD_PREFERENCE);

            // the header elements are used to map the bean values to each column (names must match)
            final String[] header = new String[]{"Project_Name", "ClassName", "CBO", "DIT", "ILCOM", "LOC", "LOD_Class", "NOM",
                    "ID",
                    "Segment",
                    "S"};


            // write the header
            beanWriter.writeHeader(header);

            // write the beans
            for (final MetricsBean metrics : lstMetricsBean) {
                beanWriter.write(metrics, header);
            }

        } finally {
            if (beanWriter != null) {
                beanWriter.close();
            }
        }

    }

    public static void processRecords() {
        for (MetricsBean bean : lstMetricsBean) {
            GithubOutputBean result = findByName(bean.getProject_Name());
            if (result == null) {
                LOG.error("Couldn't find project in githuboutput CSV : " + bean.getProject_Name());
                continue;
            }
            bean.setID(result.getId());
            bean.setS(result.getS_N_Days());
            int sInt = Integer.parseInt(result.getS_N_Days());
            String segment = "1";
            if (sInt >= 2528) {
                segment = "19";
            } else if (sInt >= 2348) {
                segment = "18";
            } else if (sInt >= 2168) {
                segment = "17";
            } else if (sInt >= 1988) {
                segment = "16";
            } else if (sInt >= 1808) {
                segment = "15";
            } else if (sInt >= 1628) {
                segment = "14";
            } else if (sInt >= 1448) {
                segment = "13";
            } else if (sInt >= 1268) {
                segment = "12";
            } else if (sInt >= 1088) {
                segment = "11";
            } else if (sInt >= 908) {
                segment = "10";
            } else if (sInt >= 728) {
                segment = "9";
            } else if (sInt >= 548) {
                segment = "8";
            } else if (sInt >= 368) {
                segment = "7";
            } else if (sInt >= 278) {
                segment = "6";
            } else if (sInt >= 188) {
                segment = "5";
            } else if (sInt >= 98) {
                segment = "4";
            } else if (sInt >= 8) {
                segment = "3";
            } else if (sInt >= 1) {
                segment = "2";
            }
            bean.setSegment(segment);
        }
    }


    public static void readMetricsFile(File metricsFile) throws Exception {
        ICsvBeanReader beanReader = null;
        try {
            beanReader = new CsvBeanReader(new FileReader(metricsFile), CsvPreference.STANDARD_PREFERENCE);

            final String[] header = beanReader.getHeader(true);
            final CellProcessor[] processors = new CellProcessor[]{
                    new Optional(),
                    new Optional(),
                    new Optional(),
                    new Optional(),
                    new Optional(),
                    new Optional(),
                    new Optional(),
                    new Optional()
            };

            MetricsBean metricsBean;
            while ((metricsBean = beanReader.read(MetricsBean.class, header, processors)) != null) {
                LOG.info("Metrics Bean : " + ToStringBuilder.reflectionToString(metricsBean));
                lstMetricsBean.add(metricsBean);
            }

        } finally {
            if (beanReader != null) {
                beanReader.close();
            }
        }
    }

    public static void readGithubOutputFile(File githubOutputFile) throws Exception {
        CsvMapReader mapReader = null;
        try {
            mapReader = new CsvMapReader(new FileReader(githubOutputFile), CsvPreference.STANDARD_PREFERENCE);

            // the header columns are used as the keys to the Map
            final String[] header = mapReader.getHeader(true);
//            final CellProcessor[] processors = null;//getProcessors();

            Map<String, String> customerMap;
            while ((customerMap = mapReader.read(header)) != null) {
                Integer id = Integer.parseInt(customerMap.get("id"));
                String name = customerMap.get("name");
                String s_n_days = customerMap.get("S_N_Days");
                GithubOutputBean bean = new GithubOutputBean();
                bean.setId(id);
                bean.setName(name);
                bean.setS_N_Days(s_n_days);
                LOG.info("GithubOutput Bean : " + ToStringBuilder.reflectionToString(bean));
                lstGithubOutputBean.add(bean);
            }
        } finally {
            if (mapReader != null) {
                mapReader.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        File githubOutputFile = new File("GitHub_output.csv");
        File metricsFile = new File("CQ_Metrics_data.csv");
        File finalOutputFile = new File("final_output.csv");

        readGithubOutputFile(githubOutputFile);

        readMetricsFile(metricsFile);

        processRecords();

        writeMetricsToCSV(finalOutputFile);
        LOG.info("Program completed");
    }
}
