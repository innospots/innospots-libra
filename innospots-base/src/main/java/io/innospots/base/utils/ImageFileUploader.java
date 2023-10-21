/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.base.utils;

import cn.hutool.core.img.ImgUtil;
import io.innospots.base.enums.ImageType;
import io.innospots.base.model.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.zip.CRC32;

/**
 * image upload
 *
 * @author Smars
 * @date 2021/7/11
 */
@Slf4j
public class ImageFileUploader {

    public final static String IMG_TYPE_ICON = "icon";
    public final static String IMG_TYPE_LOGO = "logo";
    public final static List<String> IMG_SUFFIX_ICON = Arrays.asList("icon", "ico");
    public final static List<String> IMG_SUFFIX = Arrays.asList("jpg", "jpeg", "png", "webp");
    public final static List<String> IMG_SUFFIX_AVATAR = Arrays.asList("jpg", "jpeg", "png", "ico");
    //1048576 bytes = 1M
    final static Long IMG_SIZE_MAX = 1048576L;

    /**
     * fetch CRC32
     *
     * @param msg
     * @return
     */
    public static Long crc32Value(String msg) {
        if (msg == null) {
            return 0L;
        }
        byte[] bytes = msg.getBytes();
        CRC32 crc32 = new CRC32();
        crc32.reset();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

    /**
     * check file size
     *
     * @param file
     * @return
     */
    public static boolean checkSize(MultipartFile file) {
        return checkSize(file, IMG_SIZE_MAX);
    }

    /**
     * check file size
     *
     * @param file
     * @param fileSize
     * @return
     */
    public static boolean checkSize(MultipartFile file, Long fileSize) {
        if (file.getSize() > fileSize) {
            return false;
        }
        return true;
    }

    /**
     * check file suffix
     *
     * @param file
     * @return
     */
    public static boolean checkSuffix(MultipartFile file) {
        return checkSuffix(file, IMG_SUFFIX);
    }

    /**
     * check file suffix
     *
     * @param file
     * @param suffixList
     * @return
     */
    public static boolean checkSuffix(MultipartFile file, List<String> suffixList) {
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        if (!suffixList.contains(suffix.toLowerCase())) {
            return false;
        }
        return true;
    }

    /**
     * read file contents
     *
     * @param imgPath
     * @return byte[] file content
     * @throws IOException
     */
    public static byte[] readImage(String imgPath) throws IOException {
        if (imgPath == null || imgPath.length() <= 0) {
            throw new IOException("the file path is empty");
        }
        File file = new File(imgPath);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            throw new IOException("The image does not exist:" + imgPath);
        }
        byte[] bytes;
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            long len = file.length();
            bytes = new byte[(int) len];
            int r = bufferedInputStream.read(bytes);
            if (r != len) {
                throw new IOException("error read image:" + imgPath);
            }
        }
        return bytes;
    }

    /**
     * image base64
     *
     * @param imgPath
     * @return
     * @throws IOException
     */
    public static String readImageBase64(String imgPath) throws IOException {
        byte[] data = readImage(imgPath);
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(data);
    }

    public static File upload(MultipartFile file, File destFile, ImageType imageType) throws IOException {
        if (destFile.exists()) {
            throw new IOException("file already exists," + destFile);
        } else {
            if (!destFile.getParentFile().exists()) {
                boolean mkdir = destFile.getParentFile().mkdirs();
                if (!mkdir) {
                    throw new IOException("parentFile create failed," + destFile);
                }
            }
            if (!destFile.createNewFile()) {
                throw new IOException("file create failed," + destFile);
            }
        }
        BufferedImage bufferedImage = ImgUtil.read(file.getInputStream());
        Pair<Integer, Integer> size = reSize(bufferedImage.getWidth(),bufferedImage.getHeight(),imageType);
        Image image = ImgUtil.scale(bufferedImage,size.getLeft(), size.getRight());
        log.info("upload image: width:{}, height:{}, resize:{},{}",bufferedImage.getWidth(),bufferedImage.getHeight(),size, destFile.getAbsolutePath());
        ImgUtil.write(image,destFile);
//        ImgUtil.slice(bufferedImage,destFile.getParentFile(),size.getLeft(), size.getRight());
        return destFile;
    }

    private static Pair<Integer,Integer> reSize(int width, int height,ImageType imageType){
        Pair<Integer,Integer> pair = null;
        if(imageType.isFix()){
            if(width > imageType.getWidth() || height > imageType.getHeight()){
                width = imageType.getWidth();
                height = imageType.getHeight();
                pair = Pair.of(width,height);
            }else{
                pair = Pair.of(width,height);
            }
        }else{
            float scale = imageType.getHeight() * 1.0f / imageType.getWidth();
            if(width > imageType.getWidth()){
                width = imageType.getWidth();
                height = (int) (width * scale);
                pair = Pair.of(width, height);
            }else if(height > imageType.getHeight()){
                height = imageType.getHeight();
                width = (int) (height / scale);
                pair = Pair.of(width, height);
            }
        }
        return pair;
    }

    /**
     * @param file
     * @param parentPath
     * @param fileName
     * @return
     */
    public static File upload(MultipartFile file, String parentPath, String fileName,ImageType imageType) throws IOException {
        if (file == null) {
            return null;
        }
        File dir = new File(parentPath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File destFile = new File(dir, fileName);
        return upload(file, destFile,imageType);
    }


}