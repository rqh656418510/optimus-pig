package com.pig4cloud.pig.monitor.notifier;

import com.optimus4cloud.optimus.common.notify.enums.NotifyType;
import com.optimus4cloud.optimus.common.notify.model.NotifyMessage;
import com.optimus4cloud.optimus.common.notify.service.cache.EmailCaffeineServiceImpl;
import com.optimus4cloud.optimus.common.notify.template.MessageTemplate;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 发送邮件通知消息
 *
 * @author clay
 * @version 1.0
 */
@Component
@ConditionalOnExpression("${notify.email.enable:false}")
public class EmailNotifier extends CustomNotifier {

	@Resource
	@Qualifier("emailMessageServiceImpl")
	private EmailCaffeineServiceImpl messageService;

	protected EmailNotifier(InstanceRepository repository) {
		super(repository);
	}

	@Override
	protected NotifyType getNotifyType() {
		return NotifyType.EMAIL;
	}

	@Override
	public void asynSendNotifyMessage(InstanceEvent event, Instance instance, String msgDescription) {
		NotifyMessage message = getNotifyMessage(event, instance, MessageTemplate.MONITOR_TEXT_TEMPLATE_EMAIL,
				msgDescription);
		this.messageService.putMessage(message);
	}

}
