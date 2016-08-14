package com.GitHUBProjectSelection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    static final Logger LOG = LoggerFactory.getLogger(GithubApi.class);

    static String username = "";        
    static String password = "";        

    public static void main(String[] args) {
        try {
            GithubApi github = new GithubApi(username, password, false);        
            String qArr[] = new String[]{
            		  "language:Java created:\"2009-01-01 .. 2009-01-31\"",
                      "language:Java created:\"2009-02-01 .. 2009-02-28\"",
                      "language:Java created:\"2009-03-01 .. 2009-03-31\"",
                      "language:Java created:\"2009-04-01 .. 2009-04-30\"",
                      "language:Java created:\"2009-05-01 .. 2009-05-31\"",
                      "language:Java created:\"2009-06-01 .. 2009-06-30\"",
                      "language:Java created:\"2009-07-01 .. 2009-07-31\"",
                      "language:Java created:\"2009-08-01 .. 2009-08-31\"",
                      "language:Java created:\"2009-09-01 .. 2009-09-30\"",
                      "language:Java created:\"2009-10-01 .. 2009-10-31\"",
                      "language:Java created:\"2009-11-01 .. 2009-11-30\"",
                      "language:Java created:\"2009-12-01 .. 2009-12-31\""
              };
                        for (String q : qArr) {
                github.searchRepos(q, "updated", "desc");
            }
            LOG.info("Result is Completed");
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
}
