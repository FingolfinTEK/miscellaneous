package com.fingy.micromedex.web.dto;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class InteractionResult {

    private static final String EFFECT_DELIMITER = "result in";

    private final String drug;
    private final String drug2;
    private final String severity;
    private final String documentation;
    private final String summary;
    private final String effect;
    private final String url;

    public InteractionResult(final String drugs, final String severity, final String documentation, final String summary, final String url) {
        this.drug = getDrugFromDrugsString(drugs);
        this.drug2 = getDrug2FromDrugsString(drugs);
        this.severity = severity;
        this.documentation = documentation;
        this.summary = summary;
        this.url = encodeUrl(url);
        this.effect = extractEffectFromSummary(summary);
    }

    private String getDrugFromDrugsString(final String drugs) {
        String[] drugOneAndTwo = drugs.split("--");
        return drugOneAndTwo.length == 2 ? drugOneAndTwo[0].trim() : drugs;
    }

    private String getDrug2FromDrugsString(final String drugs) {
        String[] drugOneAndTwo = drugs.split("--");
        return drugOneAndTwo.length == 2 ? drugOneAndTwo[1].trim() : null;
    }

    private String encodeUrl(final String url) {
        try {
            return URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException ignored) {
        }

        return null;
    }

    private String extractEffectFromSummary(final String summary) {
        boolean canExtract = summary.contains(EFFECT_DELIMITER);
        return canExtract ? StringUtils.removeEnd(summary.split(EFFECT_DELIMITER)[1], ".") : null;
    }

    public String getDrug() {
        return drug;
    }

    public String getDrug2() {
        return drug2;
    }

    public String getSeverity() {
        return severity;
    }

    public String getDocumentation() {
        return documentation;
    }

    public String getSummary() {
        return summary;
    }

    public String getEffect() {
        return effect;
    }

    public String getUrl() {
        return url;
    }

}
