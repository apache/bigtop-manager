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
import org.apache.bigtop.manager.dao.mapper.JobMapper;
import org.apache.bigtop.manager.dao.mapper.StageMapper;
import org.apache.bigtop.manager.dao.mapper.TaskMapper;
import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.server.command.CommandIdentifier;
import org.apache.bigtop.manager.server.command.factory.JobFactories;
import org.apache.bigtop.manager.server.command.factory.JobFactory;
import org.apache.bigtop.manager.server.command.job.Job;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.scheduler.JobScheduler;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.command.task.Task;
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
    private JobMapper jobMapper;

    @Resource
    private StageMapper stageMapper;

    @Resource
    private TaskMapper taskMapper;

    @Resource
    private JobScheduler jobScheduler;

    @Override
    public PageVO<JobVO> list(Long clusterId) {
        PageQuery pageQuery = PageUtils.getPageQuery();

        PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize(), pageQuery.getOrderBy());
        List<JobPO> jobPOList;
        if (ClusterUtils.isNoneCluster(clusterId)) {
            jobPOList = jobMapper.findAllByClusterIsNull();
        } else {
            jobPOList = jobMapper.findAllByClusterId(clusterId);
        }
        PageInfo<JobPO> pageInfo = new PageInfo<>(jobPOList);

        return PageVO.of(pageInfo);
    }

    @Override
    public JobVO get(Long id) {
        JobPO jobPO = jobMapper.findByIdJoin(id).orElseThrow(() -> new ApiException(ApiExceptionEnum.JOB_NOT_FOUND));
        return JobConverter.INSTANCE.fromPO2VO(jobPO);
    }

    @Override
    public JobVO retry(Long id) {
        JobPO jobPO = jobMapper.findByIdJoin(id).orElseThrow(() -> new ApiException(ApiExceptionEnum.JOB_NOT_FOUND));
        if (JobState.fromString(jobPO.getState()) != JobState.FAILED) {
            throw new ApiException(ApiExceptionEnum.JOB_NOT_RETRYABLE);
        }

        resetJobStatusInDB(jobPO);
        Job job = recreateJob(jobPO);
        jobScheduler.submit(job);

        return JobConverter.INSTANCE.fromPO2VO(jobPO);
    }

    private void resetJobStatusInDB(JobPO jobPO) {
        for (StagePO stagePO : jobPO.getStages()) {
            for (TaskPO taskPO : stagePO.getTasks()) {
                taskPO.setState(JobState.PENDING.getName());
                taskMapper.updateById(taskPO);
            }

            stagePO.setState(JobState.PENDING.getName());
            stageMapper.updateById(stagePO);
        }

        jobPO.setState(JobState.PENDING.getName());
        jobMapper.updateById(jobPO);
    }

    private Job recreateJob(JobPO jobPO) {
        JobContext jobContext = JsonUtils.readFromString(jobPO.getContext(), JobContext.class);
        CommandIdentifier commandIdentifier = new CommandIdentifier(
                jobContext.getCommandDTO().getCommandLevel(),
                jobContext.getCommandDTO().getCommand());
        JobFactory jobFactory = JobFactories.getJobFactory(commandIdentifier);
        Job job = jobFactory.createJob(jobContext);

        job.loadJobPO(jobPO);
        for (int i = 0; i < job.getStages().size(); i++) {
            Stage stage = job.getStages().get(i);
            StagePO stagePO = findCorrectStagePO(jobPO.getStages(), i + 1);
            if (stagePO == null) {
                throw new ApiException(ApiExceptionEnum.JOB_NOT_RETRYABLE);
            }

            stage.loadStagePO(stagePO);

            for (int j = 0; j < stage.getTasks().size(); j++) {
                Task task = stage.getTasks().get(j);
                TaskPO taskPO = findCorrectTaskPO(
                        stagePO.getTasks(), task.getTaskContext().getHostname());
                if (taskPO == null) {
                    throw new ApiException(ApiExceptionEnum.JOB_NOT_RETRYABLE);
                }

                task.loadTaskPO(taskPO);
            }
        }

        return job;
    }

    private StagePO findCorrectStagePO(List<StagePO> stagePOList, Integer order) {
        for (StagePO stagePO : stagePOList) {
            if (stagePO.getOrder().equals(order)) {
                return stagePO;
            }
        }

        return null;
    }

    private TaskPO findCorrectTaskPO(List<TaskPO> taskPOList, String hostname) {
        for (TaskPO taskPO : taskPOList) {
            if (taskPO.getHostname().equals(hostname)) {
                return taskPO;
            }
        }

        return null;
    }
}
