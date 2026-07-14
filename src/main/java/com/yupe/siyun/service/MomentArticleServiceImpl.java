package com.yupe.siyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupe.siyun.entity.ApxCourseCollectLog;
import com.yupe.siyun.entity.ApxCourseLikeLog;
import com.yupe.siyun.entity.JsMomentsArticle;
import com.yupe.siyun.mapper.ApxCourseCollectLogMapper;
import com.yupe.siyun.mapper.ApxCourseLikeLogMapper;
import com.yupe.siyun.mapper.JsMomentsArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MomentArticleServiceImpl extends ServiceImpl<JsMomentsArticleMapper, JsMomentsArticle> implements MomentArticleService {
    @Autowired
    private ApxCourseCollectLogMapper apxCourseCollectLogMapper;
    @Autowired
    private ApxCourseLikeLogMapper apxCourseLikeLogMapper;

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

    @Override
    public void updateCollect(JsMomentsArticle moment) {
        LambdaQueryWrapper<ApxCourseCollectLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApxCourseCollectLog::getCourseId,moment.getId());
        Long count = apxCourseCollectLogMapper.selectCount(wrapper);
        moment.setCountCollect(Integer.parseInt(count.toString()));
        this.updateById(moment);
    }

    @Override
    public void updateLike(JsMomentsArticle moment) {
        LambdaQueryWrapper<ApxCourseLikeLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApxCourseLikeLog::getCourseId,moment.getId());
        Long count = apxCourseLikeLogMapper.selectCount(wrapper);
        moment.setCountLike(Integer.parseInt(count.toString()));
        this.updateById(moment);
    }
}
