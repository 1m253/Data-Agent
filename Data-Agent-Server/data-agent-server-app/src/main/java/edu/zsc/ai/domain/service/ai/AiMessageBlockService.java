package edu.zsc.ai.domain.service.ai;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.zsc.ai.domain.model.entity.ai.AiMessageBlock;

import java.util.List;

public interface AiMessageBlockService extends IService<AiMessageBlock> {


    List<AiMessageBlock> getBlocksByMessageId(Long messageId);


    void deleteByMessageId(Long messageId);


    boolean saveBatchByMessageId(Long messageId, List<AiMessageBlock> blocks);
}
