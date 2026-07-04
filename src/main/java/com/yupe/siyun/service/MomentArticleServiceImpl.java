package com.yupe.siyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupe.siyun.entity.JsMomentsArticle;
import com.yupe.siyun.mapper.JsMomentsArticleMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MomentArticleServiceImpl extends ServiceImpl<JsMomentsArticleMapper, JsMomentsArticle> implements MomentArticleService {
    @Override
    public List<JsMomentsArticle> search(String keywords) {
        String key = keywords == null ? "" : keywords;
        List<JsMomentsArticle> moments = new ArrayList<>();
        //title //comtent
        LambdaQueryWrapper<JsMomentsArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(JsMomentsArticle::getTitle, key)
                .or()
                .like(JsMomentsArticle::getContent, key);
        List<JsMomentsArticle> moment1 = this.list(wrapper);
        moments.addAll(moment1);
        return moments;
    }
}
