package com.sgswit.fx.controller.iTunes.vo.giftCard;

import cn.hutool.crypto.digest.MD5;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 账号管理器 - 支持有序循环使用
 */
public  class AccountManager {
    private static final int MAX_LOGIN_RETRY = 3;

    private static final Queue<AccountForQuery> availableAccounts = new ConcurrentLinkedQueue<>();
    private static final Set<String> processingAccounts = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> failureCount = new ConcurrentHashMap<>();

    /**
     * 添加可使用账户
     */
    public static void addAccountForQueryQueue(Queue<AccountForQuery> accountForQueries) {
        availableAccounts.clear();
        availableAccounts.addAll(accountForQueries);
    }
    /**
     * 获取一个可使用账户
     */
    public AccountForQuery getNextAccount() {
        // 优先使用可用队列中的新账号
        AccountForQuery account = availableAccounts.poll();
        if (account != null) {
            return account;
        }
        return null;
    }
    public static Set<String> getProcessingAccounts() {
        return processingAccounts;
    }
    public static Queue<AccountForQuery> getAvailableAccounts() {
        return availableAccounts;
    }
    /**
     * 账户标记成功
     */
    public void markLoginSuccess(AccountForQuery account) {
        if (account == null) return;
        processingAccounts.remove(account);
        availableAccounts.offer(account);
    }
    /**
     * 账户标记登录失败
     */
    public void markLoginFailure(AccountForQuery account, boolean isPasswordError) {
        if (account == null) return;
        processingAccounts.remove(account);
        if (isPasswordError) {
            if(availableAccounts.size()>0){
                availableAccounts.remove(account);
            }
            System.out.println("[账号失败] ❌ 密码错误账号永久移除: " + account.getAccountId());
        } else {
            int count = failureCount.getOrDefault(account.getAccountId(), 0) + 1;
            failureCount.put(account.getAccountId(), count);

            if (count <= MAX_LOGIN_RETRY) {
                availableAccounts.offer(account);
            } else {
                if(availableAccounts.size()>0){
                    availableAccounts.remove(account);
                }
                failureCount.remove(account.getAccountId());
            }
        }
    }
    public static int getAvailableAccountsCount() {
        return availableAccounts.size();
    }


    public int getProcessingAccountsCount() {
        return processingAccounts.size();
    }

    /**
     * 清空正在执行的任务
     */
    public static void clearProcessingAccounts() {
        processingAccounts.clear();
    }

    public static class AccountForQuery {
        private boolean passwordError;
        private final String accountId;
        private final String txtAccount;
        private final String txtPassword;

        public AccountForQuery(String txtAccount,String txtPassword) {
            this.accountId = MD5.create().digestHex(txtAccount+":"+txtPassword);
            this.txtAccount = txtAccount;
            this.txtPassword = txtPassword;
        }

        public String getTxtAccount() {
            return txtAccount;
        }

        public String getTxtPassword() {
            return txtPassword;
        }

        public String getAccountId() { return accountId; }

        public boolean isPasswordError() {
            return passwordError;
        }

        public void setPasswordError(boolean passwordError) {
            this.passwordError = passwordError;
        }
    }
}