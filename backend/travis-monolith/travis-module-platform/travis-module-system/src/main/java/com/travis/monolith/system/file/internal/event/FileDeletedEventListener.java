package com.travis.monolith.system.file.internal.event;

import com.travis.monolith.system.file.api.event.FileDeletedEvent;
import com.travis.monolith.system.file.internal.service.SysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** 在文件元数据事务提交后清理存储对象。 */
@Component
@RequiredArgsConstructor
public class FileDeletedEventListener {

    private final SysFileService fileService;

    @ApplicationModuleListener
    void onFileDeleted(FileDeletedEvent event) {
        fileService.deleteStorageObject(event.storageType(), event.storagePath(), event.path());
    }
}
