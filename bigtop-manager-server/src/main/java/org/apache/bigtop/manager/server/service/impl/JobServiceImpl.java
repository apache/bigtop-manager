/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.bigtop.manager.server.service.impl;

import org.apache.bigtop.manager.common.enums.JobState;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.bigtop.manager.dao.repository.JobDao;
import org.apache.bigtop.manager.server.command.CommandIdentifier;
import org.apache.bigtop.manager.server.command.factory.JobFactories;
import org.apache.bigtop.manager.server.command.factory.JobFactory;
import org.apache.bigtop.manager.server.command.job.Job;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.scheduler.JobScheduler;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.converter.JobConverter;
import org.apache.bigtop.manager.server.model.query.PageQuery;
import org.apache.bigtop.manager.server.model.vo.JobVO;
import org.apache.bigtop.manager.server.model.vo.PageVO;
import org.apache.bigtop.manager.server.service.JobService;
import org.apache.bigtop.manager.server.utils.ClusterUtils;
import org.apache.bigtop.manager.server.utils.PageUtils;

import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class JobServiceImpl implements JobService {

    @Resource
    private JobDao jobDao;

    @Resource
    private JobScheduler jobScheduler;

    @Override
    public PageVO<JobVO> list(Long clusterId) {
        PageQuery pageQuery = PageUtils.getPageQuery();

        PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize(), pageQuery.getOrderBy());
        List<JobPO> jobPOList;
        if (ClusterUtils.isNoneCluster(clusterId)) {
            jobPOList = jobDao.findAllByClusterIsNull();
        } else {
            jobPOList = jobDao.findAllByClusterId(clusterId);
        }
        PageInfo<JobPO> pageInfo = new PageInfo<>(jobPOList);
        List<JobPO> allByIdsJoin =
                jobDao.findAllByIdsJoin(jobPOList.stream().map(JobPO::getId).toList());
        pageInfo.setList(allByIdsJoin);

        return PageVO.of(pageInfo);
    }

    @Override
    public JobVO get(Long id) {
        JobPO jobPO = jobDao.findByIdJoin(id).orElseThrow(() -> new ApiException(ApiExceptionEnum.JOB_NOT_FOUND));
        return JobConverter.INSTANCE.fromPO2VO(jobPO);
    }

    @Override
    public JobVO retry(Long id) {
        JobPO jobPO = jobDao.findByIdJoin(id).orElseThrow(() -> new ApiException(ApiExceptionEnum.JOB_NOT_FOUND));
        if (JobState.fromString(jobPO.getState()) != JobState.FAILED) {
            throw new ApiException(ApiExceptionEnum.JOB_NOT_RETRYABLE);
        }

        // Create job
        JobContext jobContext = JsonUtils.readFromString(jobPO.getContext(), JobContext.class);
        CommandIdentifier commandIdentifier = new CommandIdentifier(
                jobContext.getCommandDTO().getCommandLevel(),
                jobContext.getCommandDTO().getCommand());
        JobFactory jobFactory = JobFactories.getJobFactory(commandIdentifier);

        jobContext.setJobId(id);
        Job job = jobFactory.createJob(jobContext);

        jobScheduler.submit(job);

        return JobConverter.INSTANCE.fromPO2VO(jobPO);
    }
}
