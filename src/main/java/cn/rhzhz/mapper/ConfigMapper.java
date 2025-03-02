package cn.rhzhz.mapper;

import cn.rhzhz.config.AIConfig;
import cn.rhzhz.enumType.FavoriteEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ConfigMapper {

    @Select("SELECT * FROM ai_config WHERE id = 1")
    AIConfig getConfig();
}