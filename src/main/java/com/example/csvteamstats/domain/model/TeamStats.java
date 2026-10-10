package com.example.csvteamstats.domain.model;

/** Framework-independent TEAM-STATS business data. */
public class TeamStats {
    private String sourceEventId;
    private String matchId;
    private String period;
    private String category;
    private String metric;
    private String homeTeam;
    private String homeValue;
    private String awayTeam;
    private String awayValue;
    private String sourceUrl;

    public String getSourceEventId() { return sourceEventId; }
    public void setSourceEventId(String value) { sourceEventId = value; }
    public String getMatchId() { return matchId; }
    public void setMatchId(String value) { matchId = value; }
    public String getPeriod() { return period; }
    public void setPeriod(String value) { period = value; }
    public String getCategory() { return category; }
    public void setCategory(String value) { category = value; }
    public String getMetric() { return metric; }
    public void setMetric(String value) { metric = value; }
    public String getHomeTeam() { return homeTeam; }
    public void setHomeTeam(String value) { homeTeam = value; }
    public String getHomeValue() { return homeValue; }
    public void setHomeValue(String value) { homeValue = value; }
    public String getAwayTeam() { return awayTeam; }
    public void setAwayTeam(String value) { awayTeam = value; }
    public String getAwayValue() { return awayValue; }
    public void setAwayValue(String value) { awayValue = value; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String value) { sourceUrl = value; }
}
