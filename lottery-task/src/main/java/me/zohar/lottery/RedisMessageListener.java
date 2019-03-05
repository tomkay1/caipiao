package me.zohar.lottery;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import me.zohar.lottery.constants.Constant;
import me.zohar.lottery.issue.service.IssueService;
import me.zohar.lottery.rechargewithdraw.service.RechargeService;

@Slf4j
@Component
public class RedisMessageListener {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private IssueService issueService;

	@Autowired
	private RechargeService rechargeService;

	@PostConstruct
	public void init() {
		listenLotterySettlement();
		listenRechargeSettlement();
	}

	public void listenLotterySettlement() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						String issueId = redisTemplate.opsForList().rightPop(Constant.当前开奖期号ID, 2L, TimeUnit.SECONDS);
						if (StrUtil.isNotBlank(issueId)) {
							log.info("系统进行开奖结算操作,期号id为{}", issueId);
							issueService.settlement(issueId);
						}
					} catch (Exception e) {
						log.error("获取开奖结算消息异常", e);
					}
				}
			}
		}).start();
	}

	public void listenRechargeSettlement() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						String orderNo = redisTemplate.opsForList().rightPop(Constant.充值订单_已支付订单单号, 2L,
								TimeUnit.SECONDS);
						if (StrUtil.isNotBlank(orderNo)) {
							log.info("系统进行充值结算操作,订单单号为{}", orderNo);
							rechargeService.rechargeOrderSettlement(orderNo);
						}
					} catch (Exception e) {
						log.error("获取充值结算消息异常", e);
					}
				}
			}
		}).start();
	}

}
