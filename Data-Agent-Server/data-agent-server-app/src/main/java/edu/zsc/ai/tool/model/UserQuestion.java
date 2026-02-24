package edu.zsc.ai.tool.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Model representing a single user question with options and optional free-text input.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserQuestion {

    /**
     * The question to ask the user (e.g., "Which database do you want to connect to?").
     * Should be clear and specific to help users understand what information you need.
     */
    private String question;

    /**
     * List of options for user to choose from (minimum 2 options required).
     * Example: ["Database A", "Database B", "Database C"]
     * Provide concrete options based on available data (connections, databases, tables, etc.).
     */
    private List<String> options;

    /**
     * Optional hint for free-text input field (e.g., "Enter custom database name").
     * If null, no hint is shown. Use this to guide users when they provide custom input.
     */
    private String freeTextHint;

    /**
     * Whether user can select multiple options (default: true).
     * Set to false for single-choice questions where only one answer is valid.
     */
    private Boolean allowMultiSelect;

    /**
     * Whether user can provide custom text input (default: true).
     * Set to false to restrict to predefined options only, useful for strict validation scenarios.
     */
    private Boolean allowFreeText;
}
