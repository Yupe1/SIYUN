package com.yupe.siyun.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yupe.siyun.entity.OpFrontUserWallet;
import com.yupe.siyun.mapper.OpFrontUserWalletMapper;
import com.yupe.siyun.util.ErrorType;
import com.yupe.siyun.util.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FrontWalletService {

    private static final BigDecimal MAX_RECHARGE = new BigDecimal("100000.00");

    @Autowired
    private OpFrontUserWalletMapper walletMapper;

    public BigDecimal balance(Integer userId) {
        OpFrontUserWallet wallet = walletMapper.selectById(userId);
        return money(wallet == null || wallet.getWallet() == null ? BigDecimal.ZERO : BigDecimal.valueOf(wallet.getWallet()));
    }

    @Transactional
    public BigDecimal recharge(Integer userId, BigDecimal amount) {
        BigDecimal safeAmount = money(amount);
        if (safeAmount.signum() <= 0 || safeAmount.compareTo(MAX_RECHARGE) > 0) {
            throw new MyException(ErrorType.WRONG_INFO, "充值金额须在0.01到100000元之间");
        }
        return addBalance(userId, safeAmount);
    }

    @Transactional
    public BigDecimal creditCourseIncome(Integer userId, BigDecimal amount) {
        BigDecimal safeAmount = money(amount);
        if (userId == null || safeAmount.signum() <= 0) {
            return userId == null ? BigDecimal.ZERO : balance(userId);
        }
        return addBalance(userId, safeAmount);
    }

    private BigDecimal addBalance(Integer userId, BigDecimal safeAmount) {
        int updated = walletMapper.update(
                null,
                new LambdaUpdateWrapper<OpFrontUserWallet>()
                        .eq(OpFrontUserWallet::getFrontUserId, userId)
                        .setSql("wallet = COALESCE(wallet, 0) + " + safeAmount.toPlainString())
        );
        if (updated == 0) {
            OpFrontUserWallet wallet = new OpFrontUserWallet();
            wallet.setFrontUserId(userId);
            wallet.setWallet(safeAmount.doubleValue());
            wallet.setVersion(0);
            walletMapper.insert(wallet);
        }
        return balance(userId);
    }

    public void debit(Integer userId, BigDecimal amount) {
        BigDecimal safeAmount = money(amount);
        if (safeAmount.signum() <= 0) {
            return;
        }
        int updated = walletMapper.update(
                null,
                new LambdaUpdateWrapper<OpFrontUserWallet>()
                        .eq(OpFrontUserWallet::getFrontUserId, userId)
                        .ge(OpFrontUserWallet::getWallet, safeAmount.doubleValue())
                        .setSql("wallet = wallet - " + safeAmount.toPlainString())
        );
        if (updated != 1) {
            throw new MyException(ErrorType.BALANCE_NOT_ENOUGH, "余额不足，请先充值");
        }
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
