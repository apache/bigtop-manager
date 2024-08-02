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

import jakarta.annotation.Resource;
import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.bigtop.manager.dao.repository.JobRepository;
import org.apache.bigtop.manager.dao.repository.StageRepository;
import org.apache.bigtop.manager.dao.repository.TaskRepository;
import org.apache.bigtop.manager.server.command.scheduler.JobScheduler;
import org.apache.bigtop.manager.server.model.converter.JobConverter;
import org.apache.bigtop.manager.server.model.query.PageQuery;
import org.apache.bigtop.manager.server.model.vo.JobVO;
import org.apache.bigtop.manager.server.model.vo.PageVO;
import org.apache.bigtop.manager.server.service.JobService;
import org.apache.bigtop.manager.server.utils.ClusterUtils;
import org.apache.bigtop.manager.server.utils.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class JobServiceImpl implements JobService {

    @Resource
    private JobRepository jobRepository;

    @Resource
    private StageRepository stageRepository;

    @Resource
    private TaskRepository taskRepository;

    @Resource
    private JobScheduler jobScheduler;

    @Override
    public PageVO<JobVO> list(Long clusterId) {
        PageQuery pageQuery = PageUtils.getPageQuery();
        Pageable pageable = PageRequest.of(pageQuery.getPageNum(), pageQuery.getPageSize(), pageQuery.getSort());
        Page<JobPO> page;
        if (ClusterUtils.isNoneCluster(clusterId)) {
            page = jobRepository.findAllByClusterPOIsNull(pageable);
        } else {
            page = jobRepository.findAllByClusterPOId(clusterId, pageable);
        }

        return PageVO.of(page);
    }

    @Override
    public JobVO get(Long id) {
        JobPO jobPO = jobRepository.getReferenceById(id);
        return JobConverter.INSTANCE.fromPO2VO(jobPO);
    }

    @Override
    public JobVO retry(Long id) {
        throw new RuntimeException("Api is in maintenance");
//        JobPO jobPO = jobRepository.getReferenceById(id);
//        if (jobPO.getState() != JobState.FAILED) {
//            throw new ApiException(ApiExceptionEnum.JOB_NOT_RETRYABLE);
//        }
//
//        for (StagePO stagePO : jobPO.getStagePOList()) {
//            for (TaskPO taskPO : stagePO.getTaskPOList()) {
//                taskPO.setState(JobState.PENDING);
//                taskRepository.save(taskPO);
//            }
//
//            stagePO.setState(JobState.PENDING);
//            stageRepository.save(stagePO);
//        }
//
//        jobPO.setState(JobState.PENDING);
//        jobRepository.save(jobPO);
//        jobScheduler.submit(jobPO);
//
//        return JobConverter.INSTANCE.fromPO2VO(jobPO);
    }
}
