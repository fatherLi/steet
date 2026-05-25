package com.street.street.iot.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * XXL-JOB 执行器配置类
 * 
 * 作用：
 * 将当前 street-iot-adapter 模块注册为 XXL-JOB 的一个执行节点 (Executor)。
 * 启动后会自动向 Admin 后台进行注册，以便 Admin 后台能够动态调用本模块内的 JobHandler。
 */
@Slf4j
@Configuration
public class XxlJobConfig {

    // 调度中心(Admin)的部署地址，多个地址用逗号分隔
    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    // 当前执行器的应用名，Admin 后台根据此名称识别节点
    @Value("${xxl.job.executor.appname}")
    private String appname;

    // 执行器内置的通讯端口，Admin 通过此端口下发任务
    @Value("${xxl.job.executor.port}")
    private int port;

    /**
     * 实例化 XXL-JOB 执行器
     * Spring 容器启动时会自动调用该方法完成注册
     */
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appname);
        xxlJobSpringExecutor.setPort(port);
        // 如果调度中心开启了访问令牌，这里也要 setAccessToken
        return xxlJobSpringExecutor;
    }
}
