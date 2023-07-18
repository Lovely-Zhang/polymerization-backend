package com.yupi.springbootinit.esdao;

import com.yupi.springbootinit.model.dto.post.PostEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 帖子 ES 操作
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface PostEsDao extends ElasticsearchRepository<PostEsDTO, Long> {

    /**
     * 根据 id 查询帖子
     * @param userId
     * @return
     */
    List<PostEsDTO> findByUserId(Long userId);


    /**
     * 根据 标题 查询帖子
     * @param title
     * @return
     */
    List<PostEsDTO> findByTitle(String title);

}