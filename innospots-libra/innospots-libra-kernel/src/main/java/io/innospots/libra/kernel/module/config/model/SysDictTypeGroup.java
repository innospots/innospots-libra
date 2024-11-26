package io.innospots.libra.kernel.module.config.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/26
 */
@Getter
@Setter
public class SysDictTypeGroup {

    private String type;
    private String typeName;

    private List<SysDictionary> dictionaries;
}
