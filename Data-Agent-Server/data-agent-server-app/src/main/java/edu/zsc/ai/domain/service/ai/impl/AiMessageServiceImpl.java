package edu.zsc.ai.domain.service.ai.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.zsc.ai.domain.mapper.ai.AiMessageMapper;
import edu.zsc.ai.domain.model.entity.ai.AiMessage;
import edu.zsc.ai.domain.service.ai.AiMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AiMessageServiceImpl extends ServiceImpl<AiMessageMapper, AiMessage>
        implements AiMessageService {

    @Override
    public List<AiMessage> getMessagesByConversationId(Long conversationId) {
        if (conversationId == null) {
            return List.of();
        }
        Long currentUserId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<AiMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMessage::getConversationId, conversationId)
                .orderByAsc(AiMessage::getCreatedAt);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByConversationId(Long conversationId) {
        if (conversationId == null) {
            return;
        }
        Long currentUserId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<AiMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMessage::getConversationId, conversationId);
        this.remove(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchByConversationId(Long conversationId, List<AiMessage> messages) {
        if (conversationId == null || messages == null || messages.isEmpty()) {
            return true;
        }
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 设置conversationId和userId
        messages.forEach(msg -> {
            msg.setConversationId(conversationId);
        });
        return this.saveBatch(messages);
    }
}
