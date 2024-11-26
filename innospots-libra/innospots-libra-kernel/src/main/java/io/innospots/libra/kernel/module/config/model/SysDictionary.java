package io.innospots.libra.kernel.module.config.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.innospots.base.json.annotation.I18n;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/26
 */
@Getter
@Setter
public class SysDictionary {

    private String dictId;

    @I18n
    private String name;

    private String value;

    private String type;

    @I18n
    private String typeName;

    private Boolean status;
}
