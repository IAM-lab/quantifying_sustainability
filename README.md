# Quantifying Sustainability

## Aseel Aldabjan, Robert Haines and Caroline Jay

*Materials related to our Quantifying Sustainability work*

### Mining projects from GitHub

Projects were selected for analysis on 8 May 2016.

The projects we included in our initial dataset match the following criteria:
* They were added to GitHub during 2009.
* They were written in Java.

The following call was used to mine these projects using the GitHub API:

```
https://api.github.com/search/repositories?q=language:Java+created:"2009-01-01 .. 2009-12-31"&sort=updated&per_page=20&order=desc
```
