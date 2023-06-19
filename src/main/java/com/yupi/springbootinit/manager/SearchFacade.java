package com.yupi.springbootinit.manager;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.datasource.*;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.search.SearchRequest;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.model.enums.SearchTypeEnum;
import com.yupi.springbootinit.model.vo.PostVO;
import com.yupi.springbootinit.model.vo.SearchVO;
import com.yupi.springbootinit.model.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * 搜搜门面
 * 表示为一个组件
 */
@Component
@Slf4j
public class SearchFacade {

    @Resource
    private PictureDataSource pictureDataSource;
    @Resource
    private UserDataSource userDataSource;
    @Resource
    private PostDataSource postDataSource;
    @Resource
    private DataSourceRegistry dataSourceRegistry;


    public SearchVO searchAll(@RequestBody SearchRequest searchRequest) {
        String type = searchRequest.getType();
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);
        // 如果类型为空，则抛出异常, 请求参数错误
        ThrowUtils.throwIf(StringUtils.isBlank(type), ErrorCode.PARAMS_ERROR);
        String searchText = searchRequest.getSearchText();
        long current = searchRequest.getCurrent();
        long pageSize = searchRequest.getPageSize();
        // 如果为空就查询全部
        if (searchTypeEnum == null) {
            // 使用并发
            CompletableFuture<Page<Picture>> picturesTask = CompletableFuture.supplyAsync(() -> {
                Page<Picture> picturePage = pictureDataSource.doSearch(searchText, current, pageSize);
                return picturePage;
            });

            CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
                Page<UserVO> userVOPage = userDataSource.doSearch(searchText,current, pageSize);
                return userVOPage;
            });

            CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
                Page<PostVO> postVOPage = postDataSource.doSearch(searchText, current, pageSize);
                return postVOPage;
            });

            CompletableFuture.allOf(picturesTask, userTask, postTask).join();
            try {
                Page<Picture> picturePage = picturesTask.get();
                Page<UserVO> userVOPage = userTask.get();
                Page<PostVO> postVOPage = postTask.get();
                SearchVO searchVO = new SearchVO();
                searchVO.setPostList(postVOPage.getRecords());
                searchVO.setPictureList(picturePage.getRecords());
                searchVO.setUserList(userVOPage.getRecords());
                return searchVO;
            } catch (Exception e) {
                log.error("查询异常");
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询异常");
            }
        } else {
            DataSource<?> dataSource = dataSourceRegistry.getDataSoureByType(type);
            Page<?> page = dataSource.doSearch(searchText, current, pageSize);
            SearchVO searchVO = new SearchVO();
            searchVO.setDataList(page.getRecords());
            return searchVO;
        }


    }

}
