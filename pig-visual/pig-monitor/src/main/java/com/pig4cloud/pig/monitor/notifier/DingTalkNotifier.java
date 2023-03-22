package com.pig4cloud.pig.monitor.notifier;

import com.optimus4cloud.optimus.common.notify.constants.DingTalkConstants;
import com.optimus4cloud.optimus.common.notify.enums.DingTalkMessageType;
import com.optimus4cloud.optimus.common.notify.enums.NotifyType;
import com.optimus4cloud.optimus.common.notify.model.NotifyMessage;
import com.optimus4cloud.optimus.common.notify.service.cache.DingTalkCaffeineServiceImpl;
import com.optimus4cloud.optimus.common.notify.template.MessageTemplate;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 发送钉钉群机器人通知消息
 *
 * @author clay
 * @version 1.0
 */
@Component
@ConditionalOnExpression("${notify.dingtalk.enable:false}")
public class DingTalkNotifier extends CustomNotifier {

	@Resource
	@Qualifier("dingTalkMessageServiceImpl")
	private DingTalkCaffeineServiceImpl messageService;

	protected DingTalkNotifier(InstanceRepository repository) {
		super(repository);
	}

	@Override
	protected NotifyType getNotifyType() {
		return NotifyType.Ding_TALK;
	}

	@Override
	public void asynSendNotifyMessage(InstanceEvent event, Instance instance, String msgDescription) {
		// 设置钉钉群机器人的消息类型
		Map<String, Object> data = new HashMap<>(1);
		data.put(DingTalkConstants.KEY_MESSAGE_TYPE, DingTalkMessageType.MARK_DOWN);
		// 异步发送通知消息
		NotifyMessage message = getNotifyMessage(event, instance, MessageTemplate.MONITOR_MARKDOWN_TEMPLATE_DINGTALK,
				msgDescription);
		message.setMsgData(data);
		this.messageService.putMessage(message);
	}

}
