package edu.zsc.ai.enums;

import lombok.Getter;

/**
 * Delete status enumeration
 *
 * @author zgq
 */
@Getter
public enum DeleteStatusEnum {

    /**
     * Not deleted
     */
    NORMAL(0, "Normal"),

    /**
     * Deleted
     */
    DELETED(1, "Deleted");

    private final Integer value;
    private final String description;

    DeleteStatusEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * Get enum by value
     *
     * @param value status value
     * @return status enum
     */
    public static DeleteStatusEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (DeleteStatusEnum status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}