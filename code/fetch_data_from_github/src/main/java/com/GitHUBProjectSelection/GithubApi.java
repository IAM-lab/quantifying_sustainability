package com.GitHUBProjectSelection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GithubApi {

    static final Logger LOG = LoggerFactory.getLogger(GithubApi.class);

    private static final String host = "https://api.github.com";
    private static final String searchPath = "/search/repositories";

    private String username, password;
    private WebClient webClient;

    private int minSleep = 1, maxSleep = 5;     
    private ObjectMapper mapper = new ObjectMapper();
    private List<Map> results;
    private List<Map> filteredResults;

    public GithubApi(String username, String password, boolean proxy) {
        this.username = username;
        this.password = password;
        webClient = new WebClient(BrowserVersion.FIREFOX_45);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setTimeout(120 * 1000);
        if (proxy) {
            webClient.getOptions().setProxyConfig(new ProxyConfig("127.0.0.1", 8888));      //Fiddler / Charles default proxy settings
        }
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            String base64encodedUsernameAndPassword = base64Encode(username + ":" + password);
            webClient.addRequestHeader("Authorization", "Basic " + base64encodedUsernameAndPassword);
        }
    }

    private static String base64Encode(String stringToEncode) {
        return DatatypeConverter.printBase64Binary(stringToEncode.getBytes());
    }

    public List<Map<String, Object>> searchRepos(String q, String sort, String order) throws Exception {
        results = new ArrayList<>();
        filteredResults = new ArrayList<>();

        WebRequest webRequest = new WebRequest(new URL(host + searchPath), HttpMethod.GET);
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        webRequest.setRequestParameters(nameValuePairs);
        nameValuePairs.add(new NameValuePair("q", q));
        nameValuePairs.add(new NameValuePair("sort", sort));
        nameValuePairs.add(new NameValuePair("order", order));
        nameValuePairs.add(new NameValuePair("per_page", "100"));
        int currentPage = 1;
        LOG.info("Opening page " + currentPage + "  " + q);
        WebResponse webResponse = webClient.loadWebResponse(webRequest);

        do {
            String response = webResponse.getContentAsString();
            JsonNode root = mapper.readTree(response);
            for (JsonNode repo : root.get("items")) {
                Thread.sleep(randomSleepSeconds(minSleep, maxSleep));      

                processRepo(repo);
            }
            String nextPageUrl = nextPageUrl(webResponse);
            if (nextPageUrl == null) {
                break;
            } else {
//                break;        
                LOG.info("Opening page " + ++currentPage + " " + nextPageUrl);
                webResponse = webClient.loadWebResponse(new WebRequest(new URL(nextPageUrl)));
            }
        } while (true);

        

       
        LOG.info("Writing filtered results to CSV : " + filteredResults.size());
        writeToCSV(filteredResults, "results.csv");

        return null;
    }

    public void processRepo(JsonNode repo) throws Exception {
        String commitsUrl = repo.get("commits_url").asText().replace("{/sha}", "");
        String repoHtmlurl = repo.get("html_url").asText().trim();
        LOG.info("\nRepo : " + repoHtmlurl);

        String first_commit = getFirstCommit(repoHtmlurl, commitsUrl);
        if (first_commit == null) {
            LOG.info("Repository is blank (0 commits ) : " + repoHtmlurl);
            return;
        }
        ((ObjectNode) repo).put("first_commit", first_commit);
        LOG.info("First Commit : " + first_commit);

        webClient.close();

        String last_commit = getLastCommit(commitsUrl);
        ((ObjectNode) repo).put("last_commit", last_commit);
        LOG.info("Last Commit : " + last_commit);

        DateTime created_atDT = new DateTime(repo.get("created_at").asText());
        DateTime last_commitDT = new DateTime(last_commit);
        Duration duration = new Duration(created_atDT, last_commitDT);
        ((ObjectNode) repo).put("SM", duration.getStandardMinutes());
        LOG.info("Created - LastCommit : " + duration.getStandardMinutes());

        DateTime first_commitDT = new DateTime(first_commit);
        duration = new Duration(first_commitDT, last_commitDT);
        ((ObjectNode) repo).put("SS", duration.getStandardMinutes());
        LOG.info("FirstCommit - LastCommit : " + duration.getStandardMinutes());

        results.add(mapper.convertValue(repo, Map.class));

       
        if (first_commitDT.getYear() > 2008 && first_commitDT.getYear() < 2010) {
            filteredResults.add(mapper.convertValue(repo, Map.class));
        }
    }

    private void writeToCSV(List<Map> lstMap, String fileName) throws Exception {
        ICsvMapWriter mapWriter = null;
        try {
            File file = new File(fileName);
            boolean fileExists = file.exists();

            mapWriter = new CsvMapWriter(new FileWriter(file, true),
                    CsvPreference.STANDARD_PREFERENCE);

           
            for (Map m : lstMap) {
                LOG.info("Writing Repo To CSV : " + ToStringBuilder.reflectionToString(m, ToStringStyle.JSON_STYLE));
                Map ownerMap = (Map) m.get("owner");
                Set<Map.Entry> entrySet = ownerMap.entrySet();
                for (Map.Entry entry : entrySet) {
                    m.put("owner." + entry.getKey(), entry.getValue());
                }
                m.remove("owner");
            }

            String[] headers = (String[]) lstMap.get(0).keySet().toArray(new String[]{});
            
            if (!fileExists) {
                mapWriter.writeHeader(headers);
            }
            for (Map m : lstMap) {
                mapWriter.write(m, headers);
            }
        } finally {
            if (mapWriter != null) {
                mapWriter.close();
            }
        }
    }


    private String getLastCommit(String url) throws Exception {
        return getCommitPage(url, 1, false);
    }


    /**
     * https://developer.github.com/v3/repos/commits/
     *
     * @param commitsUrl
     * @param pageNo
     * @return
     * @throws Exception
     */
    private String getCommitPage(String commitsUrl, int pageNo, boolean lastRecord) throws Exception {
        WebRequest webRequest = new WebRequest(new URL(commitsUrl));
        List<NameValuePair> params = new ArrayList<>();
        params.add(new NameValuePair("page", pageNo + ""));
        webRequest.setRequestParameters(params);
        WebResponse webResponse = webClient.getPage(webRequest).getWebResponse();
        String response = webResponse.getContentAsString();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            if (lastRecord) {
                return root.get(root.size() - 1).get("commit").get("author").get("date").asText();
            } else {
                return root.get(0).get("commit").get("author").get("date").asText();
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return null;
    }


    private String getFirstCommit(String repoUrl, String commitsUrl) throws Exception {
        WebRequest webRequest = new WebRequest(new URL(repoUrl), HttpMethod.GET);
        HtmlPage page = webClient.getPage(webRequest);
        HtmlListItem listItem = page.querySelector(".numbers-summary .commits");
        if (listItem == null) {
            return null;
        }
        int totalCommits = Integer.parseInt(listItem.asText().replace(",", "").replace("commits", "").replace("commit", "").trim());
        int per_page_commits = 30;
        int last_page = totalCommits / per_page_commits;
        if (totalCommits % per_page_commits > 0) {
            last_page++;
        }
        LOG.info("Total Commits : " + totalCommits);
        LOG.info("Last Page of commits : " + last_page);        //commits are returned in reverse chronological order, that means the last page of commits has the first commit.
        return getCommitPage(commitsUrl, last_page, true);
    }

    /**
     * https://developer.github.com/guides/traversing-with-pagination/
     *
     * @param webResponse
     * @return
     */
    private String nextPageUrl(WebResponse webResponse) {
        String hValue = webResponse.getResponseHeaderValue("Link");
        Pattern pattern = Pattern.compile("<(.*?)>; rel=\"next\"");
        if (hValue == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(hValue);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static int randomSleepSeconds(int minSleep, int maxSleep) {
        Random rn = new Random();
        int result = rn.nextInt(maxSleep - minSleep + 1) + minSleep;
        LOG.info("Sleeping for " + result + " seconds");
        return result * 1000;       //milliseconds
    }
}
