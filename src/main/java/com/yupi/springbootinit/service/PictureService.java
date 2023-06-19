package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.Picture;

/**
 * 图片服务
 */
public interface PictureService extends IService<Picture> {

    /**
     * 获取到网络图片列表
     *
     * @return
     */
    Page<Picture> searchPicture(String searchText, long pageNum, long pageSize);

}
