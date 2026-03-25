package com.velora.app.core.domain.storemanagement;

import com.velora.app.core.utils.ValidationUtils;
import java.util.UUID;

/**
 * Financial/account profile for payouts; decoupled from user credentials.
 */
public class ShopAccount {

    private final UUID accountId;
    private final UUID shopId;
    private String payoutRecipientName;
    private String payoutBankAccountRef;
    private boolean verified;

    public ShopAccount(UUID accountId, UUID shopId, String payoutRecipientName, String payoutBankAccountRef,
            boolean verified) {
        ValidationUtils.validateUUID(accountId, "accountId");
        ValidationUtils.validateUUID(shopId, "shopId");
        this.accountId = accountId;
        this.shopId = shopId;
        setPayoutRecipientName(payoutRecipientName);
        setPayoutBankAccountRef(payoutBankAccountRef);
        this.verified = verified;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getShopId() {
        return shopId;
    }

    public String getPayoutRecipientName() {
        return payoutRecipientName;
    }

    public void setPayoutRecipientName(String payoutRecipientName) {
        ValidationUtils.validateNotBlank(payoutRecipientName, "payoutRecipientName");
        this.payoutRecipientName = payoutRecipientName.trim();
    }

    public String getPayoutBankAccountRef() {
        return payoutBankAccountRef;
    }

    public void setPayoutBankAccountRef(String payoutBankAccountRef) {
        ValidationUtils.validateNotBlank(payoutBankAccountRef, "payoutBankAccountRef");
        this.payoutBankAccountRef = payoutBankAccountRef.trim();
    }

    public boolean isVerified() {
        return verified;
    }

    public void markVerified() {
        this.verified = true;
    }
}
