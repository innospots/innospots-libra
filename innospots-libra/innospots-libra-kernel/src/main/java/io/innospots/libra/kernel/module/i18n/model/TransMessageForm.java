package io.innospots.libra.kernel.module.i18n.model;

import cn.hutool.core.util.ArrayUtil;
import io.innospots.base.exception.ValidatorException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/12
 */
@Getter
@Setter
@Schema(title = "create new trans message form data")
public class TransMessageForm {

    @Schema(title = "the name of application")
    private String app;

    @Schema(title = "application module")
    private String module;

    @Schema(title = "dictionary code")
    @NotBlank
    @Size(max = 64)
    private String code;

    @Schema(title = "key is locale, value is trans message")
    private Map<String, String> messages;


    public String fillDictCode() {
        if (StringUtils.isEmpty(app) || StringUtils.isEmpty(module)) {
            String[] dictCodes = code.split("\\.");
            if (dictCodes.length < 3) {
                throw ValidatorException.buildMissingException(this.getClass(), "app or module is empty, the code must include app and module prefix, for example: app.module.code");
            }
            app = dictCodes[0];
            module = dictCodes[1];
        } else {
            if (code.startsWith(module)) {
                code = app + "." + code;
            } else if (!code.startsWith(app + "." + module)) {
                code = app + "." + module + "." + code;
            }

            if (code.length() > 64) {
                throw ValidatorException.buildInvalidException(this.getClass(), "code is too long, the max length is 64");
            }
        }
        return code;
    }

    @Override
    public String toString() {
        return "TransMessageForm{" +
                "app='" + app + '\'' +
                ", module='" + module + '\'' +
                ", code='" + code + '\'' +
                ", messages=" + messages +
                '}';
    }
}
