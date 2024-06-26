package com.pagepal.capstone.controllers;

import com.pagepal.capstone.dtos.setting.SettingDto;
import com.pagepal.capstone.dtos.transaction.ListTransactionDto;
import com.pagepal.capstone.dtos.transaction.TransactionDto;
import com.pagepal.capstone.dtos.transaction.TransactionFilterDto;
import com.pagepal.capstone.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @QueryMapping
    public ListTransactionDto getListTransactionForCustomer(@Argument("customerId") UUID customerId,
                                                            @Argument("filter") TransactionFilterDto filter) throws ParseException {
        return transactionService.getListTransactionForCustomer(customerId, filter);
    }

    @QueryMapping
    public ListTransactionDto getListTransactionForReader(@Argument("readerId") UUID readerId,
                                                          @Argument("filter") TransactionFilterDto filter) throws ParseException {
        return transactionService.getListTransactionForReader(readerId, filter);
    }

    @QueryMapping
    public TransactionDto getTransactionById(@Argument("id") UUID id) {
        return transactionService.getTransactionById(id);
    }

    @QueryMapping
    public List<SettingDto> getAllSettings() {
        return transactionService.getAllSettings();
    }

    @MutationMapping(name = "createSetting")
    public SettingDto createSetting(@Argument("key") String key, @Argument("value") String value) {
        return transactionService.createSetting(key, value);
    }

    @MutationMapping(name = "updateSetting")
    public SettingDto updateSetting(@Argument("setting") SettingDto settingDto) {
        return transactionService.updateSetting(settingDto);
    }
}
