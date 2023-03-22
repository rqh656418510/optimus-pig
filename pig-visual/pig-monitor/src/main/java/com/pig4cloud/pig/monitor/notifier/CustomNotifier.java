package com.pig4cloud.pig.monitor.notifier;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.optimus4cloud.optimus.common.notify.enums.NotifyType;
import com.optimus4cloud.optimus.common.notify.model.NotifyMessage;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.notify.AbstractStatusChangeNotifier;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.UUID;

/**
 * 自定义消息通知器
 *
 * @author clay
 * @version 1.0
 */
@Slf4j
public abstract class CustomNotifier extends AbstractStatusChangeNotifier {

	protected CustomNotifier(InstanceRepository repository) {
		super(repository);
	}

	/**
	 * 监听服务状态的变化
	 * @param event 事件
	 * @param instance 实例
	 */
	@Override
	protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
		return Mono.fromRunnable(() -> {
			if (event instanceof InstanceStatusChangedEvent) {
				log.info("Instance {} ({}) is {}", instance.getRegistration().getName(), event.getInstance(),
						((InstanceStatusChangedEvent) event).getStatusInfo().getStatus());

				String status = ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus();
				switch (status) {
					// 健康检查没通过
					case "DOWN":
						asynSendNotifyMessage(event, instance, "健康检查没通过");
						break;
					// 服务下线
					case "OFFLINE":
						asynSendNotifyMessage(event, instance, "服务下线");
						break;
					// 服务上线
					case "UP":
						asynSendNotifyMessage(event, instance, "服务上线");
						break;
					// 服务未知状态
					case "UNKNOWN":
						asynSendNotifyMessage(event, instance, "服务出现未知状态");
						break;
					default:
						break;
				}
			}
			else {
				log.info("Instance {} ({}) {}", instance.getRegistration().getName(), event.getInstance(),
						event.getType());
			}
		});
	}

	/**
	 * 获取通知消息
	 * @param event 事件
	 * @param instance 实例
	 * @param msgTemplate 消息模版
	 * @param msgDescription 消息内容
	 */
	protected NotifyMessage getNotifyMessage(InstanceEvent event, Instance instance, String msgTemplate,
			String msgDescription) {
		Date date = new Date();
		String instanceName = instance.getRegistration().getName();
		String instanceId = event.getInstance().toString();
		String status = ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus();
		String serviceUrl = instance.getRegistration().getServiceUrl();
		String dateTime = DateUtil.format(date, DatePattern.NORM_DATETIME_MS_PATTERN);
		// 根据消息模板生成消息内容
		String msgText = String.format(msgTemplate, instanceName, instanceId, status, msgDescription, serviceUrl,
				dateTime);
		// 封装通知消息
		NotifyMessage message = new NotifyMessage();
		message.setMsgText(msgText);
		message.setCreateTime(date);
		message.setMsgTitle("监控服务报警消息");
		message.setNotifyType(getNotifyType());
		message.setId(UUID.randomUUID().toString().replace("-", ""));
		return message;
	}

	/**
	 * 异步发送通知消息
	 * @param event 事件
	 * @param instance 实例
	 * @param msgDescription 消息内容
	 */
	protected abstract void asynSendNotifyMessage(InstanceEvent event, Instance instance, String msgDescription);

	/**
	 * 获取通知消息类型
	 */
	protected abstract NotifyType getNotifyType();

}
