package com.codereview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class OpenAIService {

    @Value("${groq.api.key}")
    private String apiKey;

    private static final String GROQ_URL =
        "https://api.groq.com/openai/v1/chat/completions";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OpenAIService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String analyzeCode(String code, String language) {
        String prompt = buildPrompt(code, language);

        try {
            // Build Groq request JSON (same format as OpenAI)
            String requestJson = "{"
                + "\"model\":\"llama-3.3-70b-versatile\","
                + "\"temperature\":0.3,"
                + "\"max_tokens\":2000,"
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"You are an expert code reviewer. Always respond with valid JSON only. No markdown, no backticks, no extra text.\"},"
                + "{\"role\":\"user\",\"content\":" + objectMapper.writeValueAsString(prompt) + "}"
                + "]}";

            // Send HTTP request
            URL url = new URL(GROQ_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestJson.getBytes("UTF-8"));
            }

            // Read response
            int statusCode = conn.getResponseCode();
            InputStream is = statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String response = new String(is.readAllBytes(), "UTF-8");

            System.out.println("Groq HTTP status: " + statusCode);

            if (statusCode != 200) {
                System.err.println("Groq error body: " + response);
                return generateRuleBasedAnalysis(code, language);
            }

            // Parse OpenAI-compatible response
            Map<?, ?> responseMap = objectMapper.readValue(response, Map.class);
            List<?> choices = (List<?>) responseMap.get("choices");
            Map<?, ?> choice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) choice.get("message");
            String text = ((String) message.get("content")).trim();

            // Strip markdown if present
            if (text.startsWith("```json")) text = text.substring(7);
            if (text.startsWith("```")) text = text.substring(3);
            if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
            text = text.trim();

            // Validate JSON
            objectMapper.readValue(text, Map.class);
            System.out.println("Groq SUCCESS!");
            return text;

        } catch (Exception e) {
            System.err.println("Groq FAILED: " + e.getMessage());
            return generateRuleBasedAnalysis(code, language);
        }
    }

    private String buildPrompt(String code, String language) {
        return String.format("""
            Analyze the following %s code and provide a detailed code review.

            Return ONLY a valid JSON object with NO markdown, no backticks, no extra text.
            Use exactly this structure:
            {
              "overallRating": <number 1-10>,
              "complexityScore": <number 1-100>,
              "complexityLevel": "<LOW|MEDIUM|HIGH|VERY_HIGH>",
              "summary": "<brief overall summary>",
              "issues": [
                {
                  "issueType": "<BUG|CODE_SMELL|SECURITY|PERFORMANCE|STYLE|COMPLEXITY|BEST_PRACTICE>",
                  "severity": "<CRITICAL|HIGH|MEDIUM|LOW|INFO>",
                  "lineNumber": <line number as integer>,
                  "description": "<what the issue is>",
                  "suggestion": "<how to fix it>"
                }
              ],
              "positives": ["<what is good about the code>"],
              "improvements": ["<key improvements to make>"]
            }

            Be thorough — find ALL bugs, security issues, bad practices, and code smells.
            For each issue, specify the exact line number.

            CODE TO REVIEW (%s):
            %s
            """, language, language, code);
    }

    private String generateRuleBasedAnalysis(String code, String language) {
        List<Map<String, Object>> issues = new ArrayList<>();
        String[] lines = code.split("\n");
        int totalLines = lines.length;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            int lineNum = i + 1;

            if (line.length() > 120)
                issues.add(createIssue("STYLE", "LOW", lineNum,
                        "Line exceeds 120 characters", "Break into multiple lines"));

            if (trimmed.toUpperCase().contains("TODO") || trimmed.toUpperCase().contains("FIXME"))
                issues.add(createIssue("CODE_SMELL", "LOW", lineNum,
                        "Unresolved TODO/FIXME comment", "Address before production"));

            if ("java".equalsIgnoreCase(language)) {
                if (trimmed.contains("System.out.print"))
                    issues.add(createIssue("BEST_PRACTICE", "LOW", lineNum,
                            "System.out.print used instead of logger", "Use SLF4J/Logback"));

                if (trimmed.matches(".*\"[^\"]*\"\\s*==\\s*.*") || trimmed.matches(".*==\\s*\"[^\"]*\".*"))
                    issues.add(createIssue("BUG", "HIGH", lineNum,
                            "String compared with == instead of .equals()", "Use .equals() for string comparison"));

                if (trimmed.toLowerCase().contains("password") && trimmed.contains("\"") && !trimmed.startsWith("//"))
                    issues.add(createIssue("SECURITY", "CRITICAL", lineNum,
                            "Hardcoded password detected", "Use environment variables or secrets manager"));

                if (trimmed.contains(".get()") && !trimmed.contains("isPresent") && !trimmed.contains("orElse"))
                    issues.add(createIssue("BUG", "HIGH", lineNum,
                            "Optional.get() without isPresent() check", "Use orElseThrow() or orElse()"));

                if (i > 0 && trimmed.contains(".save(") && lines[i - 1].trim().contains(".save("))
                    issues.add(createIssue("BUG", "MEDIUM", lineNum,
                            "Duplicate repository.save() call", "Remove the redundant save()"));
            }

            if ("javascript".equalsIgnoreCase(language) || "js".equalsIgnoreCase(language)) {
                if (trimmed.contains("console.log("))
                    issues.add(createIssue("BEST_PRACTICE", "INFO", lineNum,
                            "console.log() found", "Remove before production"));

                if (trimmed.contains("return ") && !trimmed.contains("await") && code.contains("fetch("))
                    issues.add(createIssue("BUG", "HIGH", lineNum,
                            "Possible async issue — returning value without await", "Use async/await"));
            }

            if ("python".equalsIgnoreCase(language)) {
                if (trimmed.contains("print("))
                    issues.add(createIssue("BEST_PRACTICE", "INFO", lineNum,
                            "print() found", "Use logging module in production"));
                if (trimmed.startsWith("except:") || trimmed.startsWith("except Exception:"))
                    issues.add(createIssue("CODE_SMELL", "MEDIUM", lineNum,
                            "Broad exception catch", "Catch specific exceptions"));
            }
        }

        int complexity = calculateComplexity(code, language);
        String complexityLevel = complexity < 20 ? "LOW" : complexity < 40 ? "MEDIUM" : complexity < 70 ? "HIGH" : "VERY_HIGH";
        double rating = Math.round(Math.max(1, Math.min(10, 10 - (issues.size() * 0.7) - (complexity / 25.0))) * 10.0) / 10.0;

        Map<String, Object> result = new HashMap<>();
        result.put("overallRating", rating);
        result.put("complexityScore", complexity);
        result.put("complexityLevel", complexityLevel);
        result.put("summary", String.format("Analyzed %d lines of %s code. Found %d potential issue(s).", totalLines, language, issues.size()));
        result.put("issues", issues);
        result.put("positives", List.of("Code structure is readable", "Logic flow is clear"));
        result.put("improvements", List.of("Add unit tests", "Add documentation", "Consider input validation"));

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, Object> createIssue(String type, String severity, int line, String desc, String suggestion) {
        Map<String, Object> issue = new HashMap<>();
        issue.put("issueType", type);
        issue.put("severity", severity);
        issue.put("lineNumber", line);
        issue.put("description", desc);
        issue.put("suggestion", suggestion);
        return issue;
    }

    private int calculateComplexity(String code, String language) {
        int complexity = 5;
        String[] complexKeywords = {"if", "else", "for", "while", "switch", "case", "catch", "&&", "\\|\\|", "try"};
        for (String keyword : complexKeywords) {
            complexity += (code.split(keyword, -1).length - 1) * 3;
        }
        int ternaryCount = 0;
        for (char c : code.toCharArray()) if (c == '?') ternaryCount++;
        complexity += ternaryCount * 3;
        return Math.min(100, complexity);
    }
}