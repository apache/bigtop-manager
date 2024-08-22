package org.apache.bigtop.manager.dao.mapper;

import org.apache.bigtop.manager.dao.po.UserPO;
import org.apache.ibatis.annotations.Param;

public interface UserMapper extends BaseMapper<UserPO> {

    UserPO findByUsername(@Param("username") String username);
}
