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

package io.innospots.libra.kernel.module.todo.operator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.enums.ImageType;
import io.innospots.base.exception.InnospotException;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.utils.ApplicationContextUtils;
import io.innospots.libra.base.controller.BaseController;
import io.innospots.libra.base.event.NewAvatarEvent;
import io.innospots.base.utils.ImageFileUploader;
import io.innospots.libra.kernel.module.todo.dao.TodoTaskCommentDao;
import io.innospots.libra.kernel.module.todo.entity.TodoTaskCommentEntity;
import io.innospots.libra.kernel.module.todo.mapper.TodoTaskCommentConvertMapper;
import io.innospots.libra.kernel.module.todo.model.TodoTaskComment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/8
 */
@Service
@Slf4j
public class TodoTaskCommentOperator extends ServiceImpl<TodoTaskCommentDao, TodoTaskCommentEntity> {

    @Transactional(rollbackFor = Exception.class)
    public TodoTaskComment createTodoTaskComment(TodoTaskComment todoTaskComment) {
        TodoTaskCommentConvertMapper mapper = TodoTaskCommentConvertMapper.INSTANCE;
        TodoTaskCommentEntity entity = mapper.model2Entity(todoTaskComment);
        super.save(entity);
        return mapper.entity2Model(entity);
    }

    public List<TodoTaskComment> getTodoTaskComments(Integer taskId) {
        QueryWrapper<TodoTaskCommentEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<TodoTaskCommentEntity> lambda = queryWrapper.lambda();
        lambda.eq(TodoTaskCommentEntity::getTaskId, taskId);

        List<TodoTaskCommentEntity> entities = super.list(queryWrapper);
        return entities.stream().map(TodoTaskCommentConvertMapper.INSTANCE::entity2Model).collect(Collectors.toCollection(() -> new ArrayList<>(entities.size())));
    }

    public List<Map<String, Object>> selectCountByTaskId(List<Integer> taskIds) {
        return super.listMaps(
                new QueryWrapper<TodoTaskCommentEntity>()
                        .select("TASK_ID, COUNT(*) CNT ")
                        .groupBy("TASK_ID")
                        .in("TASK_ID", taskIds));
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTodoTaskComment(Integer commentId) {
        return this.removeById(commentId);
    }

    public List<String> uploadImage(List<MultipartFile> uploadFiles) {
        long time = System.currentTimeMillis() / 1000;
        List<String> imageUrls = new ArrayList<>();
        for (int i = 0; i < uploadFiles.size(); i++) {
            MultipartFile uploadFile = uploadFiles.get(i);
            if (!ImageFileUploader.checkSize(uploadFile)) {
                throw InnospotException.buildException(this.getClass(), ResponseCode.IMG_SIZE_ERROR, ResponseCode.IMG_SIZE_ERROR.info());
            }
            if (!ImageFileUploader.checkSuffix(uploadFile, ImageFileUploader.IMG_SUFFIX_AVATAR)) {
                throw InnospotException.buildException(this.getClass(), ResponseCode.IMG_SUFFIX_ERROR, ResponseCode.IMG_SUFFIX_ERROR.info());
            }

            try {
                Path parentPath = Files.createTempDirectory("innospots_comment");
                String imgName = System.currentTimeMillis() + "_" + uploadFile.getOriginalFilename();
                ImageFileUploader.upload(uploadFile, parentPath.toFile().getAbsolutePath(), imgName,ImageType.COMMENT);
                String imgPath = parentPath.toFile().getAbsolutePath() + File.separator + imgName;
                String base64 = ImageFileUploader.readImageBase64(imgPath);
                //TODO save to disk
                ApplicationContextUtils.sendAppEvent(new NewAvatarEvent(time, ImageType.COMMENT, i, "data:image/png;base64," + base64));
                imageUrls.add(BaseController.PATH_ROOT_ADMIN + "image/" + ImageType.COMMENT + "/" + time + "?imageSort=" + i);

            } catch (IOException e) {
                log.error("upload avatar error:", e);
                throw InnospotException.buildException(this.getClass(), ResponseCode.IMG_UPLOAD_ERROR, ResponseCode.IMG_UPLOAD_ERROR.info());
            }
        }
        return imageUrls;
    }
}