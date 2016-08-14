package com.GitHUBProjectSelection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

public class GithubTest {

    @Test
    public void testZeroCommitRepo() throws Exception {
        //https://api.github.com/repos/jafl/language_game
        ObjectMapper mapper = new ObjectMapper();
        String json = "{" +
                "\"commits_url\": \"https://api.github.com/repos/jafl/language_game/commits{/sha}\"," +
                " \"html_url\": \"https://github.com/jafl/language_game\"," +
                "\"created_at\": \"2009-01-30T21:06:12Z\"" +
                "}";
        JsonNode node = mapper.readTree(json);
        GithubApi githubApi = new GithubApi(null, null, false);
        try {
            githubApi.processRepo(node);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testZeroCommitRepo2() throws Exception {
        //https://api.github.com/repos/nitkal/TTGlyphCaptcha
        ObjectMapper mapper = new ObjectMapper();
        String json = "{" +
                "\"commits_url\": \"https://api.github.com/repos/nitkal/TTGlyphCaptcha/commits{/sha}\"," +
                "\"html_url\": \"https://github.com/nitkal/TTGlyphCaptcha\"" +
                "}";
        JsonNode node = mapper.readTree(json);
        GithubApi githubApi = new GithubApi(null, null, false);
        try {
            githubApi.processRepo(node);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail(ex.getMessage());
        }
    }

}
