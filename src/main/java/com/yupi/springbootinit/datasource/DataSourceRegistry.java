package com.yupi.springbootinit.datasource;

import com.yupi.springbootinit.model.enums.SearchTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataSourceRegistry {

    @Resource
    private PictureDataSource pictureDataSource;
    @Resource
    private UserDataSource userDataSource;
    @Resource
    private PostDataSource postDataSource;

    private Map<String, DataSource> typeDataSource;

    // 依赖注入之后才去调用， 不这样的话会被提前注入，找不到后边的值
    @PostConstruct
    public void init() {
        typeDataSource = new HashMap<String, DataSource>() {{
            put(SearchTypeEnum.POST.getValue(), postDataSource);
            put(SearchTypeEnum.USER.getValue(), userDataSource);
            put(SearchTypeEnum.PICTURE.getValue(), pictureDataSource);
        }};
    }

    public DataSource getDataSoureByType(String type) {
        if (type == null) {
            return null;
        }
        return this.typeDataSource.get(type);
    }


}
