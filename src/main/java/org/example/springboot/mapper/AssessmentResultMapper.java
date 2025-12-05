package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.springboot.entity.AssessmentResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测评结果Mapper
 * @author system
 */
@Mapper
public interface AssessmentResultMapper extends BaseMapper<AssessmentResult> {

    /**
     * 根据用户ID查询测评记录
     */
    List<AssessmentResult> selectByUserId(@Param("uid") String userId);

    /**
     * 根据用户ID和测评ID查询最新记录
     */
    AssessmentResult selectLatestByUserAndTest(@Param("uid") String userId, @Param("testid") String testId);
}