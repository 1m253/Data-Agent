package edu.zsc.ai.domain.service.ai.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.zsc.ai.domain.mapper.ai.AiMessageBlockMapper;
import edu.zsc.ai.domain.model.entity.ai.AiMessageBlock;
import edu.zsc.ai.domain.service.ai.AiMessageBlockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AiMessageBlockServiceImpl extends ServiceImpl<AiMessageBlockMapper, AiMessageBlock>
        implements AiMessageBlockService {

    @Override
    public List<AiMessageBlock> getBlocksByMessageId(Long messageId) {
        if (messageId == null) {
            return List.of();
        }
        LambdaQueryWrapper<AiMessageBlock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMessageBlock::getMessageId, messageId)
                .orderByAsc(AiMessageBlock::getId);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByMessageId(Long messageId) {
        if (messageId == null) {
            return;
        }
        LambdaQueryWrapper<AiMessageBlock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMessageBlock::getMessageId, messageId);
        this.remove(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchByMessageId(Long messageId, List<AiMessageBlock> blocks) {
        if (messageId == null || blocks == null || blocks.isEmpty()) {
            return true;
        }
        blocks.forEach(block -> block.setMessageId(messageId));
        return this.saveBatch(blocks);
    }
}
