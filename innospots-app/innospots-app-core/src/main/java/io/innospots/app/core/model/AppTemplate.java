package io.innospots.app.core.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/11
 */
@Getter
@Setter
public class AppTemplate extends BaseAppInfo {

    protected LocalDate publishTime;

    protected String author;

    protected AppSetting appSetting;

    protected AppConfig appConfig;

}
