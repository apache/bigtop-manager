package org.apache.bigtop.manager.dao.mapper;

import org.apache.bigtop.manager.dao.po.StackPO;
import org.apache.ibatis.annotations.Param;

public interface StackMapper extends BaseMapper<StackPO> {

    StackPO findByStackNameAndStackVersion(@Param("stackName") String stackName, @Param("stackVersion") String stackVersion);

}
