package io.innospots.libra.kernel.module.config.model;

import io.innospots.base.json.annotation.I18n;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/26
 */
@Getter
@Setter
public class SysDictionaryType {

    private String type;

    @I18n
    private String typeName;
}
