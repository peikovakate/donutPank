package com.donutpank.bank.transaction;

import com.donutpank.bank.account.AccountRepository;
import com.donutpank.bank.common.BadRequestException;
import com.donutpank.bank.common.NotFoundException;
import com.donutpank.bank.ledger.LedgerEntry;
import com.donutpank.bank.ledger.LedgerRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private static final int MAX_PAGE_SIZE = 100;

    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;

    public TransactionService(AccountRepository accountRepository, LedgerRepository ledgerRepository) {
        this.accountRepository = accountRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @Transactional(readOnly = true)
    public TransactionHistoryPage listHistory(Long userId, Long accountId, String cursor, int limit) {
        accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        int pageSize = Math.min(limit, MAX_PAGE_SIZE);
        var pageable = PageRequest.of(0, pageSize + 1);

        List<LedgerEntry> entries = cursor == null
                ? ledgerRepository.findFirstPage(accountId, pageable)
                : ledgerRepository.findNextPage(accountId, decodeCursorCreatedAt(cursor),
                        decodeCursorId(cursor), pageable);

        boolean hasMore = entries.size() > pageSize;
        List<LedgerEntry> page = hasMore ? entries.subList(0, pageSize) : entries;
        String nextCursor = hasMore ? encodeCursor(page.getLast()) : null;

        return new TransactionHistoryPage(
                page.stream().map(TransactionHistoryItem::from).toList(),
                nextCursor);
    }

    private String encodeCursor(LedgerEntry entry) {
        String raw = entry.getCreatedAt().toString() + "," + entry.getId();
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private Instant decodeCursorCreatedAt(String cursor) {
        return Instant.parse(decodeCursorParts(cursor)[0]);
    }

    private Long decodeCursorId(String cursor) {
        return Long.parseLong(decodeCursorParts(cursor)[1]);
    }

    private String[] decodeCursorParts(String cursor) {
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decoded.split(",", 2);
            if (parts.length != 2) throw new BadRequestException("Invalid pagination cursor");
            return parts;
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid pagination cursor");
        }
    }
}
