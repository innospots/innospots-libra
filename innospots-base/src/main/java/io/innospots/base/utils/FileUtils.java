package io.innospots.base.utils;

import cn.hutool.core.io.IoUtil;
import io.innospots.base.execution.ExecutionResource;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/7
 */
@Slf4j
public class FileUtils {

    private static FileUtils fileUtils;

    public static FileUtils getInstance() {
        if (fileUtils == null) {
            fileUtils = new FileUtils();
        }

        return fileUtils;
    }

    public ExecutionResource upload(InputStream inputStream,
                                    String parentPath, String fileName,
                                    String contextPath, boolean force) {
        File parentFile = new File(parentPath);
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        File destFile = new File(parentFile, fileName);
        if (force && destFile.exists()) {
            destFile.delete();
        }

        if (force || !destFile.exists()) {
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(destFile));
                IoUtil.copy(inputStream, bos);
            } catch (FileNotFoundException e) {
                log.error(e.getMessage(), e);
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        return ExecutionResource.buildResource(destFile, false, contextPath);

    }

}
