package com.pig4cloud.pig.monitor.notifier;

import com.optimus4cloud.optimus.common.notify.enums.NotifyType;
import com.optimus4cloud.optimus.common.notify.model.NotifyMessage;
import com.optimus4cloud.optimus.common.notify.service.cache.FtqqCaffeineServiceImpl;
import com.optimus4cloud.optimus.common.notify.template.MessageTemplate;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 发送Server酱通知消息
 *
 * @author clay
 * @version 1.0
 */
@Component
@ConditionalOnExpression("${notify.ftqq.enable:false}")
public class FtqqNotifier extends CustomNotifier {

	@Resource
	@Qualifier("ftqqMessageServiceImpl")
	private FtqqCaffeineServiceImpl messageService;

	protected FtqqNotifier(InstanceRepository repository) {
		super(repository);
	}

	@Override
	protected NotifyType getNotifyType() {
		return NotifyType.FTQQ;
	}

	@Override
	public void asynSendNotifyMessage(InstanceEvent event, Instance instance, String msgDescription) {
		NotifyMessage message = getNotifyMessage(event, instance, MessageTemplate.MONITOR_MARKDOWN_TEMPLATE_FTQQ,
				msgDescription);
		this.messageService.putMessage(message);
	}

}
