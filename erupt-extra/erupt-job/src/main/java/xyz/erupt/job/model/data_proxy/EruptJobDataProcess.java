package xyz.erupt.job.model.data_proxy;

import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.job.model.EruptJob;
import xyz.erupt.job.service.EruptJobService;
import xyz.erupt.jpa.dao.EruptDao;

import jakarta.annotation.Resource;
import jakarta.persistence.Transient;

import java.text.ParseException;
import java.util.List;

/**
 * @author YuePeng
 * date 2022/9/8 21:59
 */
@Slf4j
@Service
public class EruptJobDataProcess implements DataProxy<EruptJob>, OperationHandler<EruptJob, EruptJobExecDialog> {

    @Transient
    @Resource
    private EruptJobService eruptJobService;

    @Resource
    private EruptDao eruptDao;

    @Override
    public void beforeAdd(EruptJob eruptJob) {
        CronTriggerImpl trigger = new CronTriggerImpl();
        try {
            trigger.setCronExpression(eruptJob.getCron());
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException("Cron error " + e.getMessage(), e);
        }
    }

    @Override
    public void beforeUpdate(EruptJob eruptJob) {
        beforeAdd(eruptJob);
    }

    @Override
    public void afterAdd(EruptJob eruptJob) {
        try {
            eruptJobService.addJob(eruptJob);
        } catch (SchedulerException | ParseException e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    @Override
    public void afterUpdate(EruptJob eruptJob) {
        try {
            eruptJobService.modifyJob(eruptJob);
        } catch (SchedulerException | ParseException e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    @Override
    public void afterDelete(EruptJob eruptJob) {
        try {
            eruptJobService.deleteJob(eruptJob);
        } catch (SchedulerException e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    @Override
    public String exec(List<EruptJob> eruptJob, EruptJobExecDialog param, String[] operationParam) {
        String action = operationParam[0];
        try {
            EruptJob curJob = eruptJob.get(0);
            switch (action) {
                case "action":
                    for (EruptJob job : eruptJob) {
                        eruptDao.detach(job);
                        job.setHandlerParam(param.getParam());
                        eruptJobService.triggerJob(job);
                    }
                    break;
                case "pause":
                    curJob.setStatus(false);
                    eruptDao.mergeAndFlush(curJob);
                    eruptJobService.shutdown(curJob);
                    break;
                case "resume":
                    curJob.setStatus(true);
                    eruptDao.mergeAndFlush(curJob);
                    eruptJobService.deleteJob(curJob);
                    eruptJobService.addJob(curJob);
                    break;
                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("EruptJobDataProcess exec error", e);
            return "msg.error('" + e.getMessage() + "')";
        }
        return null;
    }

    @Override
    public EruptJobExecDialog eruptFormValue(List<EruptJob> data, EruptJobExecDialog eruptJobExecDialog, String[] param) {
        eruptJobExecDialog.setParam(data.get(0).getHandlerParam());
        return eruptJobExecDialog;
    }

    @Override
    public void addBehavior(EruptJob eruptJob) {
        eruptJob.setCode(Erupts.generateCode());
    }
}
