package org.dhentech.infrastructure.export;

import org.dhentech.application.dto.TransferResponseDto;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class TransferCsvExporter {

    public byte[] toCsv(List<TransferResponseDto> transfers) {
        StringBuilder csv = new StringBuilder("id,sourceAccount,destinationAccount,amount,fee,transferDate,schedulingDate,status\n");
        for (TransferResponseDto t : transfers) {
            csv.append(t.getId()).append(",")
               .append(t.getSourceAccount()).append(",")
               .append(t.getDestinationAccount()).append(",")
               .append(t.getAmount()).append(",")
               .append(t.getFee()).append(",")
               .append(t.getTransferDate()).append(",")
               .append(t.getSchedulingDate()).append(",")
               .append(t.getStatus()).append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
}
