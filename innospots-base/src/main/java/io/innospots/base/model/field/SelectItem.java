package io.innospots.base.model.field;

import io.innospots.base.json.annotation.I18n;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @date 2024/7/18
 */
@Getter
@Setter
public class SelectItem {

    @I18n
    private String label;

    private String value;

    private String tips;

    private String type;

    public SelectItem(String label, String value, String tips) {
        this.label = label;
        this.value = value;
        this.tips = tips;
    }

    public SelectItem(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public SelectItem(String label, String value, String tips, String type) {
        this.label = label;
        this.value = value;
        this.tips = tips;
        this.type = type;
    }
}
