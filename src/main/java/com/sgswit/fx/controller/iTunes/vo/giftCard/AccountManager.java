package com.sgswit.fx.controller.iTunes.vo.giftCard;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 账号管理器 - 支持有序循环使用
 */
public  class AccountManager {
    private static final int MAX_LOGIN_RETRY = 3;

    private static final Queue<AccountForQuery> availableAccounts = new ConcurrentLinkedQueue<>();
    private final Set<AccountForQuery> processingAccounts = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> failureCount = new ConcurrentHashMap<>();
    private final Set<String> passwordErrorAccounts = ConcurrentHashMap.newKeySet();
    private final Set<String> successAccounts = ConcurrentHashMap.newKeySet();
    private final Queue<AccountForQuery> successAccountQueue = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger totalAccountCount = new AtomicInteger(0);
    private final AtomicInteger completedAccountCount = new AtomicInteger(0);

    public static void addAccountForQuery(Queue<AccountForQuery> accountForQueries) {
        availableAccounts.addAll(accountForQueries);
        totalAccountCount.set(availableAccounts.size());
    }

    public AccountForQuery getNextAccountWithPriority() {
        // 优先使用可用队列中的新账号
        AccountForQuery account = availableAccounts.poll();
        if (account != null) {
            processingAccounts.add(account);
            String accountType = successAccounts.contains(account.getAccountId()) ? "成功账号" : "新账号";
            System.out.println(String.format(
                    "[账号获取] %s优先: %s (可用队列:%d, 成功队列:%d)",
                    accountType, account.getAccountId(), availableAccounts.size(), successAccountQueue.size()
            ));
            return account;
        }

        // 可用队列为空时，使用成功队列
        account = successAccountQueue.poll();
        if (account != null) {
            processingAccounts.add(account);
            System.out.println(String.format(
                    "[账号获取] 成功队列: %s (成功队列剩余:%d)",
                    account.getAccountId(), successAccountQueue.size()
            ));
            return account;
        }

        return null;
    }

    public void markLoginSuccess(AccountForQuery account) {
        if (account == null) return;

        processingAccounts.remove(account);
        successAccounts.add(account.getAccountId());
        failureCount.remove(account.getAccountId());

        // 成功账号放到成功队列末尾
        if(!successAccountQueue.contains(account)){
            successAccountQueue.offer(account);
        }
        System.out.println(String.format(
                "[账号成功] ✅ 账号: %s 登录成功，已加入成功队列 (成功队列大小: %d)",
                account.getAccountId(), successAccountQueue.size()
        ));
    }

    public void markLoginFailure(AccountForQuery account, boolean isPasswordError) {
        if (account == null) return;

        processingAccounts.remove(account);
        completedAccountCount.incrementAndGet();

        if (isPasswordError) {
            passwordErrorAccounts.add(account.getAccountId());
            failureCount.remove(account.getAccountId());
            removeFromSuccessQueue(account);
            System.out.println("[账号失败] ❌ 密码错误账号永久移除: " + account.getAccountId());
        } else {
            int count = failureCount.getOrDefault(account.getAccountId(), 0) + 1;
            failureCount.put(account.getAccountId(), count);

            if (count <= MAX_LOGIN_RETRY) {
                availableAccounts.offer(account);
                System.out.println(String.format(
                        "[账号重试] 🔄 账号: %s 第%d次重试",
                        account.getAccountId(), count
                ));
            } else {
                removeFromSuccessQueue(account);
                System.out.println("[账号移除] 💥 账号: " + account.getAccountId() + " 达到最大重试次数");
            }
        }
    }

    private void removeFromSuccessQueue(AccountForQuery account) {
        Iterator<AccountForQuery> iterator = successAccountQueue.iterator();
        while (iterator.hasNext()) {
            AccountForQuery acc = iterator.next();
            if (acc.getAccountId().equals(account.getAccountId())) {
                iterator.remove();
                break;
            }
        }
    }

    public void returnAccountToAppropriateQueue(AccountForQuery account) {
        if (account == null) return;

        if (successAccounts.contains(account.getAccountId())) {
            successAccountQueue.offer(account);
        } else {
            availableAccounts.offer(account);
        }
    }

    public boolean hasAvailableAccounts() {
        return !availableAccounts.isEmpty() || !successAccountQueue.isEmpty();
    }

    public int getTotalAvailableAccountsCount() {
        return availableAccounts.size() + successAccountQueue.size();
    }

    public int getAvailableAccountsCount() {
        return availableAccounts.size();
    }

    public int getSuccessAccountQueueSize() {
        return successAccountQueue.size();
    }

    public int getProcessingAccountsCount() {
        return processingAccounts.size();
    }

    public int getSuccessAccountCount() {
        return successAccounts.size();
    }

    public int getTotalAccountCount() {
        return totalAccountCount.get();
    }

    public boolean isAllAccountsProcessed() {
        return completedAccountCount.get() >= totalAccountCount.get();
    }

    public String getAccountStatus() {
        return String.format(
                "账号状态: 总数=%d, 已完成=%d, 成功=%d, 可用队列=%d, 成功队列=%d, 处理中=%d, 密码错误=%d, 重试失败=%d",
                totalAccountCount.get(), completedAccountCount.get(), successAccounts.size(),
                availableAccounts.size(), successAccountQueue.size(), processingAccounts.size(),
                passwordErrorAccounts.size(), failureCount.size()
        );
    }

    public String getAccountQueueInfo() {
        return String.format(
                "队列状态: 可用队列=%d (新/重试), 成功队列=%d (循环使用), 处理中=%d",
                availableAccounts.size(), successAccountQueue.size(), processingAccounts.size()
        );
    }
    /**
     * 账户执行状态跟踪器
     */
    public static class AccountExecutionTracker {
        private final Set<String> executingAccounts = ConcurrentHashMap.newKeySet();
        private final Map<String, Long> accountLastExecutionTime = new ConcurrentHashMap<>();

        public boolean canExecuteAccount(String accountId) {
            if (executingAccounts.contains(accountId)) {
                System.out.println("[执行跟踪] ❌ 账户正在执行中: " + accountId);
                return false;
            }

            Long lastExecTime = accountLastExecutionTime.get(accountId);
            if (lastExecTime != null) {
                long elapsed = System.currentTimeMillis() - lastExecTime;
                if (elapsed < 1000) {
                    System.out.println("[执行跟踪] ⏰ 账户执行间隔太短: " + accountId);
                    return false;
                }
            }

            return true;
        }

        public boolean markAccountExecuting(String accountId) {
            if (!canExecuteAccount(accountId)) {
                return false;
            }

            executingAccounts.add(accountId);
            accountLastExecutionTime.put(accountId, System.currentTimeMillis());

            System.out.println("[执行跟踪] ✅ 开始执行账户: " + accountId);
            return true;
        }

        public void markAccountCompleted(String accountId) {
            executingAccounts.remove(accountId);
            System.out.println("[执行跟踪] ✅ 完成执行账户: " + accountId);
        }

        public Set<String> getExecutingAccounts() {
            return new HashSet<>(executingAccounts);
        }

        public void clear() {
            executingAccounts.clear();
            System.out.println("[执行跟踪] 🧹 清空所有执行状态");
        }
    }
    public static class AccountForQuery {
        private boolean passwordError;
        private final String accountId;
        private final String txtAccountAndPassword;

        public AccountForQuery(String txtAccountAndPassword) {
            this.accountId = MD5.create().digestHex(txtAccountAndPassword);
            this.txtAccountAndPassword = txtAccountAndPassword;
        }

        public String getTxtAccountAndPassword() {
            return txtAccountAndPassword;
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