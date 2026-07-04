package com.yupe.siyun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupe.siyun.entity.JsMomentsArticle;

import java.util.List;

public interface MomentArticleService extends IService<JsMomentsArticle> {
    List<JsMomentsArticle> search(String keywords);
}
