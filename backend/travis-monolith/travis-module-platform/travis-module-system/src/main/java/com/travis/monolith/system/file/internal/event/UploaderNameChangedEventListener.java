package com.travis.monolith.system.file.internal.event;

import com.travis.monolith.system.file.api.event.UploaderNameChangedEvent;
import com.travis.monolith.system.file.internal.service.SysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** 同步文件上传人名称 */
@Component
@RequiredArgsConstructor
public class UploaderNameChangedEventListener {

    private final SysFileService fileService;

    @ApplicationModuleListener
    void onUploaderNameChanged(UploaderNameChangedEvent event) {
        fileService.updateUploaderName(
                event.uploaderType(), event.uploaderId(), event.uploaderName());
    }
}
