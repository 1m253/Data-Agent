package edu.zsc.ai.domain.service.ai;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.zsc.ai.domain.model.entity.ai.AiMessage;

import java.util.List;

public interface AiMessageService extends IService<AiMessage> {


    List<AiMessage> getMessagesByConversationId(Long conversationId);


    void deleteByConversationId(Long conversationId);


    boolean saveBatchByConversationId(Long conversationId, List<AiMessage> messages);
}
