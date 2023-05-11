package org.zeveon.walletmanagementview.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.MultipleInstancesResponseType;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zeveon.walletmanagementview.api.dto.WalletBalanceHistoryRequest;
import org.zeveon.walletmanagementview.api.dto.WalletBalanceHistoryResponse;
import org.zeveon.walletmanagementview.domain.model.entity.WalletBalanceHistory;
import org.zeveon.walletmanagementview.domain.query.FindDefaultWalletHistoryByDateTimeRangeQuery;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Stanislav Vafin
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wallet")
public class WalletController {

    private final QueryGateway queryGateway;

    @PostMapping(value = "/default/history", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Flux<WalletBalanceHistoryResponse> getDefaultWalletHistory(
            @RequestBody @Valid WalletBalanceHistoryRequest request
    ) {
        var query = FindDefaultWalletHistoryByDateTimeRangeQuery.builder()
                .startDatetime(request.getStartDatetime())
                .endDatetime(request.getEndDatetime())
                .build();
        return Mono.fromFuture(queryGateway.query(query, new MultipleInstancesResponseType<>(WalletBalanceHistory.class)))
                .flatMapMany(histories -> Flux.fromStream(histories.stream()))
                .map(this::buildResponse);
    }

    private WalletBalanceHistoryResponse buildResponse(WalletBalanceHistory walletBalanceHistory) {
        return WalletBalanceHistoryResponse.builder()
                .dateTime(walletBalanceHistory.getUpdateTime())
                .amount(walletBalanceHistory.getBalance())
                .build();
    }
}
